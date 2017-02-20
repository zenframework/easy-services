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

import org.apache.commons.lang.ClassUtils;
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

        if (debug && LOG.isDebugEnabled()) {
            String requestStr;
            if (serializerFactory.isTextBased()) {
                request.cacheInput();
                requestStr = read(request.getInputStream());
            } else {
                requestStr = "[bin]";
            }
            LOG.debug("REQUEST " + request + ' ' + requestStr);
        }

        try {
            Object service = lookupService(request);
            request.setServiceClass(service.getClass());
            if (request.getMethodName() == null) {
                context = new InvocationContext(null, null, serializerFactory.getSerializer(null, Map.class, null), null);
                responseObject.setResult(getServiceInfo(request, service));
            } else {
                context = getInvocationContext(request);
                responseObject.setResult(invokeMethod(request, service, context));
                if (request.isOutParametersMode())
                    responseObject.setParameters(getOutParameters(context));
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
                LOG.info(request + (context != null ? '[' + context.toString() + ']' : "") + " invocation error" + context, e);
            } else {
                LOG.error(request + (context != null ? '[' + context.toString() + ']' : "") + " service error", e);
            }
            responseObject.setResult(e);
            responseObject.setSuccess(false);
            response.sendError(e);
        }
        OutputStream out = response.getOutputStream();
        try {
            Serializer serializer = context != null ? context.serializer : serializerFactory.getSerializer(null, Map.class, null);
            serializer.serialize(request.isOutParametersMode() ? responseObject : responseObject.getResult(), out);
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
        debug = config.getParam(PARAM_DEBUG, debug);
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
        TimeChecker time = (debug || context.methodDescriptor != null && context.methodDescriptor.getDebug()) && LOG.isDebugEnabled()
                ? new TimeChecker(request + " INVOKE ", LOG) : null;
        Object result = context.method.invoke(service, context.params);
        // If method must return reference, bind result to service register and set result to service locator
        ValueDescriptor returnDescriptor = context.methodDescriptor != null ? context.methodDescriptor.getReturnDescriptor() : null;
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

    private InvocationContext getInvocationContext(ServiceRequest request) throws IOException, ServiceException {
        if (serializerFactory.isTypeSafe()) {
            // If serializer factory is type-safe, simply deserialize parameters & get method by name/alias and its parameter types
            TimeChecker time = debug && LOG.isDebugEnabled() ? new TimeChecker(request + " GET CONTEXT (type-safe)", LOG) : null;
            InvocationContext context = newInvocationContext(request);
            if (time != null)
                time.printDifference(context);
            return context;
        } else {
            // Else first find appropriate method, then deserialize 
            Map<MethodIdentifier, MethodDescriptor> methodDescriptors = descriptorFactory.getMethodDescriptors(request.getServiceClass());
            for (Map.Entry<MethodIdentifier, MethodDescriptor> entry : methodDescriptors.entrySet()) {
                boolean nameEquals = entry.getValue().getAlias() == null && request.getMethodName().equals(entry.getKey().getName());
                boolean aliasEquals = request.getMethodName().equals(entry.getValue().getAlias());
                if (nameEquals || aliasEquals) {
                    Method method = getMethod(request.getServiceClass(), entry.getKey().getName(), entry.getKey().getParameterTypes());
                    try {
                        if (duplicateMethodNamesSafe && !aliasEquals)
                            request.cacheInput();
                        TimeChecker time = debug && LOG.isDebugEnabled()
                                ? new TimeChecker(
                                        request + " GET CONTEXT (non-type-safe)\n\tmethod:     " + method + "\n\tmethodId:   " + entry.getKey()
                                                + (!entry.getValue().isEmpty() ? "\n\tdescriptor: " + entry.getValue().toString() : "") + '\n',
                                        LOG)
                                : null;
                        InvocationContext context = newInvocationContext(request, method, entry.getValue());
                        if (time != null)
                            time.printDifference(context);
                        return context;
                    } catch (SerializationException e) {
                        if (duplicateMethodNamesSafe && !aliasEquals)
                            LOG.debug(request + ": can't convert given args to method argument types " + Arrays.toString(method.getParameterTypes()));
                        else
                            throw e;
                    }
                }
            }
        }
        throw new ServiceException("Can't find method [" + request.getMethodName() + "] applicable for given args");
    }

    private InvocationContext newInvocationContext(ServiceRequest request) throws IOException, ServiceException {
        Serializer serializer = serializerFactory.getTypeSafeSerializer();
        Object[] params = serializer.deserializeParameters(request.getInputStream());
        Map.Entry<MethodIdentifier, MethodDescriptor> entry = descriptorFactory.getMethodEntry(request.getServiceClass(), request.getMethodName());
        MethodDescriptor methodDescriptor = entry != null ? entry.getValue() : null;
        Method method = null;
        findAndReplaceRefs(params);
        if (entry != null) {
            method = getMethod(request.getServiceClass(), entry.getKey().getName(), entry.getKey().getParameterTypes());
        } else {
            Map<MethodIdentifier, MethodDescriptor> methodDescriptors = descriptorFactory.getMethodDescriptors(request.getServiceClass());
            for (Map.Entry<MethodIdentifier, MethodDescriptor> e : methodDescriptors.entrySet()) {
                boolean nameEquals = e.getValue().getAlias() == null && request.getMethodName().equals(e.getKey().getName());
                boolean aliasEquals = request.getMethodName().equals(e.getValue().getAlias());
                Class<?>[] paramTypes = e.getKey().getParameterTypes();
                if ((nameEquals || aliasEquals) && params.length == paramTypes.length && instancesOf(params, e.getKey().getParameterTypes())) {
                    method = getMethod(request.getServiceClass(), request.getMethodName(), paramTypes);
                    methodDescriptor = e.getValue();
                }
            }
        }
        if (method == null)
            throw new ServiceException("Can't find method [" + request.getMethodName() + "] applicable for given args");
        return new InvocationContext(method, methodDescriptor, serializer, params);
    }

    private InvocationContext newInvocationContext(ServiceRequest request, Method method, MethodDescriptor methodDescriptor)
            throws IOException, ServiceException {
        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        if (methodDescriptor != null) {
            for (int i = 0; i < paramTypes.length; i++) {
                ValueDescriptor paramDescriptor = methodDescriptor.getParameterDescriptor(i);
                if (paramDescriptor != null && paramDescriptor.getTransfer() == ValueTransfer.REF)
                    paramTypes[i] = ServiceLocator.class;
            }
        }
        ValueDescriptor returnDescriptor = methodDescriptor != null ? methodDescriptor.getReturnDescriptor() : null;
        if (returnDescriptor != null && returnDescriptor.getTransfer() == ValueTransfer.REF)
            returnType = ServiceLocator.class;
        Serializer serializer = serializerFactory.getSerializer(paramTypes, returnType, methodDescriptor);
        Object[] params = serializer.deserializeParameters(request.getInputStream());
        findAndReplaceRefs(params);
        return new InvocationContext(method, methodDescriptor, serializer, params);
    }

    private void findAndReplaceRefs(Object[] params) throws ServiceException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof ServiceLocator) {
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

    private static boolean instancesOf(Object[] params, Class<?>[] classes) {
        for (int i = 0; i < params.length; i++)
            if (!classes[i].isInstance(params[i]) && ClassUtils.wrapperToPrimitive(params[i].getClass()) != classes[i])
                return false;
        return true;
    }

    private static Method getMethod(Class<?> cls, String name, Class<?>[] paramTypes) throws ServiceException {
        try {
            return cls.getMethod(name, paramTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ServiceException(e);
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
                String mdStr = methodDescriptor.toString();
                if (mdStr.length() > 0)
                    str.append(", descriptor ").append(mdStr);
            }
            str.append(", serializer: ").append(serializer).append(", parameters: ").append(Arrays.toString(params));
            return str.toString();
        }

    }

}
