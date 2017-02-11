package org.zenframework.easyservices.serialize.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializationException;

@SuppressWarnings("unchecked")
public class BinarySerializer implements Serializer {

    @Override
    public <T> T deserialize(InputStream in, Class<T> objType, ValueDescriptor valueDescriptor) throws IOException, SerializationException {
        ObjectInputStream obj = new ObjectInputStream(in);
        try {
            return (T) obj.readObject();
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
    public void serialize(Object object, OutputStream out) throws IOException {
        ObjectOutputStream obj = new ObjectOutputStream(out);
        obj.writeObject(object);
        obj.flush();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> objType, ValueDescriptor valueDescriptor) throws SerializationException, IOException {
        return deserialize(new ByteArrayInputStream(data), objType, valueDescriptor);
    }

    @Override
    public Object[] deserialize(byte[] data, Class<?>[] objTypes, ValueDescriptor[] valueDescriptors) throws SerializationException, IOException {
        return deserialize(new ByteArrayInputStream(data), objTypes, valueDescriptors);
    }

    @Override
    public byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(object, out);
        return out.toByteArray();
    }

}
