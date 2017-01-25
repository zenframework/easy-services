package org.zenframework.easyservices.serialize;

public interface SerializerFactory<S> {

    Serializer<S> getSerializer();

    <T> SerializerAdapter<S, T> getAdapter(Class<T> type);

}
