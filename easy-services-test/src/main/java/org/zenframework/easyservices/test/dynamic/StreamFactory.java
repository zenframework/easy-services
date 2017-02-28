package org.zenframework.easyservices.test.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;

public interface StreamFactory extends Remote {

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;

    RmiInputStream getRmiInputStream() throws IOException;
    
    RmiOutputStream getRmiOutputStream() throws IOException;

}
