package org.zenframework.easyservices.serialize.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;

public class BinarySerializer implements Serializer {

    public static final BinarySerializer INSTANCE = new BinarySerializer();

    @Override
    public Object[] deserializeParameters(InputStream in) throws IOException, SerializationException {
        ObjectInputStream obj = new ObjectInputStream(in);
        try {
            return (Object[]) obj.readObject();
        } catch (ClassNotFoundException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public Object deserializeResult(InputStream in, boolean success) throws IOException, SerializationException {
        ObjectInputStream obj = new ObjectInputStream(in);
        try {
            return obj.readObject();
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

}
