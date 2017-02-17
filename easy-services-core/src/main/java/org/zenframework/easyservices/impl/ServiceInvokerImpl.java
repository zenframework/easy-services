package org.zenframework.easyservices.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ResponseObject;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.ServiceRequest;
import org.zenframework.easyservices.ServiceResponse;
import org.zenframework.easyservices.ValueTransfer;
import org.zenframework.easyservices.descriptor.DefaultDescriptorFactory;
import org.zenframework.easyservices.descriptor.DescriptorFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.MethodIdentifier;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.jndi.JNDIHelper;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.util.cls.ClassInfo;
import org.zenframework.easyservices.util.config.Config;
import org.zenframework.easyservices.util.config.Configurable;
import org.zenframework.easyservices.util.debug.TimeChecker;

public class ServiceInvokerImpl implements ServiceInvoker, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvokerImpl.class);

    private static final String PARAM_SERVICE_REGISTRY = "serviceRegistry";
    private static final String PARAM_SERIALIZER_FACTORY = "serializerFactory";
    private static final String PARAM_DESCRIPTOR_FACTORY = "descriptorFactory";
    private static final String PARAM_DEBUG = "debug";

    private Context serviceRegistry = JNDIHelper.getDefaultContext();
    private DescriptorFactory descriptorFactory = new DefaultDescriptorFactory();
    private SerializerFactory serializerFactory = Environment.getSerializerFactory();
    private boolean duplicateMethodNamesSafe = Environment.isDuplicateMethodNamesSafe();
    private boolean debug = Environment.isDebug();

    @Override
    public void invoke(ServiceRequest request, ServiceResponse response) throws IOException {

        ResponseObject responseObject = new ResponseObject();
        InvocationContext context = null;
        String methodName = request.getMethodName();
        boolean outParametersMode = request.isOutParametersMode();

        if (debug && LOG.isDebugEnabled()) {
            request.cacheInput();
            LOG.debug("CALL " + request.getServiceName() + '.' + methodName + ' ' + read(request.getInputStream()));
        }

        try {
            Object service = lookupService(request);
            if (methodName == null) {
                context = new InvocationContext(null, null, serializerFactory.getSerializer(null, Map.class, null), null);
                responseObject.setResult(getServiceInfo(request, service));
            } else {
                context = getInvocationContext(request, service.getClass());
                responseObject.setResult(invokeMethod(request, service, context));
                if (outParametersMode)
                    responseObject.setParameters(getOutParameters(context));
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
                LOG.info("Error invoking " + request.getServiceName() + '.' + request.getMethodName() + ' ' + context, e);
            } else {
                LOG.error("Error invoking " + request.getServiceName() + '.' + request.getMethodName() + ' ' + context, e);
            }
            responseObject.setResult(e);
            responseObject.setSuccess(false);
            response.sendError(e);
        }
        OutputStream out = response.getOutputStream();
        try {
            Serializer serializer = context != null ? context.serializer : serializerFactory.getSerializer(null, Map.class, null);
            serializer.serialize(outParametersMode ? responseObject : responseObject.getResult(), out);
        } finally {
            out.close();
        }
    }

    @Override
    public void init(Config config) {
        Config serviceRegistryConfig = config.getSubConfig(PARAM_SERVICE_REGISTRY);
        if (!serviceRegistryConfig.isEmpty())
            serviceRegistry = JNDIHelper.newDefaultContext(serviceRegistryConfig);
        descriptorFactory = (DescriptorFactory) config.getInstance(PARAM_DESCRIPTOR_FACTORY, descriptorFactory);
        serializerFactory = (SerializerFactory) config.getInstance(PARAM_SERIALIZER_FACTORY, serializerFactory);
        debug = config.getParam(PARAM_DEBUG, false);
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(descriptorFactory, serializerFactory);
    }

    public void setServiceRegistry(Context serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory) {
        this.descriptorFactory = descriptorFactory;
    }

    public Context getServiceRegistry() {
        return serviceRegistry;
    }

    public DescriptorFactory getClassDescriptorFactory() {
        return descriptorFactory;
    }

    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setDuplicateMethodNamesSafe(boolean duplicateMethodNamesSafe) {
        this.duplicateMethodNamesSafe = duplicateMethodNamesSafe;
    }

    public DescriptorFactory getDescriptorFactory() {
        return descriptorFactory;
    }

    public boolean isDuplicateMethodNamesSafe() {
        return duplicateMethodNamesSafe;
    }

    protected Object getServiceInfo(ServiceRequest request, Object service) throws IOException {
        TimeChecker time = debug && LOG.isDebugEnabled() ? new TimeChecker("INFO " + request.getServiceName(), LOG) : null;
        ClassInfo classInfo = ClassInfo.getClassRef(service.getClass()).getClassInfo();
        Map<String, Object> serviceInfo = new HashMap<String, Object>();
        serviceInfo.put("className", classInfo.getName());
        serviceInfo.put("classes", ClassInfo.getClassInfos(service.getClass()));
        if (time != null)
            time.printDifference(serviceInfo);
        return serviceInfo;
    }

    protected Object invokeMethod(ServiceRequest request, Object service, InvocationContext context) throws Throwable {
        TimeChecker time = (debug || context.methodDescriptor.getDebug()) && LOG.isDebugEnabled()
                ? new TimeChecker("INVOKE " + request.getServiceName() + '.' + request.getMethodName(), LOG) : null;
        Object result = context.method.invoke(service, context.params);
        // If method must return reference, bind result to service register and set result to service locator
        ValueDescriptor returnDescriptor = context.methodDescriptor.getReturnDescriptor();
        if (returnDescriptor != null && returnDescriptor.getTransfer() == ValueTransfer.REF) {
            String name = getName(result);
            try {
                serviceRegistry.lookup(name);
            } catch (NamingException e) {
                try {
                    serviceRegistry.bind(name, result);
                } catch (NamingException e1) {
                    throw new ServiceException("Can't bind service " + name);
                }
            }
            result = ServiceLocator.relative(name);
        }
        if (time != null)
            time.printDifference(result);
        return result;
    }

    protected Object lookupService(ServiceRequest request) throws ServiceException {
        String serviceName = request.getServiceName();
        if (DescriptorFactory.NAME.equals(serviceName))
            return descriptorFactory;
        try {
            return serviceRegistry.lookup(serviceName);
        } catch (NamingException e) {
            throw new ServiceException("Service " + serviceName + " not found");
        }
    }

    protected static String getName(Object obj) {
        return "/dynamic/" + obj.getClass().getCanonicalName() + '@' + System.identityHashCode(obj);
    }

    private InvocationContext getInvocationContext(ServiceRequest request, Class<?> serviceClass)
            throws IOException, ServiceException, NoSuchMethodException, SecurityException {
        TimeChecker time = debug && LOG.isDebugEnabled()
                ? new TimeChecker("GET CONTEXT " + request.getServiceName() + "." + request.getMethodName(), LOG) : null;
        Map<MethodIdentifier, MethodDescriptor> methodDescriptors = descriptorFactory.getMethodDescriptors(serviceClass);
        for (Map.Entry<MethodIdentifier, MethodDescriptor> entry : methodDescriptors.entrySet()) {
            boolean nameEquals = request.getMethodName().equals(entry.getKey().getName());
            boolean aliasEquals = request.getMethodName().equals(entry.getValue().getAlias());
            if (nameEquals || aliasEquals) {
                Method method = serviceClass.getMethod(entry.getKey().getName(), entry.getKey().getParameterTypes());
                try {
                    if (duplicateMethodNamesSafe && !aliasEquals)
                        request.cacheInput();
                    InvocationContext context = newInvocationContext(request, method, entry.getValue());
                    if (time != null)
                        time.printDifference(context);
                    return context;
                } catch (SerializationException e) {
                    if (duplicateMethodNamesSafe && !aliasEquals)
                        LOG.debug("Can't convert given args to method '" + request.getMethodName() + "' argument types "
                                + Arrays.toString(method.getParameterTypes()));
                    else
                        throw e;
                }
            }
        }
        throw new ServiceException("Can't find method [" + request.getMethodName() + "] applicable for given args");
    }

    private InvocationContext newInvocationContext(ServiceRequest request, Method method, MethodDescriptor methodDescriptor)
            throws IOException, ServiceException {
        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        ValueDescriptor[] paramDescriptors = methodDescriptor != null ? methodDescriptor.getParameterDescriptors()
                : new ValueDescriptor[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            ValueDescriptor paramDescriptor = paramDescriptors[i];
            if (paramDescriptor != null && paramDescriptor.getTransfer() == ValueTransfer.REF)
                paramTypes[i] = ServiceLocator.class;
        }
        ValueDescriptor returnDescriptor = methodDescriptor != null ? methodDescriptor.getReturnDescriptor() : null;
        if (returnDescriptor != null && returnDescriptor.getTransfer() == ValueTransfer.REF)
            returnType = ServiceLocator.class;
        Serializer serializer = serializerFactory.getSerializer(paramTypes, returnType, methodDescriptor);
        Object[] params = serializer.deserializeParameters(request.getInputStream());
        // Find and replace references
        for (int i = 0; i < params.length; i++) {
            ValueDescriptor paramDescriptor = paramDescriptors[i];
            if (paramDescriptor != null && paramDescriptor.getTransfer() == ValueTransfer.REF) {
                ServiceLocator locator = (ServiceLocator) params[i];
                if (locator.isAbsolute())
                    throw new ServiceException("Can't get dynamic service by absolute service locator '" + locator + "'");
                try {
                    params[i] = serviceRegistry.lookup(locator.getServiceName());
                } catch (NamingException e) {
                    throw new ServiceException("Can't find service " + locator.getServiceName(), e);
                }
            }
        }
        return new InvocationContext(method, methodDescriptor, serializer, params);
    }

    private static Object[] getOutParameters(InvocationContext context) {
        Object[] outParams = new Object[context.params.length];
        boolean empty = true;
        if (context.methodDescriptor != null) {
            for (int i = 0; i < context.params.length; i++) {
                ValueDescriptor paramDescriptor = context.methodDescriptor.getParameterDescriptors()[i];
                ValueTransfer transfer = paramDescriptor != null ? paramDescriptor.getTransfer() : null;
                if (transfer == ValueTransfer.OUT || transfer == ValueTransfer.IN_OUT) {
                    outParams[i] = context.params[i];
                    empty = false;
                }
            }
        }
        return !empty ? outParams : null;
    }

    private static String read(InputStream in) throws IOException {
        try {
            byte[] buf = new byte[8192];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int n = in.read(buf); n >= 0; n = in.read(buf))
                out.write(buf, 0, n);
            return new String(out.toByteArray());
        } finally {
            in.close();
        }
    }

    private static class InvocationContext {

        final Method method;
        final MethodDescriptor methodDescriptor;
        final Serializer serializer;
        final Object params[];

        InvocationContext(Method method, MethodDescriptor methodDescriptor, Serializer serializer, Object[] params)
                throws IOException, ServiceException {
            this.method = method;
            this.methodDescriptor = methodDescriptor;
            this.serializer = serializer;
            this.params = params;
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append(method);
            if (methodDescriptor != null) {
                String mdStr = methodDescriptor.toString(1);
                if (mdStr.length() > 0)
                    str.append(", descriptor [\n").append(mdStr).append("\n]");
            }
            str.append(", serializer: ").append(serializer).append(", parameters: ").append(Arrays.toString(params));
            return str.toString();
        }

    }

}
