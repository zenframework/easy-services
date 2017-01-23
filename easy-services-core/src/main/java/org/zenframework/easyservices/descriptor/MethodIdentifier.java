package org.zenframework.easyservices.descriptor;

import java.lang.reflect.Method;

public class MethodIdentifier {

    private final String name;
    private Class<?>[] argTypes;

    public MethodIdentifier(String name, Class<?>[] argTypes) {
        this.name = name;
        this.argTypes = argTypes;
    }

    public MethodIdentifier(Method method) {
        this(method.getName(), method.getParameterTypes());
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public void setArgTypes(Class<?>[] argTypes) {
        this.argTypes = argTypes;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = name.hashCode();
        for (Class<?> argType : argTypes)
            hash ^= argType.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodIdentifier))
            return false;
        MethodIdentifier mi = (MethodIdentifier) obj;
        if (!name.equals(mi.name) || argTypes.length != mi.argTypes.length)
            return false;
        for (int i = 0; i < argTypes.length; i++)
            if (!argTypes[i].equals(mi.argTypes[i]))
                return false;
        return true;
    }

}
