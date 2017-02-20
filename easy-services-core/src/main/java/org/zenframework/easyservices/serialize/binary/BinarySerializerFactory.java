package org.zenframework.easyservices.serialize.binary;

import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class BinarySerializerFactory implements SerializerFactory {

    public static final String FORMAT = "bin";

    @Override
    public boolean isTextBased() {
        return false;
    }

    @Override
    public boolean isTypeSafe() {
        return true;
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public Serializer getSerializer(Class<?>[] paramTypes, Class<?> returnType, MethodDescriptor methodDescriptor) {
        return BinarySerializer.INSTANCE;
    }

    @Override
    public Serializer getTypeSafeSerializer() {
        return BinarySerializer.INSTANCE;
    }

}
