package org.zenframework.easyservices.serialize;

public interface SerializerAdapter<S, T> {

    S serialize(Serializer<S> serializer, Object object);

    T deserialize(Serializer<S> serializer, S structure, Class<?>... typeParameters) throws SerializationException;

}
