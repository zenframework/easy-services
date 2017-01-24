package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.descriptor.ValueDescriptor;

public interface Serializer<S> {

    SerializerFactory<S> getFactory();

    S parse(String data) throws SerializationException;

    S[] parseArray(String data) throws SerializationException;

    String compile(S objStruct) throws SerializationException;

    Object deserialize(S objStruct, Class<?> objType) throws SerializationException;

    Object deserialize(S objStruct, Class<?> objType, ValueDescriptor valueDescriptor) throws SerializationException;

    Object deserialize(S objStruct, SerializerAdapter<S> adapter, Class<?>... typeParameters) throws SerializationException;

    Object[] deserialize(S[] arrStruct, Class<?> objTypes[]) throws SerializationException;

    Object[] deserialize(S[] arrStruct, Class<?> objTypes[], ValueDescriptor[] valueDescriptors) throws SerializationException;

    Object[] deserialize(S[] arrStruct, SerializerAdapter<S> adapters[], Class<?> typeParameters[][]) throws SerializationException;

    String serialize(Object object);

    String serialize(Object object, SerializerAdapter<S> adapter);

}
