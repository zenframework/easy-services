package org.zenframework.easyservices.descriptor;

import org.zenframework.easyservices.serialize.SerializerAdapter;

public class ValueDescriptor {

    private Class<?>[] typeParameters;
    private SerializerAdapter<?> serializerAdapter;
    private boolean dynamicService;

    public Class<?>[] getTypeParameters() {
        return typeParameters;
    }

    public void setTypeParameters(Class<?>[] typeParameters) {
        this.typeParameters = typeParameters;
    }

    public SerializerAdapter<?> getSerializerAdapter() {
        return serializerAdapter;
    }

    public void setSerializerAdapter(SerializerAdapter<?> serializerAdapter) {
        this.serializerAdapter = serializerAdapter;
    }

    public boolean isDynamicService() {
        return dynamicService;
    }

    public void setDynamicService(boolean dynamicService) {
        this.dynamicService = dynamicService;
    }

}
