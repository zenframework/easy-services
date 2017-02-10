package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;

import org.apache.commons.lang.ClassUtils;

public class MethodIdentifier {

    private static final String VOID = "void";

    private final String name;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;
    private final int hashCode;
    private final String str;

    public MethodIdentifier(String name, Class<?>[] parameterTypes, Class<?> returnType) {
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.hashCode = getHashCode(name, parameterTypes);
        this.str = toString(name, parameterTypes, returnType);
    }

    public MethodIdentifier(Method method) {
        this(method.getName(), method.getParameterTypes(), method.getReturnType());
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
        if (beginParams < 1 || endParams < 2 || str.charAt(endParams + 1) != ':' || str.length() < endParams + 3)
            throw new IllegalArgumentException(str);
        String name = str.substring(0, beginParams);
        String params[] = endParams - beginParams > 1 ? str.substring(beginParams + 1, endParams).split("\\,") : new String[0];
        Class<?>[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++)
            paramTypes[i] = getClass(params[i]);
        return new MethodIdentifier(name, paramTypes, getClass(str.substring(endParams + 2)));
    }

    private static Class<?> getClass(String name) throws ClassNotFoundException {
        return VOID.equals(name) ? void.class : ClassUtils.getClass(name);
    }

    private static int getHashCode(String name, Class<?>[] parameterTypes) {
        int hash = name.hashCode();
        for (Class<?> argType : parameterTypes)
            hash ^= argType.hashCode();
        return hash;
    }

    private static String toString(String name, Class<?>[] parameterTypes, Class<?> returnType) {
        StringBuilder str = new StringBuilder();
        str.append(name).append('(');
        for (Class<?> paramType : parameterTypes)
            str.append(paramType.getName()).append(',');
        if (parameterTypes.length > 0)
            str.setLength(str.length() - 1);
        str.append("):").append(returnType.getCanonicalName());
        return str.toString();
    }

}
