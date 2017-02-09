package org.zenframework.easyservices;

import java.io.IOException;
import java.io.OutputStream;

public interface ServiceResponse {

    OutputStream getOutputStream() throws IOException;

    OutputStream getErrorStream(Throwable e) throws IOException;

}
