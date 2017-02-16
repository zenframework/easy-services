package org.zenframework.easyservices.impl;

import java.io.IOException;
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
import org.zenframework.commons.bean.PrettyStringBuilder;
import org.zenframework.commons.cls.ClassInfo;
import org.zenframework.commons.config.Config;
import org.zenframework.commons.config.Configurable;
import org.zenframework.commons.debug.TimeChecker;
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
        try {
            Object service = lookupSystemService(request.getServiceName());
            if (service == null)
                service = serviceRegistry.lookup(request.getServiceName());
            if (service == null)
                throw new ServiceException("Service " + request.getServiceName() + " not found");
            String methodName = request.getMethodName();
            if (methodName == null) {
                context = new InvocationContext(null, null, serializerFactory.getSerializer(null, Map.class, null), null);
                getServiceInfo(service, responseObject);
            } else {
                context = getInvocationContext(request, service.getClass());
                invokeMethod(request, service, context, responseObject);
            }
            responseObject.setSuccess(true);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
                LOG.warn(e.getMessage(), e);
            } else {
                LOG.error(e.getMessage(), e);
            }
            responseObject.setResult(e);
            responseObject.setSuccess(false);
            response.sendError(e);
        }
        OutputStream out = response.getOutputStream();
        try {
            Serializer serializer = context != null ? context.serializer : serializerFactory.getSerializer(null, Map.class, null);
            serializer.serialize(responseObject, out);
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

    protected void getServiceInfo(Object service, ResponseObject responseObject) throws IOException {
        ClassInfo classInfo = ClassInfo.getClassRef(service.getClass()).getClassInfo();
        Map<String, Object> serviceInfo = new HashMap<String, Object>();
        serviceInfo.put("className", classInfo.getName());
        serviceInfo.put("classes", ClassInfo.getClassInfos(service.getClass()));
        responseObject.setResult(serviceInfo);
    }

    protected void invokeMethod(ServiceRequest request, Object service, InvocationContext context, ResponseObject responseObject) throws Throwable {

        String serviceName = request.getServiceName();
        String methodName = request.getMethodName();
        Object result;

        TimeChecker time = LOG.isDebugEnabled() ? new TimeChecker("FIND BY ALIAS " + methodName, LOG) : null;

        time = LOG.isDebugEnabled() ? new TimeChecker("FIND BY ARGS " + methodName, LOG) : null;
        if (time != null)
            time.printDifference(context.method);

        time = (debug || context.methodDescriptor.getDebug()) && LOG.isDebugEnabled() ? new TimeChecker(new StringBuilder(1024).append(serviceName)
                .append('.').append(methodName).append(new PrettyStringBuilder().toString(context.args)).toString(), LOG) : null;

        // Invoke method
        try {
            result = context.method.invoke(service, context.args);
            if (time != null)
                time.printDifference(result);
        } catch (Throwable e) {
            if (time != null)
                time.printDifference(e);
            throw e;
        }

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

        Object[] outParams = new Object[context.args.length];
        boolean empty = true;
        if (context.methodDescriptor != null) {
            for (int i = 0; i < context.args.length; i++) {
                ValueDescriptor paramDescriptor = context.methodDescriptor.getParameterDescriptors()[i];
                ValueTransfer transfer = paramDescriptor != null ? paramDescriptor.getTransfer() : null;
                if (transfer == ValueTransfer.OUT || transfer == ValueTransfer.IN_OUT) {
                    outParams[i] = context.args[i];
                    empty = false;
                }
            }
        }
        if (!empty)
            responseObject.setParameters(outParams);

        responseObject.setResult(result);

    }

    protected Object lookupSystemService(String name) {
        if (DescriptorFactory.NAME.equals(name))
            return descriptorFactory;
        return null;
    }

    protected static String getName(Object obj) {
        return "/dynamic/" + obj.getClass().getCanonicalName() + '@' + System.identityHashCode(obj);
    }

    private InvocationContext getInvocationContext(ServiceRequest request, Class<?> serviceClass)
            throws IOException, ServiceException, NoSuchMethodException, SecurityException {
        Map<MethodIdentifier, MethodDescriptor> methodDescriptors = descriptorFactory.getMethodDescriptors(serviceClass);
        for (Map.Entry<MethodIdentifier, MethodDescriptor> entry : methodDescriptors.entrySet()) {
            boolean nameEquals = request.getMethodName().equals(entry.getKey().getName());
            boolean aliasEquals = request.getMethodName().equals(entry.getValue().getAlias());
            if (nameEquals || aliasEquals) {
                Method method = serviceClass.getMethod(entry.getKey().getName(), entry.getKey().getParameterTypes());
                try {
                    return newInvocationContext(request, method, entry.getValue());
                } catch (SerializationException e) {
                    if (duplicateMethodNamesSafe && nameEquals)
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
        Object[] args = serializer.deserializeParameters(request.getInputStream());
        // Find and replace references
        for (int i = 0; i < args.length; i++) {
            ValueDescriptor argDescriptor = paramDescriptors[i];
            if (argDescriptor != null && argDescriptor.getTransfer() == ValueTransfer.REF) {
                ServiceLocator locator = (ServiceLocator) args[i];
                if (locator.isAbsolute())
                    throw new ServiceException("Can't get dynamic service by absolute service locator '" + locator + "'");
                try {
                    args[i] = serviceRegistry.lookup(locator.getServiceName());
                } catch (NamingException e) {
                    throw new ServiceException("Can't find service " + locator.getServiceName(), e);
                }
            }
        }
        return new InvocationContext(method, methodDescriptor, serializer, args);
    }

    private static class InvocationContext {

        final Method method;
        final MethodDescriptor methodDescriptor;
        final Serializer serializer;
        final Object args[];

        InvocationContext(Method method, MethodDescriptor methodDescriptor, Serializer serializer, Object[] args)
                throws IOException, ServiceException {
            this.method = method;
            this.methodDescriptor = methodDescriptor;
            this.serializer = serializer;
            this.args = args;
        }

    }

}
