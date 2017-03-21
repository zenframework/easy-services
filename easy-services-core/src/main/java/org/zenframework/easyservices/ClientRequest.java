package org.zenframework.easyservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ClientRequest {

    void writeRequestHeader() throws IOException;

    OutputStream getOutputStream() throws IOException;

    void readResponseHeader() throws IOException;

    InputStream getInputStream() throws IOException;

    boolean isSuccessful();

}
