package org.zenframework.easyservices;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public interface ServiceResponse {

    OutputStream getOutputStream() throws IOException;

    Writer getWriter() throws IOException;

    void sendError(Throwable e);

}
