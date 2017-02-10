package org.zenframework.easyservices;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public interface ServiceResponse {

    OutputStream getOutputStream() throws IOException;

    OutputStream getErrorOutputStream(Throwable e) throws IOException;

    Writer getWriter() throws IOException;

    Writer getErrorWriter(Throwable e) throws IOException;

}
