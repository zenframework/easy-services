package org.zenframework.easyservices.serialize;

public interface SerializerFactory {

    boolean isByteSerializationSupported();

    boolean isCharSerializationSupported();

    ByteSerializer getByteSerializer();

    CharSerializer getCharSerializer();
}
