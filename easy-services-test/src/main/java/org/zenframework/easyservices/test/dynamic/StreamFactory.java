package org.zenframework.easyservices.test.dynamic;

import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.annotations.Value;

public interface StreamFactory {

    @Value(reference = true)
    InputStream getInputStream();

    @Value(reference = true)
    OutputStream getOuptputStream();

}
