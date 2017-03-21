package org.zenframework.easyservices;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public interface URLHandler {

    String getProtocol();

    void prepareConnection(URLConnection connection);

    String getSessionId(URLConnection connection) throws IOException;

    void setSessionId(URLConnection connection, String sessionId) throws IOException;

    boolean isSuccessful(URLConnection connection) throws IOException;

    InputStream getErrorStream(URLConnection connection) throws IOException;

}
