package org.zenframework.easyservices.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TcpClient extends Closeable {

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;

    String getHost();

    int getPort();

}
