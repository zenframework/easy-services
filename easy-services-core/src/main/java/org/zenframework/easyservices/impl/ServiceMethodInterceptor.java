package org.zenframework.easyservices.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ResponseObject;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.DescriptorFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.MethodIdentifier;
import org.zenframework.easyservices.descriptor.ParamDescriptor;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.descriptor.ValueTransfer;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.update.ValueUpdater;
import org.zenframework.easyservices.util.debug.TimeChecker;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ServiceMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceMethodInterceptor.class);

    private final ServiceLocator serviceLocator;
    private final DescriptorFactory descriptorFactory;
    private final SerializerFactory serializerFactory;
    private final boolean outParametersMode;
    private final ValueUpdater updater;
    private final boolean debug;

    public ServiceMethodInterceptor(ServiceLocator serviceLocator, DescriptorFactory descriptorFactory, SerializerFactory serializerFactory,
            boolean outParametersMode, ValueUpdater updater, boolean debug) {
        this.serviceLocator = serviceLocator;
        this.descriptorFactory = descriptorFactory;
        this.serializerFactory = serializerFactory;
        this.outParametersMode = outParametersMode;
        this.updater = updater;
        this.debug = debug;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        // Check service locator is absolute
        if (serviceLocator.isRelative())
            throw new ClientException("Service locator '" + serviceLocator + "' must be absolute");

        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        MethodIdentifier methodId = new MethodIdentifier(method);
        ClassDescriptor classDescriptor = descriptorFactory != null ? descriptorFactory.getClassDescriptor(method.getDeclaringClass()) : null;
        MethodDescriptor methodDescriptor = classDescriptor != null ? classDescriptor.getMethodDescriptor(methodId)
                : new MethodDescriptor(args.length);
        ParamDescriptor[] paramDescriptors = methodDescriptor.getParameterDescriptors();
        ValueDescriptor returnDescriptor = methodDescriptor.getReturnDescriptor();
        Serializer serializer = serializerFactory.getSerializer(paramTypes, returnType, methodDescriptor);

        // Find and replace proxy objects with references
        for (int i = 0; i < args.length; i++) {
            ParamDescriptor paramDescriptor = paramDescriptors[i];
            if (paramDescriptor != null && paramDescriptor.getTransfer() == ValueTransfer.REF) {
                ServiceMethodInterceptor intercpetor = ClientProxy.getMethodInterceptor(args[i], ServiceMethodInterceptor.class);
                args[i] = intercpetor.getServiceLocator();
            }
        }

        TimeChecker time = null;
        if ((debug || methodDescriptor.getDebug()) && LOG.isDebugEnabled())
            time = new TimeChecker("CALL " + serviceLocator.getServiceUrl() + '.' + getMethodName(method, methodDescriptor)
                    + getMethodParams(method, methodDescriptor), LOG);

        // Call service
        URL url = getServiceURL(serviceLocator, getMethodName(method, methodDescriptor));
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        OutputStream out = connection.getOutputStream();
        try {
            //out.write("args=");
            serializer.serialize(args, out);
        } finally {
            out.close();
        }

        // Receive response
        ResponseObject responseObject = null;
        InputStream in = null;
        try {
            in = connection.getInputStream();
            if (outParametersMode) {
                responseObject = serializer.deserializeResponse(in, true);
            } else {
                responseObject = new ResponseObject();
                if (returnType != void.class)
                    responseObject.setResult(serializer.deserializeResult(in, true));
            }
            if (returnDescriptor != null && returnDescriptor.getTransfer() == ValueTransfer.REF) {
                // If result is reference, replace with proxy object
                ServiceLocator locator = (ServiceLocator) responseObject.getResult();
                if (locator.isRelative())
                    locator = ServiceLocator.qualified(serviceLocator.getBaseUrl(), locator.getServiceName());
                responseObject.setResult(ClientProxy.getCGLibProxy(method.getReturnType(), locator, descriptorFactory, serializerFactory,
                        outParametersMode, updater, debug));
            }
            // Update OUT parameters
            for (int i = 0; i < paramDescriptors.length; i++) {
                ParamDescriptor paramDescriptor = paramDescriptors[i];
                ValueTransfer transfer = paramDescriptor != null ? paramDescriptor.getTransfer() : null;
                Object[] outParams = responseObject.getParameters();
                if (transfer == ValueTransfer.OUT && outParams != null)
                    updater.update(args[i], outParams[i]);
            }
            if (time != null)
                time.printDifference(responseObject);
            return responseObject.getResult();
        } catch (IOException e) {
            tryHandleError(connection, serializer, method.getParameterTypes(), paramDescriptors);
            throw e;
        } finally {
            if (in != null)
                in.close();
        }

    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    // TODO must be extendable
    private void tryHandleError(URLConnection connection, Serializer serializer, Class<?>[] paramTypes, ValueDescriptor[] paramDescriptors)
            throws Throwable {
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                InputStream in = httpConnection.getErrorStream();
                Throwable e = (Throwable) (outParametersMode ? serializer.deserializeResponse(in, false).getResult()
                        : serializer.deserializeResult(in, false));
                throw e;
            }
        }
    }

    private URL getServiceURL(ServiceLocator serviceLocator, String methodName) throws MalformedURLException {
        StringBuilder str = new StringBuilder();
        str.append(serviceLocator.getServiceUrl()).append("?method=").append(methodName);
        if (outParametersMode)
            str.append("&outParameters=true");
        return new URL(str.toString());
    }

    private static String getMethodName(Method method, MethodDescriptor methodDescriptor) {
        String alias = methodDescriptor != null ? methodDescriptor.getAlias() : null;
        return alias != null ? alias : method.getName();
    }

    private static String getMethodParams(Method method, MethodDescriptor methodDescriptor) {
        Class<?>[] paramTypes = method.getParameterTypes();
        StringBuilder str = new StringBuilder(paramTypes.length * 20).append('(');
        ValueDescriptor[] valueDescriptors = methodDescriptor != null ? methodDescriptor.getParameterDescriptors()
                : new ValueDescriptor[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            ValueDescriptor valueDescriptor = valueDescriptors[i];
            str.append(paramTypes[i].getSimpleName());
            if (valueDescriptor != null && valueDescriptor.getTransfer() != null)
                str.append(valueDescriptor.getTransfer().getMarker());
            if (i < paramTypes.length - 1)
                str.append(", ");
        }
        return str.append(')').toString();
    }

}
