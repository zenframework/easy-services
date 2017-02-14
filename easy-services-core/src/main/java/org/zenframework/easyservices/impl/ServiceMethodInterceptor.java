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
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ResponseObject;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.ValueTransfer;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.ClassDescriptorFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ServiceMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceMethodInterceptor.class);

    private final ServiceLocator serviceLocator;
    private final Class<?> serviceClass;
    private final ClassDescriptorFactory serviceDescriptorFactory;
    private final SerializerFactory serializerFactory;
    private final boolean debug;

    public ServiceMethodInterceptor(ServiceLocator serviceLocator, Class<?> serviceClass, ClassDescriptorFactory serviceDescriptorFactory,
            SerializerFactory serializerFactory, boolean debug) {
        this.serviceLocator = serviceLocator;
        this.serviceClass = serviceClass;
        this.serviceDescriptorFactory = serviceDescriptorFactory;
        this.serializerFactory = serializerFactory;
        this.debug = debug;
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        // Check service locator is absolute
        if (serviceLocator.isRelative())
            throw new ClientException("Service locator '" + serviceLocator + "' must be absolute");

        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        ClassDescriptor classDescriptor = serviceDescriptorFactory != null ? serviceDescriptorFactory.getClassDescriptor(serviceClass) : null;
        MethodDescriptor methodDescriptor = classDescriptor != null ? classDescriptor.getMethodDescriptor(method) : null;
        ValueDescriptor[] argDescriptors = methodDescriptor != null ? methodDescriptor.getParameterDescriptors() : new ValueDescriptor[args.length];
        Serializer serializer = serializerFactory.getSerializer(paramTypes, returnType, methodDescriptor);

        // Find and replace proxy objects with references
        for (int i = 0; i < args.length; i++) {
            ValueDescriptor argDescriptor = argDescriptors[i];
            if (argDescriptor != null && argDescriptor.getTransfer() == ValueTransfer.REF) {
                //ServiceInvocationHandler handler = (ServiceInvocationHandler) Proxy.getInvocationHandler(args[i]);
                ServiceMethodInterceptor intercpetor = ClientProxy.getMethodInterceptor(args[i], ServiceMethodInterceptor.class);
                args[i] = intercpetor.getServiceLocator();
            }
        }

        TimeChecker time = null;
        if ((debug || isMethodDebug(classDescriptor, methodDescriptor)) && LOG.isDebugEnabled())
            time = new TimeChecker("CALL " + serviceLocator.getServiceUrl() + ' ' + getMethodName(method, methodDescriptor), LOG);

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
        try {
            InputStream in = connection.getInputStream();
            ValueDescriptor returnDescriptor = methodDescriptor != null ? methodDescriptor.getReturnDescriptor() : null;
            if (returnDescriptor != null && returnDescriptor.getTransfer() == ValueTransfer.REF) {
                // If result is reference, replace with proxy object
                responseObject = serializer.deserializeResponse(in, true);
                ServiceLocator locator = (ServiceLocator) responseObject.getResult();
                if (locator.isRelative())
                    locator = ServiceLocator.qualified(serviceLocator.getBaseUrl(), locator.getServiceName());
                responseObject
                        .setResult(ClientProxy.getCGLibProxy(method.getReturnType(), locator, serviceDescriptorFactory, serializerFactory, debug));
            } else {
                // Else return deserialized result
                responseObject = serializer.deserializeResponse(in, true);
            }
            if (time != null)
                time.printDifference(responseObject);
        } catch (IOException e) {
            tryHandleError(connection, serializer, method.getParameterTypes(), argDescriptors);
            throw e;
        }

        if (responseObject.isSuccess())
            return responseObject.getResult();
        else
            throw (Throwable) responseObject.getResult();
    }

    private boolean isMethodDebug(ClassDescriptor classDescriptor, MethodDescriptor methodDescriptor) {
        return methodDescriptor != null ? methodDescriptor.isDebug() : classDescriptor != null ? classDescriptor.isDebug() : false;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    // TODO must be extendable
    private void tryHandleError(URLConnection connection, Serializer serializer, Class<?>[] paramTypes, ValueDescriptor[] paramDescriptors)
            throws Throwable {
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw (Throwable) serializer.deserializeResponse(httpConnection.getErrorStream(), false).getResult();
        }
    }

    private static URL getServiceURL(ServiceLocator serviceLocator, String methodName) throws MalformedURLException {
        return new URL(serviceLocator.getServiceUrl() + "?method=" + methodName);
    }

    private static String getMethodName(Method method, MethodDescriptor methodDescriptor) {
        String alias = methodDescriptor != null ? methodDescriptor.getAlias() : null;
        return alias != null ? alias : method.getName();
    }

}
