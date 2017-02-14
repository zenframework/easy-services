package org.zenframework.easyservices.serialize.binary;

import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class BinarySerializerFactory implements SerializerFactory {

    public static final String FORMAT = "bin";

    private static final BinarySerializer SERIALIZER = new BinarySerializer();

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public Serializer getSerializer(Class<?>[] paramTypes, Class<?> returnType, MethodDescriptor methodDescriptor) {
        return SERIALIZER;
    }

}
