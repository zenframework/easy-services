package org.zenframework.easyservices.serialize;

public interface SerializerAdapter<S> {

    S serialize(Serializer<S> serializer, Object object);

    Object deserialize(Serializer<S> serializer, S structure, Class<?>... typeParameters) throws SerializationException;

}
