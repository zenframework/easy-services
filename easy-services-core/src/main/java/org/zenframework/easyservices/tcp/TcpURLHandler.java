package org.zenframework.easyservices.tcp;

import java.io.IOException;

import org.zenframework.easyservices.net.TcpURLConnection;

public class TcpURLHandler extends AbstractTcpURLHandler<TcpURLConnection> {

    public static final String PROTOCOL = "tcp";

    public static final String HEADER_SESSION_ID = "sessionId";
    public static final String HEADER_SUCCESSFUL = "successful";

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void prepareConnection(TcpURLConnection connection) {}

    @Override
    public String getSessionId(TcpURLConnection connection) throws IOException {
        return connection.getHeaderField(HEADER_SESSION_ID);
    }

    @Override
    public void setSessionId(TcpURLConnection connection, String sessionId) throws IOException {
        connection.setRequestProperty(HEADER_SESSION_ID, sessionId);
    }

    @Override
    public boolean isSuccessful(TcpURLConnection connection) throws IOException {
        return Boolean.parseBoolean(connection.getHeaderField(HEADER_SUCCESSFUL));
    }

}
