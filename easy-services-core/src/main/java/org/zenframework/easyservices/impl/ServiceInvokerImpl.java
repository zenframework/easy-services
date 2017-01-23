package org.zenframework.easyservices.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.commons.bean.PrettyStringBuilder;
import org.zenframework.commons.debug.TimeChecker;
import org.zenframework.easyservices.ClassInfo;
import org.zenframework.easyservices.InvocationException;
import org.zenframework.easyservices.RequestContext;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;

public class ServiceInvokerImpl implements ServiceInvoker {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvokerImpl.class);

    @Override
    public Map<String, Object> getServiceInfo(Object service) {
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
        return serviceInfo;
    }

    @Override
    public Object invoke(RequestContext context, Object service, Serializer<?> serializer) throws ServiceException {
        Method method = null;
        Object args[] = null;

        for (Method m : service.getClass().getMethods()) {
            if (m.getName().equals(context.getMethodName())) {
                try {
                    args = serializer.deserialize(context.getArguments(), m.getParameterTypes());
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
            Object result = method.invoke(service, args);
            if (time != null)
                time.printDifference(result);
            return result;
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

}
