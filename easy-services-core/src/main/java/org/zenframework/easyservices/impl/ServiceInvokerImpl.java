package org.zenframework.easyservices.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.bean.PrettyStringBuilder;
import org.zenframework.commons.bean.ServiceUtil;
import org.zenframework.commons.cls.ClassInfo;
import org.zenframework.commons.config.Config;
import org.zenframework.commons.config.Configurable;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.ErrorDescription;
import org.zenframework.easyservices.ErrorHandler;
import org.zenframework.easyservices.InvocationException;
import org.zenframework.easyservices.RequestContext;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.AnnotationServiceDescriptorFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.ServiceDescriptor;
import org.zenframework.easyservices.descriptor.ServiceDescriptorFactory;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.jndi.JNDIHelper;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceInvokerImpl implements ServiceInvoker, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvokerImpl.class);

    private static final String PARAM_SERVICE_REGISTRY = "serviceRegistry";
    private static final String PARAM_SERIALIZER_FACTORY = "serializerFactory";
    private static final String PARAM_REQUEST_MAPPER = "requestMapper";
    private static final String PARAM_SERVICE_DESCRIPTOR_FACTORY = "serviceDescriptorFactory";

    private static final SerializerFactory DEFAULT_SERIALIZER_FACTORY = ServiceUtil.getService(SerializerFactory.class);

    private Context serviceRegistry = JNDIHelper.getDefaultContext();
    private RequestMapper requestMapper = RequestMapperImpl.INSTANCE;
    private ServiceDescriptorFactory serviceDescriptorFactory = AnnotationServiceDescriptorFactory.INSTANE;
    private SerializerFactory serializerFactory = DEFAULT_SERIALIZER_FACTORY;

    @Override
    public String invoke(URI requestUri, String contextPath, ErrorHandler errorHandler) {
        Serializer serializer = serializerFactory.getSerializer();
        String result;
        try {
            RequestContext context = requestMapper.getRequestContext(requestUri, contextPath);
            Object service = serviceRegistry.lookup(context.getServiceName());
            ServiceDescriptor serviceDescriptor = serviceDescriptorFactory.getServiceDescriptor(service.getClass());
            result = context.getMethodName() == null ? getServiceInfo(service, serializer)
                    : invokeMethod(context, service, serializer, serviceDescriptor);
        } catch (Throwable e) {
            if (e instanceof ServiceException)
                LOG.warn(e.getMessage(), e);
            else
                LOG.error(e.getMessage(), e);
            if (e instanceof InvocationException)
                e = e.getCause();
            result = serializer.serialize(new ErrorDescription(e));
            errorHandler.onError(e);
        }
        return result;
    }

    @Override
    public void init(Config config) {
        Config serviceRegistryConfig = config.getSubConfig(PARAM_SERVICE_REGISTRY);
        if (!serviceRegistryConfig.isEmpty())
            serviceRegistry = JNDIHelper.newDefaultContext(serviceRegistryConfig);
        requestMapper = (RequestMapper) config.getInstance(PARAM_REQUEST_MAPPER, requestMapper);
        serviceDescriptorFactory = (ServiceDescriptorFactory) config.getInstance(PARAM_SERVICE_DESCRIPTOR_FACTORY, serviceDescriptorFactory);
        serializerFactory = (SerializerFactory) config.getInstance(PARAM_SERIALIZER_FACTORY, serializerFactory);
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(requestMapper, serviceDescriptorFactory, serializerFactory);
    }

    public void setServiceRegistry(Context serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setRequestMapper(RequestMapper requestMapper) {
        this.requestMapper = requestMapper;
    }

    public void setServiceDescriptorFactory(ServiceDescriptorFactory serviceDescriptorFactory) {
        this.serviceDescriptorFactory = serviceDescriptorFactory;
    }

    public Context getServiceRegistry() {
        return serviceRegistry;
    }

    public RequestMapper getRequestMapper() {
        return requestMapper;
    }

    public ServiceDescriptorFactory getServiceDescriptorFactory() {
        return serviceDescriptorFactory;
    }

    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }

    protected String getServiceInfo(Object service, Serializer serializer) {
        if (service == null)
            return null;
        ClassInfo classInfo = ClassInfo.getClassRef(service.getClass()).getClassInfo();
        Map<String, Object> serviceInfo = new HashMap<String, Object>();
        serviceInfo.put("className", classInfo.getName());
        serviceInfo.put("classes", ClassInfo.getClassInfos(service.getClass()));
        return serializer.serialize(serviceInfo);
    }

    protected String invokeMethod(RequestContext context, Object service, Serializer serializer, ServiceDescriptor serviceDescriptor)
            throws ServiceException, NamingException {

        Method method = null;
        MethodDescriptor methodDescriptor = null;
        Object args[] = null;
        Object result;

        // Try to find appropriate method
        for (Method m : service.getClass().getMethods()) {
            if (m.getName().equals(context.getMethodName())) {
                try {
                    Class<?>[] argTypes = m.getParameterTypes();
                    methodDescriptor = serviceDescriptor != null ? serviceDescriptor.getMethodDescriptor(m) : new MethodDescriptor(argTypes.length);
                    ValueDescriptor[] argDescriptors = methodDescriptor.getArgumentDescriptors();
                    for (int i = 0; i < argTypes.length; i++) {
                        ValueDescriptor argDescriptor = argDescriptors[i];
                        if (argDescriptor != null && argDescriptor.isReference())
                            argTypes[i] = ServiceLocator.class;
                    }
                    args = serializer.deserialize(context.getArguments(), argTypes, argDescriptors);
                    method = m;
                    break;
                } catch (SerializationException e) {
                    LOG.debug("Can't convert given args " + context.getArguments() + " to method '" + context.getMethodName() + "' argument types "
                            + m.getParameterTypes());
                }
            }
        }

        if (method == null)
            throw new ServiceException("Can't find method [" + context.getMethodName() + "] applicable for given args " + context.getArguments());

        TimeChecker time = null;
        if (LOG.isDebugEnabled())
            time = new TimeChecker(new StringBuilder(1024).append(context.getServiceName()).append('.').append(context.getMethodName())
                    .append(new PrettyStringBuilder().toString(args)).toString(), LOG);

        try {

            // Find and replace references
            ValueDescriptor[] argDescriptors = methodDescriptor.getArgumentDescriptors();
            for (int i = 0; i < args.length; i++) {
                ValueDescriptor argDescriptor = argDescriptors[i];
                if (argDescriptor != null && argDescriptor.isReference()) {
                    ServiceLocator locator = (ServiceLocator) args[i];
                    if (locator.isAbsolute())
                        throw new ServiceException("Can't get dynamic service by absolute service locator '" + locator + "'");
                    args[i] = serviceRegistry.lookup(locator.getServiceName());
                }
            }

            // Invoke method
            result = method.invoke(service, args);
            if (time != null)
                time.printDifference(result);

            // If method must return reference, bind result to service register and set result to service locator
            ValueDescriptor returnDescriptor = methodDescriptor.getReturnDescriptor();
            if (returnDescriptor != null && returnDescriptor.isReference()) {
                String name = getName(result);
                try {
                    serviceRegistry.lookup(name);
                } catch (NamingException e) {
                    serviceRegistry.bind(name, result);
                }
                result = ServiceLocator.relative(name);
            }

            return serializer.serialize(result);

        } catch (Throwable e) {
            if (e instanceof InvocationTargetException)
                e = ((InvocationTargetException) e).getTargetException();
            if (time != null)
                time.printDifference(e);
            throw new InvocationException(context, args, e);
        }

    }

    protected static String getName(Object obj) {
        return "/dynamic/" + System.identityHashCode(obj);
    }

}
