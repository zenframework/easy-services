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
import org.zenframework.commons.bean.PrettyStringBuilder;
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
import org.zenframework.easyservices.descriptor.MethodIdentifier;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.jndi.JNDIHelper;
import org.zenframework.easyservices.serialize.Serialization;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class ServiceInvokerImpl implements ServiceInvoker, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvokerImpl.class);

    private static final String PARAM_SERVICE_REGISTRY = "serviceRegistry";
    private static final String PARAM_SERIALIZER_FACTORY = "serializerFactory";
    private static final String PARAM_CLASS_DESCRIPTOR_FACTORY = "classDescriptorFactory";
    private static final String PARAM_DEBUG = "debug";

    private Context serviceRegistry = JNDIHelper.getDefaultContext();
    private ClassDescriptorFactory classDescriptorFactory = AnnotationClassDescriptorFactory.INSTANSE;
    private SerializerFactory serializerFactory = Serialization.getDefaultFactory();
    private boolean debug;

    @Override
    public void invoke(ServiceRequest request, ServiceResponse response) throws IOException {
        Serializer serializer = serializerFactory.getSerializer();
        Object result = null;
        try {
            Object service = lookupSystemService(request.getServiceName());
            if (service == null)
                service = serviceRegistry.lookup(request.getServiceName());
            if (service == null)
                throw new ServiceException("Service " + request.getServiceName() + " not found");
            result = request.getMethodName() == null ? getServiceInfo(service) : invokeMethod(request, service, serializer);
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getTargetException();
                LOG.warn(e.getMessage(), e);
            } else {
                LOG.error(e.getMessage(), e);
            }
            response.sendError(e);
            result = e;
        }
        OutputStream out = response.getOutputStream();
        try {
            serializer.serialize(result, out);
        } finally {
            out.close();
        }
    }

    @Override
    public void init(Config config) {
        Config serviceRegistryConfig = config.getSubConfig(PARAM_SERVICE_REGISTRY);
        if (!serviceRegistryConfig.isEmpty())
            serviceRegistry = JNDIHelper.newDefaultContext(serviceRegistryConfig);
        classDescriptorFactory = (ClassDescriptorFactory) config.getInstance(PARAM_CLASS_DESCRIPTOR_FACTORY, classDescriptorFactory);
        serializerFactory = (SerializerFactory) config.getInstance(PARAM_SERIALIZER_FACTORY, serializerFactory);
        debug = config.getParam(PARAM_DEBUG, false);
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

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    protected Map<String, Object> getServiceInfo(Object service) throws IOException {
        ClassInfo classInfo = ClassInfo.getClassRef(service.getClass()).getClassInfo();
        Map<String, Object> serviceInfo = new HashMap<String, Object>();
        serviceInfo.put("className", classInfo.getName());
        serviceInfo.put("classes", ClassInfo.getClassInfos(service.getClass()));
        return serviceInfo;
    }

    protected Object invokeMethod(ServiceRequest request, Object service, Serializer serializer) throws Throwable {

        String methodName = request.getMethodName();
        ClassDescriptor classDescriptor = classDescriptorFactory.getClassDescriptor(service.getClass());

        Method method = null;
        MethodDescriptor methodDescriptor = null;
        ValueDescriptor[] paramDescriptors = null;
        Object args[] = null;
        Object result;

        // Try to find method by alias
        TimeChecker time = LOG.isDebugEnabled() ? new TimeChecker("FIND BY ALIAS " + methodName, LOG) : null;
        Map.Entry<MethodIdentifier, MethodDescriptor> methodEntry = classDescriptor != null ? classDescriptor.findMethodEntry(methodName) : null;
        if (methodEntry != null) {
            method = service.getClass().getMethod(methodEntry.getKey().getName(), methodEntry.getKey().getParameterTypes());
            methodDescriptor = methodEntry.getValue();
            paramDescriptors = methodDescriptor.getParameterDescriptors();
            if (request.isArgsByParameter())
                args = serializer.deserialize(request.getArguments(), methodEntry.getKey().getParameterTypes(), paramDescriptors);
            else
                args = serializer.deserialize(request.getInputStream(), methodEntry.getKey().getParameterTypes(), paramDescriptors);
            if (time != null)
                time.printDifference(method);
        } else {
            // Try to find method by args
            byte[] argsStr = request.isArgsByParameter() ? request.getArguments() : read(request.getInputStream());
            if (time != null)
                time.printDifference(null);
            time = LOG.isDebugEnabled() ? new TimeChecker("FIND BY ARGS " + methodName, LOG) : null;
            for (Method m : service.getClass().getMethods()) {
                if (m.getName().equals(request.getMethodName())) {
                    try {
                        Class<?>[] argTypes = m.getParameterTypes();
                        methodDescriptor = classDescriptor != null ? classDescriptor.getMethodDescriptor(m) : null;
                        paramDescriptors = methodDescriptor != null ? methodDescriptor.getParameterDescriptors()
                                : new ValueDescriptor[argTypes.length];
                        for (int i = 0; i < argTypes.length; i++) {
                            ValueDescriptor argDescriptor = paramDescriptors[i];
                            if (argDescriptor != null && argDescriptor.isReference())
                                argTypes[i] = ServiceLocator.class;
                        }
                        args = serializer.deserialize(argsStr, argTypes, paramDescriptors);
                        method = m;
                        break;
                    } catch (SerializationException e) {
                        LOG.debug("Can't convert given args to method '" + request.getMethodName() + "' argument types "
                                + Arrays.toString(m.getParameterTypes()));
                    }
                }
            }
            if (time != null)
                time.printDifference(method);
        }

        if (method == null)
            throw new ServiceException("Can't find method [" + request.getMethodName() + "] applicable for given args");

        boolean methodDebug = methodDescriptor != null ? methodDescriptor.isDebug() : classDescriptor != null ? classDescriptor.isDebug() : false;
        time = (debug || methodDebug) && LOG.isDebugEnabled() ? new TimeChecker(new StringBuilder(1024).append(request.getServiceName()).append('.')
                .append(request.getMethodName()).append(new PrettyStringBuilder().toString(args)).toString(), LOG) : null;

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

        return result;

    }

    protected Object lookupSystemService(String name) {
        if (ClassDescriptorFactory.NAME.equals(name))
            return classDescriptorFactory;
        return null;
    }

    protected static String getName(Object obj) {
        return "/dynamic/" + obj.getClass().getCanonicalName() + '@' + System.identityHashCode(obj);
    }

    private static byte[] read(InputStream in) throws IOException {
        try {
            byte[] buf = new byte[8192];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int n = in.read(buf); n >= 0; n = in.read(buf))
                out.write(buf, 0, n);
            return out.toByteArray();
        } finally {
            in.close();
        }
    }

}
