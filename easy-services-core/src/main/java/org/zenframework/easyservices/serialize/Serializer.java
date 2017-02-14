package org.zenframework.easyservices.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.ResponseObject;

public interface Serializer {

    Object[] deserializeParameters(InputStream in) throws IOException, SerializationException;

    Object deserializeResult(InputStream in) throws IOException, SerializationException;

    ResponseObject deserializeResponse(InputStream in, boolean success) throws IOException, SerializationException;

    void serialize(Object object, OutputStream out) throws IOException;

}
