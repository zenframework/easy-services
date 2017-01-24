package org.zenframework.easyservices.serialize;

public interface SerializerFactory<S> {

    Serializer<S> getSerializer();

    SerializerAdapter<S> getAdapter(Class<?> type);

}
