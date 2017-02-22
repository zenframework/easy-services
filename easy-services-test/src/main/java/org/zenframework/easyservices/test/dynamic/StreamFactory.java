package org.zenframework.easyservices.test.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamFactory {

    InputStream getInputStream() throws IOException;

    OutputStream getOuptputStream() throws IOException;

}
