package org.zenframework.easyservices.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.zenframework.easyservices.URLHandler;
import org.zenframework.easyservices.util.StringUtil;

public class HttpURLHandler implements URLHandler<HttpURLConnection> {

    public static final String PROTOCOL = "http";

    private static final String REQ_HEADER_COOKIE = "Cookie";
    private static final String RESP_HEADER_COOKIE = "Set-Cookie";
    private static final String SESSION_ID = "JSESSIONID";

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void prepareConnection(HttpURLConnection connection) {}

    @Override
    public String getSessionId(HttpURLConnection connection) {
        return StringUtil.toMap(connection.getHeaderField(RESP_HEADER_COOKIE), "=", ";").get(SESSION_ID);
    }

    @Override
    public void setSessionId(HttpURLConnection connection, String sessionId) {
        connection.setRequestProperty(REQ_HEADER_COOKIE, SESSION_ID + '=' + sessionId);
    }

    @Override
    public boolean isSuccessful(HttpURLConnection connection) throws IOException {
        return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    @Override
    public OutputStream getOutputStream(HttpURLConnection connection, boolean cacheInputSafe) throws IOException {
        return connection.getOutputStream();
    }

    @Override
    public InputStream getInputStream(HttpURLConnection connection, boolean cacheInputSafe) throws IOException {
        return connection.getResponseCode() == HttpURLConnection.HTTP_OK ? connection.getInputStream() : connection.getErrorStream();
    }

}
