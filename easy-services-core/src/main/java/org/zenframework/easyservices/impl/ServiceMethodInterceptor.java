package org.zenframework.easyservices.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
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
import org.zenframework.easyservices.util.debug.TimeStat;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ServiceMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceMethodInterceptor.class);

    private static final List<MethodIdentifier> OBJECT_METHODS = (List<MethodIdentifier>) CollectionUtils
            .collect(Arrays.asList(Object.class.getMethods()), new Transformer<Method, MethodIdentifier>() {

                @Override
                public MethodIdentifier transform(Method method) {
                    return new MethodIdentifier(method);
                }

            });

    protected final ClientFactoryImpl clientFactory;
    protected final ServiceLocator serviceLocator;
    protected final DescriptorFactory descriptorFactory;
    protected final SerializerFactory serializerFactory;
    protected final boolean outParametersMode;
    protected final ValueUpdater updater;
    protected final boolean invokeBaseObjectMethods;
    protected final boolean debug;

    public ServiceMethodInterceptor(ClientFactoryImpl clientFactory, ServiceLocator serviceLocator, boolean useDescriptors) {
        this.clientFactory = clientFactory;
        this.serviceLocator = serviceLocator;
        this.descriptorFactory = useDescriptors ? clientFactory.getDescriptorFactory() : null;
        this.serializerFactory = clientFactory.getSerializerFactory();
        this.outParametersMode = clientFactory.isOutParametersMode();
        this.updater = clientFactory.getUpdater();
        this.invokeBaseObjectMethods = clientFactory.isInvokeBaseObjectMethods();
        this.debug = clientFactory.isDebug();
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        MethodIdentifier methodId = new MethodIdentifier(method);

        if (!invokeBaseObjectMethods && OBJECT_METHODS.contains(methodId))
            return method.invoke(this, args);

        TimeStat timeStat = TimeStat.getThreadTimeStat();

        // Check service locator is absolute
        if (serviceLocator.isRelative())
            throw new ClientException("Service locator '" + serviceLocator + "' must be absolute");

        if (timeStat != null)
            timeStat.stage("initRequest");

        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
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

        if (timeStat != null)
            timeStat.stage("sendRequest");

        TimeChecker time = null;
        if ((debug || methodDescriptor.isDebug()) && LOG.isDebugEnabled())
            time = new TimeChecker("CALL " + serviceLocator.getServiceUrl() + '.' + getMethodName(method, methodDescriptor)
                    + getMethodParams(method, methodDescriptor), LOG);

        // Call service
        ClientRequest request = createRequest(method, methodDescriptor);
        request.writeRequestHeader();
        OutputStream out = request.getOutputStream();
        try {
            serializer.serialize(args, out);
        } finally {
            out.close();
        }

        if (timeStat != null)
            timeStat.stage("receiveResponse");

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
                if (timeStat != null)
                    timeStat.stage("replaceRef");
                // If result is reference, replace with proxy object
                ServiceLocator locator = (ServiceLocator) result;
                if (locator.isRelative())
                    locator = ServiceLocator.qualified(serviceLocator.getBaseUrl(), locator.getServiceName());
                result = clientFactory.getClient(method.getReturnType(), locator.getServiceName());
            }
            // Update OUT parameters
            if (outParams != null) {
                if (timeStat != null)
                    timeStat.stage("updateParameters");
                updateParameters(args, outParams, paramDescriptors);
            }
            if (time != null)
                time.printDifference(result);
            if (success)
                return result;
            else
                // TODO Do something with remote stack trace
                //throw new RuntimeException("Remote invocation exception", (Throwable) result);
                throw (Throwable) result;
        } finally {
            in.close();
            if (timeStat != null)
                timeStat.finish();
        }

    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    protected ClientRequest createRequest(Method method, MethodDescriptor methodDescriptor) throws IOException {
        return new ClientRequestImpl(clientFactory, serviceLocator, getMethodName(method, methodDescriptor));
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
