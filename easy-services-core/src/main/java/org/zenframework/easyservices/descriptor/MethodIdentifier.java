package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;

import org.apache.commons.lang.ClassUtils;

public class MethodIdentifier {

    private final String name;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;

    public MethodIdentifier(String name, Class<?>[] parameterTypes, Class<?> returnType) {
        this.name = name;
        this.parameterTypes = ClassUtils.primitivesToWrappers(parameterTypes);
        this.returnType = returnType;
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
        int hash = name.hashCode();
        for (Class<?> argType : parameterTypes)
            hash ^= argType.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodIdentifier))
            return false;
        MethodIdentifier mi = (MethodIdentifier) obj;
        if (!name.equals(mi.name) || parameterTypes.length != mi.parameterTypes.length)
            return false;
        for (int i = 0; i < parameterTypes.length; i++)
            if (!parameterTypes[i].equals(mi.parameterTypes[i]))
                return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(name).append('(');
        for (Class<?> paramType : parameterTypes)
            str.append(ClassUtils.primitiveToWrapper(paramType).getName()).append(',');
        if (parameterTypes.length > 0)
            str.setLength(str.length() - 1);
        str.append("):").append(ClassUtils.primitiveToWrapper(returnType).getName());
        return str.toString();
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
            paramTypes[i] = Class.forName(params[i]);
        return new MethodIdentifier(name, paramTypes, Class.forName(str.substring(endParams + 2)));
    }

}
