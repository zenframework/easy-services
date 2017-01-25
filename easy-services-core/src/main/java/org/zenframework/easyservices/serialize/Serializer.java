package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.descriptor.ValueDescriptor;

public interface Serializer<S> {

    S parse(String data) throws SerializationException;

    S[] parseArray(String data) throws SerializationException;

    String compile(S structure);

    String compile(S[] array);

    <T> T deserialize(S structure, Class<T> objType) throws SerializationException;

    <T> T deserialize(S structure, Class<T> objType, ValueDescriptor valueDescriptor) throws SerializationException;

    S serialize(Object object);

    S serialize(Object object, ValueDescriptor valueDescriptor);

    S[] newArray(int length);

}
