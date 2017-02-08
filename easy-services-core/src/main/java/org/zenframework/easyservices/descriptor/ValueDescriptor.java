package org.zenframework.easyservices.descriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;

public class ValueDescriptor {

    private final Set<Object> adapters = new HashSet<Object>();
    private Class<?>[] typeParameters = new Class<?>[0];
    private boolean reference = false;

    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> cls) {
        for (Object adapter : adapters)
            if (cls.isInstance(adapter))
                return (T) adapter;
        return null;
    }

    public void addAdapter(Object adapter) {
        this.adapters.add(adapter);
    }

    public Class<?>[] getTypeParameters() {
        return typeParameters;
    }

    public void setTypeParameters(Class<?>... typeParameters) {
        this.typeParameters = typeParameters;
    }

    public Set<Object> getAdapters() {
        return adapters;
    }

    public void setAdapters(Collection<Object> adapters) {
        this.adapters.clear();
        this.adapters.addAll(adapters);
    }

    public boolean isReference() {
        return reference;
    }

    public void setReference(boolean reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append('[');
        if (reference)
            str.append("ref ");
        if (typeParameters != null && typeParameters.length > 0)
            str.append("tp:").append(ClassUtils.convertClassesToClassNames(Arrays.asList(typeParameters))).append(' ');
        if (!adapters.isEmpty())
            str.append("adp:").append(adapters).append(' ');
        if (str.length() > 1)
            str.setLength(str.length() - 1);
        return str.append(']').toString();
    }

}
