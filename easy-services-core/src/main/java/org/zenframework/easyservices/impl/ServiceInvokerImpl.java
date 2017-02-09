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
import org.zenframework.commons.bean.ServiceUtil;
import org.zenframework.commons.cls.ClassInfo;
import org.zenframework.commons.config.Config;
import org.zenframework.commons.config.Configurable;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.ServiceLocator;
import org.zenframework.easyservices.ServiceRequest;
import org.zenframework.easyservices.ServiceResponse;
import org.zenframework.easyservices.descriptor.AnnotationClassDescriptorFactory;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.ClassDescriptorFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.jndi.JNDIHelper;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceInvokerImpl implements ServiceInvoker, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvokerImpl.class);

    private static final String PARAM_SERVICE_REGISTRY = "serviceRegistry";
    private static final String PARAM_SERIALIZER_FACTORY = "serializerFactory";
    private static final String PARAM_CLASS_DESCRIPTOR_FACTORY = "classDescriptorFactory";

    private static final SerializerFactory DEFAULT_SERIALIZER_FACTORY = ServiceUtil.getService(SerializerFactory.class);

    private Context serviceRegistry = JNDIHelper.getDefaultContext();
    private ClassDescriptorFactory classDescriptorFactory = AnnotationClassDescriptorFactory.INSTANSE;
    private SerializerFactory serializerFactory = DEFAULT_SERIALIZER_FACTORY;

    @Override
    public void invoke(ServiceRequest request, ServiceResponse response) throws IOException {
        Serializer serializer = serializerFactory.getSerializer();
        try {
            Object service = lookupSystemService(request.getServiceName());
            if (service == null)
                service = serviceRegistry.lookup(request.getServiceName());
            if (service == null)
                throw new ServiceException("Service " + request.getServiceName() + " not found");
            OutputStream out = response.getOutputStream();
            try {
                if (request.getMethodName() == null)
                    invokeServiceInfo(service, serializer, out);
                else
                    invokeMethod(request, service, serializer, out);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
                LOG.warn(e.getMessage(), e);
            } else {
                LOG.error(e.getMessage(), e);
            }
            serializer.serialize(e, response.getErrorStream(e));
        }
    }

    @Override
    public void init(Config config) {
        Config serviceRegistryConfig = config.getSubConfig(PARAM_SERVICE_REGISTRY);
        if (!serviceRegistryConfig.isEmpty())
            serviceRegistry = JNDIHelper.newDefaultContext(serviceRegistryConfig);
        classDescriptorFactory = (ClassDescriptorFactory) config.getInstance(PARAM_CLASS_DESCRIPTOR_FACTORY, classDescriptorFactory);
        serializerFactory = (SerializerFactory) config.getInstance(PARAM_SERIALIZER_FACTORY, serializerFactory);
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(classDescriptorFactory, serializerFactory);
    }

    public void setServiceRegistry(Context serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public void setClassDescriptorFactory(ClassDescriptorFactory classDescriptorFactory) {
        this.classDescriptorFactory = classDescriptorFactory;
    }

    public Context getServiceRegistry() {
        return serviceRegistry;
    }

    public ClassDescriptorFactory getClassDescriptorFactory() {
        return classDescriptorFactory;
    }

    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }

    protected void invokeServiceInfo(Object service, Serializer serializer, OutputStream out) throws IOException {
        ClassInfo classInfo = ClassInfo.getClassRef(service.getClass()).getClassInfo();
        Map<String, Object> serviceInfo = new HashMap<String, Object>();
        serviceInfo.put("className", classInfo.getName());
        serviceInfo.put("classes", ClassInfo.getClassInfos(service.getClass()));
        serializer.serialize(serviceInfo, out);
    }

    protected void invokeMethod(ServiceRequest request, Object service, Serializer serializer, OutputStream out) throws Throwable {

        ClassDescriptor classDescriptor = classDescriptorFactory.getClassDescriptor(service.getClass());

        Method method = null;
        MethodDescriptor methodDescriptor = null;
        ValueDescriptor[] paramDescriptors = null;
        Object args[] = null;
        Object result;

        // Try to find appropriate method
        for (Method m : service.getClass().getMethods()) {
            if (m.getName().equals(request.getMethodName())) {
                try {
                    Class<?>[] argTypes = m.getParameterTypes();
                    methodDescriptor = classDescriptor != null ? classDescriptor.getMethodDescriptor(m) : null;
                    paramDescriptors = methodDescriptor != null ? methodDescriptor.getParameterDescriptors() : new ValueDescriptor[argTypes.length];
                    for (int i = 0; i < argTypes.length; i++) {
                        ValueDescriptor argDescriptor = paramDescriptors[i];
                        if (argDescriptor != null && argDescriptor.isReference())
                            argTypes[i] = ServiceLocator.class;
                    }
                    args = serializer.deserialize(request.getArguments(), argTypes, paramDescriptors);
                    method = m;
                    break;
                } catch (SerializationException e) {
                    LOG.debug("Can't convert given args " + request.getArguments() + " to method '" + request.getMethodName() + "' argument types "
                            + Arrays.toString(m.getParameterTypes()));
                }
            }
        }

        if (method == null)
            throw new ServiceException("Can't find method [" + request.getMethodName() + "] applicable for given args " + request.getArguments());

        TimeChecker time = null;
        boolean debug = methodDescriptor != null ? methodDescriptor.isDebug() : classDescriptor != null ? classDescriptor.isDebug() : false;
        if (debug && LOG.isDebugEnabled())
            time = new TimeChecker(new StringBuilder(1024).append(request.getServiceName()).append('.').append(request.getMethodName())
                    .append(new PrettyStringBuilder().toString(args)).toString(), LOG);

        // Find and replace references
        for (int i = 0; i < args.length; i++) {
            ValueDescriptor argDescriptor = paramDescriptors[i];
            if (argDescriptor != null && argDescriptor.isReference()) {
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

        // Invoke method
        try {
            result = method.invoke(service, args);
            if (time != null)
                time.printDifference(result);
        } catch (Throwable e) {
            if (time != null)
                time.printDifference(e);
            throw e;
        }

        // If method must return reference, bind result to service register and set result to service locator
        ValueDescriptor returnDescriptor = methodDescriptor != null ? methodDescriptor.getReturnDescriptor() : null;
        if (returnDescriptor != null && returnDescriptor.isReference()) {
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

        serializer.serialize(result, out);

    }

    protected Object lookupSystemService(String name) {
        if (ClassDescriptorFactory.NAME.equals(name))
            return classDescriptorFactory;
        return null;
    }

    protected static String getName(Object obj) {
        return "/dynamic/" + obj.getClass().getCanonicalName() + '@' + System.identityHashCode(obj);
    }

}
