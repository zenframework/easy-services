package org.zenframework.easyservices.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ClientRequest;
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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ServiceMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceMethodInterceptor.class);

    protected final ClientFactoryImpl clientFactory;
    protected final ServiceLocator serviceLocator;
    protected final DescriptorFactory descriptorFactory;
    protected final SerializerFactory serializerFactory;
    protected final boolean outParametersMode;
    protected final ValueUpdater updater;
    protected final boolean debug;

    public ServiceMethodInterceptor(ClientFactoryImpl clientFactory, ServiceLocator serviceLocator, boolean useDescriptors) {
        this.clientFactory = clientFactory;
        this.serviceLocator = serviceLocator;
        this.descriptorFactory = useDescriptors ? clientFactory.getDescriptorFactory() : null;
        this.serializerFactory = clientFactory.getSerializerFactory();
        this.outParametersMode = clientFactory.isOutParametersMode();
        this.updater = clientFactory.getUpdater();
        this.debug = clientFactory.isDebug();
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
        Serializer serializer = serializerFactory.getSerializer(paramTypes, returnType, methodDescriptor, outParametersMode);

        // Find and replace proxy objects with references
        for (int i = 0; i < args.length; i++) {
            ParamDescriptor paramDescriptor = paramDescriptors[i];
            if (paramDescriptor != null && paramDescriptor.getTransfer() == ValueTransfer.REF) {
                ServiceMethodInterceptor intercpetor = getMethodInterceptor(args[i], ServiceMethodInterceptor.class);
                args[i] = intercpetor.getServiceLocator();
            }
        }

        String methodName = getMethodName(method, methodDescriptor);
        TimeChecker time = null;
        if ((debug || methodDescriptor.getDebug()) && LOG.isDebugEnabled())
            time = new TimeChecker("CALL " + serviceLocator.getServiceUrl() + '.' + methodName + getMethodParams(method, methodDescriptor), LOG);

        // Call service
        ClientRequest request = createRequest(methodName);
        request.writeRequestHeader();
        OutputStream out = request.getOutputStream();
        try {
            serializer.serialize(args, out);
        } finally {
            out.close();
        }

        // Receive response
        request.readResponseHeader();
        InputStream in = request.getInputStream();
        boolean success = request.isSuccessful();
        Object result = null;
        Object[] outParams = null;
        try {
            if (returnType != void.class || !success || outParametersMode)
                result = serializer.deserializeResult(in, success);
            if (result instanceof ResponseObject) {
                ResponseObject responseObject = (ResponseObject) result;
                outParams = responseObject.getParameters();
                result = responseObject.getResult();
            }
            if (success && returnDescriptor != null && returnDescriptor.getTransfer() == ValueTransfer.REF) {
                // If result is reference, replace with proxy object
                ServiceLocator locator = (ServiceLocator) result;
                if (locator.isRelative())
                    locator = ServiceLocator.qualified(serviceLocator.getBaseUrl(), locator.getServiceName());
                result = clientFactory.getClient(method.getReturnType(), locator.getServiceName());
            }
            // Update OUT parameters
            if (outParams != null)
                updateParameters(args, outParams, paramDescriptors);
            if (time != null)
                time.printDifference(result);
            if (success)
                return result;
            else
                throw (Throwable) result;
        } finally {
            in.close();
        }

    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    protected ClientRequest createRequest(String methodName) throws IOException {
        return new ClientRequestImpl(clientFactory, serviceLocator, methodName);
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

    private void updateParameters(Object[] params, Object[] outParams, ParamDescriptor[] paramDescriptors) {
        for (int i = 0; i < paramDescriptors.length; i++) {
            ParamDescriptor paramDescriptor = paramDescriptors[i];
            ValueTransfer transfer = paramDescriptor != null ? paramDescriptor.getTransfer() : null;
            if (transfer == ValueTransfer.OUT && outParams != null)
                updater.update(params[i], outParams[i]);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends MethodInterceptor> T getMethodInterceptor(Object proxy, Class<T> interceptorClass) {
        if (proxy instanceof Factory) {
            Factory factory = (Factory) proxy;
            Callback callback = factory.getCallback(0);
            if (interceptorClass.isInstance(callback)) {
                return (T) callback;
            }
        }
        throw new RuntimeException(proxy + " is not a proxy object");
    }

}
