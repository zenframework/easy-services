package org.zenframework.easyservices;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public interface ClientURLHandler {

    String getProtocol();

    String getSessionId(URLConnection connection);

    void setSessionId(URLConnection connection, String sessionId);

    boolean isError(URLConnection connection) throws IOException;

    InputStream getErrorStream(URLConnection connection) throws IOException;

}
