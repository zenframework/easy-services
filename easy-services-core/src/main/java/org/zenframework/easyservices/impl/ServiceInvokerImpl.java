package org.zenframework.easyservices.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.Name;
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
import org.zenframework.easyservices.ServiceRequestFilter;
import org.zenframework.easyservices.ServiceResponse;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.Configurable;
import org.zenframework.easyservices.descriptor.ClassDescriptor;
import org.zenframework.easyservices.descriptor.DefaultDescriptorFactory;
import org.zenframework.easyservices.descriptor.DescriptorFactory;
import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.MethodIdentifier;
import org.zenframework.easyservices.descriptor.ParamDescriptor;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.descriptor.ValueTransfer;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;
import org.zenframework.easyservices.util.cls.ClassInfo;
import org.zenframework.easyservices.util.debug.TimeChecker;
import org.zenframework.easyservices.util.debug.TimeStat;
import org.zenframework.easyservices.util.jndi.JNDIHelper;

public class ServiceInvokerImpl implements ServiceInvoker, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvokerImpl.class);

    private static final String PARAM_SERVICE_REGISTRY = "serviceRegistry";
    private static final String PARAM_REQUEST_FILTER_PREF = "requestFilter.";
    private static final String PARAM_SERIALIZER_FACTORY = "serializerFactory";
    private static final String PARAM_DESCRIPTOR_FACTORY = "descriptorFactory";
    private static final String PARAM_DEBUG = "debug";

    private final List<ServiceRequestFilter> requestFilters = new ArrayList<ServiceRequestFilter>();

    private Context serviceRegistry = JNDIHelper.getDefaultContext();
    private DescriptorFactory descriptorFactory = new DefaultDescriptorFactory();
    private SerializerFactory serializerFactory = Environment.getSerializerFactory();
    private boolean duplicateMethodNamesSafe = Environment.isDuplicateMethodNamesSafe();
    private boolean debug = Environment.isDebug();

    @Override
    public void invoke(ServiceRequest request, ServiceResponse response) throws IOException {

        ResponseObject responseObject = new ResponseObject();
        InvocationContext context = null;

        TimeStat timeStat = TimeStat.getThreadTimeStat();

        if (debug && LOG.isDebugEnabled()) {
            String requestStr;
            if (serializerFactory.isTextBased()) {
                request.cacheInput();
                requestStr = read(request.getInputStream(), 200);
            } else {
                requestStr = "[bin]";
            }
            LOG.debug("REQUEST " + request + ' ' + requestStr);
        }

        for (ServiceRequestFilter filter : requestFilters)
            filter.filterRequest(request);

        try {
            if (timeStat != null)
                timeStat.stage("lookupService");
            Object service = lookupService(request);
            if (timeStat != null)
                timeStat.stage("receiveRequest");
            if (request.getMethodName() == null) {
                if (timeStat != null)
                    timeStat.stage("invokeMethod");
                responseObject.setResult(getServiceInfo(request, service));
            } else {
                context = getInvocationContext(request, service.getClass());
                if (timeStat != null)
                    timeStat.stage("invokeMethod");
                for (ServiceRequestFilter filter : requestFilters)
                    filter.filterContext(request, context);
                responseObject.setResult(invokeMethod(request, context, service));
                if (request.isOutParametersMode())
                    responseObject.setParameters(getOutParameters(context));
                else if (hasOutParameters(context))
                    LOG.warn(request + ": Requested method has OUT parameters, but outParametersMode is off");
            }
            response.sendSuccess();
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            responseObject.setResult(e);
            response.sendError(e);
        } finally {
            request.getInputStream().close();
        }

        if (timeStat != null)
            timeStat.stage("sendResponse");
        OutputStream out = response.getOutputStream();
        try {
            if (context == null || context.getMethod().getReturnType() != void.class || responseObject.getResult() != null
                    || request.isOutParametersMode()) {
                Serializer serializer = context != null ? context.getSerializer()
                        : serializerFactory.getSerializer(null, Map.class, null, request.isOutParametersMode());
                serializer.serialize(request.isOutParametersMode() ? responseObject : responseObject.getResult(), out);
            }
        } finally {
            out.close();
            if (timeStat != null)
                timeStat.finish();
        }

    }

    @Override
    public void init(Config config) {
        Config serviceRegistryConfig = config.getSubConfig(PARAM_SERVICE_REGISTRY);
        if (!serviceRegistryConfig.isEmpty())
            serviceRegistry = JNDIHelper.getInitialContext(new Hashtable<String, Object>(serviceRegistryConfig.toMap()));
        requestFilters.addAll(config.<ServiceRequestFilter> getInstances(PARAM_REQUEST_FILTER_PREF));
        descriptorFactory = config.getInstance(PARAM_DESCRIPTOR_FACTORY, descriptorFactory);
        serializerFactory = config.getInstance(PARAM_SERIALIZER_FACTORY, serializerFactory);
        debug = config.getParam(PARAM_DEBUG, debug);
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(descriptorFactory, serializerFactory);
    }

    public void setServiceRegistry(Context serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public Context getServiceRegistry() {
        return serviceRegistry;
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public List<ServiceRequestFilter> getRequestFilters() {
        return requestFilters;
    }

    public void setRequestFilters(List<ServiceRequestFilter> requestFilters) {
        this.requestFilters.clear();
        this.requestFilters.addAll(requestFilters);
    }

    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }

    public void setDescriptorFactory(DescriptorFactory descriptorFactory) {
        this.descriptorFactory = descriptorFactory;
    }

    public DescriptorFactory getClassDescriptorFactory() {
        return descriptorFactory;
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

    protected Object invokeMethod(ServiceRequest request, InvocationContext context, Object service) throws Throwable {
        MethodDescriptor methodDescriptor = context.getMethodDescriptor();
        TimeChecker time = (debug || methodDescriptor.isDebug()) && LOG.isDebugEnabled() ? new TimeChecker(request + " INVOKE ", LOG) : null;
        try {
            Object result = context.getMethod().invoke(service, context.getParams());
            // If method must return reference, bind result to service register and set result to service locator
            ValueDescriptor returnDescriptor = methodDescriptor != null ? methodDescriptor.getReturnDescriptor() : null;
            if (returnDescriptor != null && returnDescriptor.getTransfer() == ValueTransfer.REF)
                result = ServiceLocator.relative(bindDynamicService(request, result));
            // If method must close dynamic service, remove it from repository
            if (methodDescriptor.isClose())
                request.getSession().getServiceRegistry().unbind(request.getServiceName());
            // Close dynamic services, passed as parameters
            for (int i = 0; i < context.getRawParams().length; i++) {
                ParamDescriptor paramDescriptor = methodDescriptor.getParameterDescriptor(i);
                if (paramDescriptor != null && paramDescriptor.getTransfer() == ValueTransfer.REF && paramDescriptor.isClose())
                    request.getSession().getServiceRegistry().unbind(((ServiceLocator) context.getRawParams()[i]).getServiceName());
            }
            if (time != null)
                time.printDifference(result);
            return result;
        } catch (InvocationTargetException e) {
            Throwable target = ((InvocationTargetException) e).getTargetException();
            if (LOG.isDebugEnabled())
                LOG.debug(request + (context != null ? '[' + context.toString() + ']' : "") + " invocation error" + context, target);
            throw target;
        } catch (Exception e) {
            LOG.info(request + (context != null ? '[' + context.toString() + ']' : "") + " service error", e);
            throw new ServiceException(request.toString() + ", invocation " + context + " error", e);
        }
    }

    protected Object lookupService(ServiceRequest request) {
        String serviceName = request.getServiceName();
        try {
            return DescriptorFactory.NAME.equals(serviceName) ? descriptorFactory : request.getSession().getServiceRegistry().lookupLink(serviceName);
        } catch (NamingException e) {
            throw new ServiceException(request.toString() + " error: service not found", e);
        }
    }

    protected String bindDynamicService(ServiceRequest request, Object service) throws NamingException {
        Context serviceRegistry = request.getSession().getServiceRegistry();
        Name name = request.getSession().getSessionContextName().add(service.getClass().getName() + '@' + System.identityHashCode(service));
        try {
            serviceRegistry.lookupLink(name);
        } catch (NamingException e) {
            serviceRegistry.bind(name, service);
        }
        return name.toString();
    }

    private InvocationContext getInvocationContext(ServiceRequest request, Class<?> serviceClass) throws IOException, ServiceException {
        ClassDescriptor classDescriptor = descriptorFactory.getClassDescriptor(serviceClass);
        if (serializerFactory.isTypeSafe() || request.getParameterTypes() != null) {
            // If serializer factory is type-safe, simply deserialize parameters & get method by name/alias and its parameter types
            TimeChecker time = debug && LOG.isDebugEnabled() ? new TimeChecker(request + " GET CONTEXT (type-safe)", LOG) : null;
            InvocationContext context = newInvocationContext(request, serviceClass, classDescriptor);
            if (time != null)
                time.printDifference(context);
            return context;
        } else {
            // Else first find appropriate method, then deserialize 
            for (Map.Entry<MethodIdentifier, MethodDescriptor> entry : classDescriptor.getMethodDescriptors().entrySet()) {
                boolean nameEquals = entry.getValue().getAlias() == null && request.getMethodName().equals(entry.getKey().getName());
                boolean aliasEquals = request.getMethodName().equals(entry.getValue().getAlias());
                if (nameEquals || aliasEquals) {
                    Method method = getMethod(serviceClass, entry.getKey().getName(), entry.getKey().getParameterTypes());
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

    private InvocationContext newInvocationContext(ServiceRequest request, Class<?> serviceClass, ClassDescriptor classDescriptor)
            throws IOException, ServiceException {
        Method method = null;
        MethodDescriptor methodDescriptor = null;
        Serializer serializer = null;
        Object[] rawParams, params;

        if (request.getParameterTypes() != null) {
            method = getMethod(serviceClass, request.getMethodName(), request.getParameterTypes());
            methodDescriptor = classDescriptor.getMethodDescriptor(new MethodIdentifier(method));
            serializer = serializerFactory.getSerializer(request.getParameterTypes(), method.getReturnType(), methodDescriptor,
                    request.isOutParametersMode());
        } else {
            serializer = serializerFactory.getTypeSafeSerializer();
        }

        rawParams = serializer.deserializeParameters(request.getInputStream());
        params = findAndReplaceRefs(rawParams);

        if (method != null) {
            return new InvocationContext(method, methodDescriptor, serializer, rawParams, params);
        } else {
            for (Map.Entry<MethodIdentifier, MethodDescriptor> e : classDescriptor.getMethodDescriptors().entrySet()) {
                Class<?>[] paramTypes = e.getKey().getParameterTypes();
                if (request.getMethodName().equals(e.getValue().getAlias())
                        || e.getValue().getAlias() == null && request.getMethodName().equals(e.getKey().getName())
                                && params.length == paramTypes.length && instancesOf(params, paramTypes)) {
                    method = getMethod(serviceClass, e.getKey().getName(), paramTypes);
                    methodDescriptor = e.getValue();
                    return new InvocationContext(method, methodDescriptor, serializer, rawParams, params);
                }
            }
        }

        throw new ServiceException("Can't find method [" + request.getMethodName() + "] applicable for given args");
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
        Serializer serializer = serializerFactory.getSerializer(paramTypes, returnType, methodDescriptor, request.isOutParametersMode());
        Object[] rawParams = serializer.deserializeParameters(request.getInputStream());
        return new InvocationContext(method, methodDescriptor, serializer, rawParams, findAndReplaceRefs(rawParams));
    }

    private Object[] findAndReplaceRefs(Object[] params) throws ServiceException {
        Object[] result = new Object[params.length];
        for (int i = 0; i < params.length; i++)
            result[i] = findAndReplaceRef(params[i]);
        return result;
    }

    private Object findAndReplaceRef(Object param) throws ServiceException {
        if (param instanceof ServiceLocator) {
            ServiceLocator locator = (ServiceLocator) param;
            if (locator.isAbsolute())
                throw new ServiceException("Can't get dynamic service by absolute service locator '" + locator + "'");
            try {
                param = serviceRegistry.lookupLink(locator.getServiceName());
            } catch (NamingException e) {
                throw new ServiceException("Can't find service " + locator.getServiceName(), e);
            }
        }
        return param;
    }

    private static boolean hasOutParameters(InvocationContext context) {
        for (ParamDescriptor paramDescriptor : context.getMethodDescriptor().getParameterDescriptors())
            if (paramDescriptor != null && paramDescriptor.getTransfer() == ValueTransfer.OUT)
                return true;
        return false;
    }

    private static Object[] getOutParameters(InvocationContext context) {
        Object[] outParams = new Object[context.getParams().length];
        boolean empty = true;
        if (context.getMethodDescriptor() != null) {
            for (int i = 0; i < context.getParams().length; i++) {
                ParamDescriptor paramDescriptor = context.getMethodDescriptor().getParameterDescriptors()[i];
                ValueTransfer transfer = paramDescriptor != null ? paramDescriptor.getTransfer() : null;
                if (transfer == ValueTransfer.OUT) {
                    outParams[i] = context.getParams()[i];
                    empty = false;
                }
            }
        }
        return !empty ? outParams : null;
    }

    private static String read(InputStream in, int max) throws IOException {
        try {
            byte[] buf = new byte[Math.min(8192, max + 1)];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int n = in.read(buf); n >= 0 && max >= 0; n = in.read(buf)) {
                out.write(buf, 0, n);
                max -= n;
            }
            String result = new String(out.toByteArray());
            if (max < 0)
                result += " ...";
            return result;
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

}
