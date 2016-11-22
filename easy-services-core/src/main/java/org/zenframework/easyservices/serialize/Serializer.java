package org.zenframework.easyservices.serialize;

public interface Serializer<S> {

    <T> T deserialize(S objStruct, Class<T> objType) throws SerializationException;

    <T> T deserialize(String data, Class<T> objType) throws SerializationException;

    Object deserialize(String data, SerializerAdapter<S> adapter, Class<?>... typeParameters) throws SerializationException;

    Object[] deserialize(S arrStruct, Class<?> objTypes[]) throws SerializationException;

    Object[] deserialize(String data, Class<?> objTypes[]) throws SerializationException;

    Object[] deserialize(String data, SerializerAdapter<S> adapters[], Class<?> typeParameters[][]) throws SerializationException;

    String serialize(Object object);

    String serialize(Object object, SerializerAdapter<S> adapter);

}
