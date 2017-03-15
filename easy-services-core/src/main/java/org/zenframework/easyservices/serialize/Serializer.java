package org.zenframework.easyservices.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer {

    Object[] deserializeParameters(InputStream in) throws IOException, SerializationException;

    Object deserializeResult(InputStream in, boolean success) throws IOException, SerializationException;

    void serialize(Object object, OutputStream out) throws IOException;

}
