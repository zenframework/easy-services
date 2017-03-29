package org.zenframework.easyservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

public interface URLHandler<CONN extends URLConnection> {

    String getProtocol();

    void prepareConnection(CONN connection);

    String getSessionId(CONN connection) throws IOException;

    void setSessionId(CONN connection, String sessionId) throws IOException;

    boolean isSuccessful(CONN connection) throws IOException;

    OutputStream getOutputStream(CONN connection) throws IOException;

    InputStream getInputStream(CONN connection) throws IOException;

}
