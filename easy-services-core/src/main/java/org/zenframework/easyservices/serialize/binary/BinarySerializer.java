package org.zenframework.easyservices.serialize.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.ResponseObject;
import org.zenframework.easyservices.serialize.SerializationException;
import org.zenframework.easyservices.serialize.Serializer;

public class BinarySerializer implements Serializer {

    private final Class<?>[] paramTypes;

    public BinarySerializer(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    @Override
    public Object[] deserializeParameters(InputStream in) throws IOException, SerializationException {
        ObjectInputStream obj = new ObjectInputStream(in);
        try {
            Object[] result = (Object[]) obj.readObject();
            if (result.length != paramTypes.length)
                throw new SerializationException("result.length == " + result.length + " != " + paramTypes.length + " == paramTypes.length");
            return result;
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
    public ResponseObject deserializeResponse(InputStream in, boolean success) throws IOException, SerializationException {
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

}
