package org.zenframework.easyservices.serialize;

public interface SerializerFactory {

    String getFormat();

    Serializer getSerializer();

}
