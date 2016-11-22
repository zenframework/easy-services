package org.zenframework.easyservices.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.zenframework.easyservices.ClassInfo;
import org.zenframework.easyservices.ServiceInfo;

public class ServiceInfoImpl implements ServiceInfo {

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
