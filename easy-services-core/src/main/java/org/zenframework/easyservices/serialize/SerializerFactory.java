package org.zenframework.easyservices.serialize;

public interface SerializerFactory<T> {

    Serializer<T> getSerializer();

    SerializerAdapter<?> getAdapter(Class<?> type);

}
