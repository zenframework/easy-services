package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.descriptor.ValueDescriptor;

public interface Serializer<S> {

    S parse(String data) throws SerializationException;

    String compile(S structure);

    Object deserialize(S structure, Class<?> objType) throws SerializationException;

    Object deserialize(S structure, Class<?> objType, ValueDescriptor valueDescriptor) throws SerializationException;

    Object deserialize(S structure, SerializerAdapter<S> adapter, Class<?>... typeParameters) throws SerializationException;

    Object[] deserialize(S structure, Class<?> objTypes[]) throws SerializationException;

    Object[] deserialize(S structure, Class<?> objTypes[], ValueDescriptor[] valueDescriptors) throws SerializationException;

    Object[] deserialize(S structure, SerializerAdapter<S> adapters[], Class<?> typeParameters[][]) throws SerializationException;

    S serialize(Object object);

    S serialize(Object object, ValueDescriptor valueDescriptor);

    S serialize(Object object, SerializerAdapter<S> adapter);

    S serialize(Object[] array);

    S serialize(Object[] array, ValueDescriptor[] valueDescriptors);

    S serialize(Object[] array, SerializerAdapter<S>[] adapters);

}
