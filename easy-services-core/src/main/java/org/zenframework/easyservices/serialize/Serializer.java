package org.zenframework.easyservices.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.descriptor.ValueDescriptor;

public interface Serializer {

    <T> T deserialize(InputStream in, Class<T> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException;

    Object[] deserialize(InputStream in, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws IOException, SerializationException;

    void serialize(Object object, OutputStream out) throws IOException;

    <T> T deserialize(String data, Class<T> objType, ValueDescriptor valueDescriptor) throws SerializationException;

    Object[] deserialize(String data, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws SerializationException;

    String serialize(Object object);

}
