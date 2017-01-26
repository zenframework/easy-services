package org.zenframework.easyservices.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.bean.PrettyStringBuilder;
import org.zenframework.commons.bean.ServiceUtil;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.ClassInfo;
import org.zenframework.easyservices.ErrorDescription;
import org.zenframework.easyservices.ErrorHandler;
import org.zenframework.easyservices.InvocationException;
import org.zenframework.easyservices.RequestContext;
import org.zenframework.easyservices.RequestMapper;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.descriptor.AnnotationServiceDescriptorFactory;
import org.zenframework.easyservices.descriptor.ServiceDescriptor;
import org.zenframework.easyservices.descriptor.ServiceDescriptorFactory;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.jndi.JNDIHelper;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceInvokerImpl implements ServiceInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvokerImpl.class);

    private static final RequestMapper DEFAULT_REQUEST_MAPPER = new RequestMapperImpl();
    private static final ServiceDescriptorFactory DEFAULT_SERVICE_DESCRIPTOR_FACTORY = new AnnotationServiceDescriptorFactory();
    private static final SerializerFactory DEFAULT_SERIALIZER_FACTORY = ServiceUtil.getService(SerializerFactory.class);
    private static final String DEFAULT_SERVICE_INFO_ALIAS = "serviceInfo";

    private Context serviceRegistry = JNDIHelper.getDefaultContext();
    private RequestMapper requestMapper = DEFAULT_REQUEST_MAPPER;
    private ServiceDescriptorFactory serviceDescriptorFactory = DEFAULT_SERVICE_DESCRIPTOR_FACTORY;
    private SerializerFactory serializerFactory = DEFAULT_SERIALIZER_FACTORY;
    private String serviceInfoAlias = DEFAULT_SERVICE_INFO_ALIAS;

    @Override
    public String invoke(URI requestUri, String contextPath, ErrorHandler errorHandler) {
        Serializer serializer = serializerFactory.getSerializer();
        String result;
        try {
            RequestContext context = requestMapper.getRequestContext(requestUri, contextPath);
            Object service = serviceRegistry.lookup(context.getServiceName());
            ServiceDescriptor serviceDescriptor = serviceDescriptorFactory.getServiceDescriptor(service.getClass());
            result = context.getMethodName().equals(serviceInfoAlias) ? getServiceInfo(service, serializer)
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

    public void setServiceInfoAlias(String serviceInfoAlias) {
        this.serviceInfoAlias = serviceInfoAlias;
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

    public String getServiceInfoAlias() {
        return serviceInfoAlias;
    }

    private String getServiceInfo(Object service, Serializer serializer) {
        if (service == null)
            return null;
        Map<String, Object> serviceInfo = new HashMap<String, Object>();
        Map<String, Object> methodsInfo = new HashMap<String, Object>();
        Collection<Class<?>> structured = new LinkedList<Class<?>>();
        serviceInfo.put(ClassInfo.METHODS, methodsInfo);
        for (Method method : ClassInfo.getMethods(service)) {
            Map<String, Object> methodInfo = new HashMap<String, Object>();
            List<Object> argsInfo = new LinkedList<Object>();
            methodsInfo.put(method.getName(), methodInfo);
            methodInfo.put(ClassInfo.ARGUMENTS, argsInfo);
            for (Class<?> argType : method.getParameterTypes())
                argsInfo.add(getClassInfo(argType, structured, 0));
            methodInfo.put(ClassInfo.RETURNS, getClassInfo(method.getReturnType(), structured, 0));
        }
        return serializer.serialize(serviceInfo);
    }

    private String invokeMethod(RequestContext context, Object service, Serializer serializer, ServiceDescriptor serviceDescriptor)
            throws ServiceException, NamingException {

        Method method = null;
        ValueDescriptor[] argDescriptors = null;
        Object args[] = null;
        Object result;

        // Try to find appropriate method
        for (Method m : service.getClass().getMethods()) {
            if (m.getName().equals(context.getMethodName())) {
                try {
                    argDescriptors = ServiceDescriptor.getArgumentDescriptors(serviceDescriptor, m);
                    Class<?>[] argTypes = m.getParameterTypes();
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
            for (int i = 0; i < args.length; i++) {
                ValueDescriptor argDescriptor = argDescriptors[i];
                if (argDescriptor != null && argDescriptor.isReference()) {
                    ServiceLocator locator = (ServiceLocator) args[i];
                    if (locator.isAbsolute())
                        throw new ServiceException("Can't get dynamic service by absolute service locator '" + locator + "'");
                    args[i] = serviceRegistry.lookup(locator.getServiceName());
                }
            }
            result = method.invoke(service, args);
            if (time != null)
                time.printDifference(result);
            ValueDescriptor returnDescriptor = ServiceDescriptor.getReturnDescriptor(serviceDescriptor, method);
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

    private static Object getClassInfo(Class<?> clazz, Collection<Class<?>> structured, int arrayDeep) {
        if (clazz.isArray())
            return getClassInfo(clazz.getComponentType(), structured, arrayDeep + 1);
        String mapped = ClassInfo.mapSimpleClass(clazz);
        if (mapped != null) {
            return ClassInfo.toArray(arrayDeep, mapped);
        } else if (clazz.isEnum()) {
            Map<String, Object> classInfo = new HashMap<String, Object>();
            List<String> enumConsts = new LinkedList<String>();
            classInfo.put(ClassInfo.TYPE, ClassInfo.toArray(arrayDeep, ClassInfo.ENUM, clazz.getCanonicalName()));
            classInfo.put(ClassInfo.ENUM, enumConsts);
            for (Object o : clazz.getEnumConstants()) {
                enumConsts.add(((Enum<?>) o).name());
            }
            return classInfo;
        } else {
            if (structured.contains(clazz)) {
                return ClassInfo.toArray(arrayDeep, ClassInfo.CLASS, clazz.getCanonicalName());
            } else {
                structured.add(clazz);
                Map<String, Object> classInfo = new HashMap<String, Object>();
                Map<String, Object> fieldsInfo = new HashMap<String, Object>();
                classInfo.put(ClassInfo.TYPE, ClassInfo.toArray(arrayDeep, ClassInfo.CLASS, clazz.getCanonicalName()));
                classInfo.put(ClassInfo.CLASS, fieldsInfo);
                for (Entry<String, Class<?>> field : ClassInfo.getFields(clazz).entrySet()) {
                    fieldsInfo.put(field.getKey(), getClassInfo(field.getValue(), structured, 0));
                }
                return classInfo;
            }
        }
    }

    private static String getName(Object obj) {
        return "/dynamic/" + System.identityHashCode(obj);
    }

}
