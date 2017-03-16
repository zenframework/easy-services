package org.zenframework.easyservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ClientConnection {

    OutputStream getOutputStream() throws IOException;

    InputStream getInputStream() throws IOException;

    boolean isSuccessful();

    void close();

}
