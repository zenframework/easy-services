package org.zenframework.easyservices.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.ResponseObject;
import org.zenframework.easyservices.descriptor.ValueDescriptor;

public interface Serializer {

    Object deserialize(InputStream in, Class<?> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException;

    Object[] deserialize(InputStream in, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws IOException, SerializationException;

    ResponseObject deserialize(InputStream in, Class<?> returnType, ValueDescriptor returnDescriptor, Class<?>[] paramTypes,
            ValueDescriptor[] paramDescirptors) throws IOException, SerializationException;

    void serialize(Object object, OutputStream out) throws IOException;

    Object deserialize(byte[] data, Class<?> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException;

    Object[] deserialize(byte[] data, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws IOException, SerializationException;

    ResponseObject deserialize(byte[] data, Class<?> returnType, ValueDescriptor returnDescriptor, Class<?>[] paramTypes,
            ValueDescriptor[] paramDescirptors) throws IOException, SerializationException;

    byte[] serialize(Object object) throws IOException;

}
