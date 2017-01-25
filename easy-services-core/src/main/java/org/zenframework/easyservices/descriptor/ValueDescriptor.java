package org.zenframework.easyservices.descriptor;

import org.zenframework.easyservices.serialize.SerializerAdapter;

public class ValueDescriptor {

    private Class<?>[] typeParameters;
    private SerializerAdapter<?, ?> serializerAdapter;
    private boolean reference;

    public Class<?>[] getTypeParameters() {
        return typeParameters;
    }

    public void setTypeParameters(Class<?>... typeParameters) {
        this.typeParameters = typeParameters;
    }

    public SerializerAdapter<?, ?> getSerializerAdapter() {
        return serializerAdapter;
    }

    public void setSerializerAdapter(SerializerAdapter<?, ?> serializerAdapter) {
        this.serializerAdapter = serializerAdapter;
    }

    public boolean isReference() {
        return reference;
    }

    public void setReference(boolean reference) {
        this.reference = reference;
    }

}
