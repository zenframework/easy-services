package org.zenframework.easyservices.serialize.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.ResponseObject;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;

public class BinarySerializer implements Serializer {

    @Override
    public Object deserialize(InputStream in, Class<?> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException {
        ObjectInputStream obj = new ObjectInputStream(in);
        try {
            return obj.readObject();
        } catch (ClassNotFoundException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public Object[] deserialize(InputStream in, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws IOException, SerializationException {
        ObjectInputStream obj = new ObjectInputStream(in);
        try {
            return (Object[]) obj.readObject();
        } catch (ClassNotFoundException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public ResponseObject deserialize(InputStream in, Class<?> returnType, ValueDescriptor returnDescriptor, Class<?>[] paramTypes,
            ValueDescriptor[] paramDescirptors) throws IOException, SerializationException {
        ObjectInputStream obj = new ObjectInputStream(in);
        try {
            return (ResponseObject) obj.readObject();
        } catch (ClassNotFoundException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public void serialize(Object object, OutputStream out) throws IOException {
        ObjectOutputStream obj = new ObjectOutputStream(out);
        obj.writeObject(object);
        obj.flush();
    }

    @Override
    public Object deserialize(byte[] data, Class<?> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException {
        return deserialize(new ByteArrayInputStream(data), objType, valueDescriptor);
    }

    @Override
    public Object[] deserialize(byte[] data, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws SerializationException, IOException {
        return deserialize(new ByteArrayInputStream(data), objTypes, valueDescriptors);
    }

    @Override
    public ResponseObject deserialize(byte[] data, Class<?> returnType, ValueDescriptor returnDescriptor, Class<?>[] paramTypes,
            ValueDescriptor[] paramDescirptors) throws SerializationException, IOException {
        return deserialize(new ByteArrayInputStream(data), returnType, returnDescriptor, paramTypes, paramDescirptors);
    }

    @Override
    public byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(object, out);
        return out.toByteArray();
    }

}
