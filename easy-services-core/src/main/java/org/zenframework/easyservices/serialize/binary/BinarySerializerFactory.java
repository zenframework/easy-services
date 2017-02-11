package org.zenframework.easyservices.serialize.binary;

import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.SerializerFactory;

public class BinarySerializerFactory implements SerializerFactory {

    public static final String FORMAT = "bin";

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public Serializer getSerializer() {
        return new BinarySerializer();
    }

}
