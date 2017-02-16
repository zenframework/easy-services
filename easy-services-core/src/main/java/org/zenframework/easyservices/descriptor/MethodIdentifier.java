package org.zenframework.easyservices.descriptor;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.commons.lang.ClassUtils;

public class MethodIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String VOID = "void";

    private final Class<?> cls;
    private final String name;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;
    private final int hashCode;
    private final String str;

    public MethodIdentifier(Class<?> cls, String name, Class<?>[] parameterTypes, Class<?> returnType) {
        this.cls = cls;
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.hashCode = getHashCode(cls, name, parameterTypes);
        this.str = toString(cls, name, parameterTypes, returnType);
    }

    public MethodIdentifier(Method method) {
        this(method.getDeclaringClass(), method.getName(), method.getParameterTypes(), method.getReturnType());
    }

    public Class<?> getMethodClass() {
        return cls;
    }

    public String getName() {
        return name;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodIdentifier))
            return false;
        MethodIdentifier mi = (MethodIdentifier) obj;
        return str.equals(mi.toString());
    }

    @Override
    public String toString() {
        return str;
    }

    public static MethodIdentifier parse(String str) throws ClassNotFoundException {
        int beginParams = str.indexOf('(');
        int endParams = str.lastIndexOf(')');
        int endClass = str.lastIndexOf('.', beginParams);
        if (endClass < 1 || beginParams < 3 || endParams < 4 || str.charAt(endParams + 1) != ':' || str.length() < endParams + 3)
            throw new IllegalArgumentException(str);
        String className = str.substring(0, endClass);
        String name = str.substring(endClass + 1, beginParams);
        String params[] = endParams - beginParams > 1 ? str.substring(beginParams + 1, endParams).split("\\,") : new String[0];
        Class<?>[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++)
            paramTypes[i] = getClass(params[i]);
        return new MethodIdentifier(getClass(className), name, paramTypes, getClass(str.substring(endParams + 2)));
    }

    private static Class<?> getClass(String name) throws ClassNotFoundException {
        return VOID.equals(name) ? void.class : ClassUtils.getClass(name);
    }

    private static int getHashCode(Class<?> cls, String name, Class<?>[] parameterTypes) {
        int hash = cls.hashCode() ^ name.hashCode();
        for (Class<?> argType : parameterTypes)
            hash ^= argType.hashCode();
        return hash;
    }

    private static String toString(Class<?> cls, String name, Class<?>[] parameterTypes, Class<?> returnType) {
        StringBuilder str = new StringBuilder();
        str.append(cls.getCanonicalName()).append('.').append(name).append('(');
        for (Class<?> paramType : parameterTypes)
            str.append(paramType.getCanonicalName()).append(',');
        if (parameterTypes.length > 0)
            str.setLength(str.length() - 1);
        str.append("):").append(returnType.getCanonicalName());
        return str.toString();
    }

}
