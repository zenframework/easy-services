package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;

public class MethodIdentifier {

    private final String name;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;

    public MethodIdentifier(String name, Class<?>[] parameterTypes, Class<?> returnType) {
        this.name = name;
        this.parameterTypes = parameterTypes;
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

}
