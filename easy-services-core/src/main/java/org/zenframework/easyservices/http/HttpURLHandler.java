package org.zenframework.easyservices.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.zenframework.easyservices.URLHandler;
import org.zenframework.easyservices.util.StringUtil;

public class HttpURLHandler implements URLHandler {

    public static final String PROTOCOL = "http";

    private static final String REQ_HEADER_COOKIE = "Cookie";
    private static final String RESP_HEADER_COOKIE = "Set-Cookie";
    private static final String SESSION_ID = "JSESSIONID";

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void prepareConnection(URLConnection connection) {}

    @Override
    public String getSessionId(URLConnection connection) {
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        return StringUtil.toMap(httpConnection.getHeaderField(RESP_HEADER_COOKIE), "=", ";").get(SESSION_ID);
    }

    @Override
    public void setSessionId(URLConnection connection, String sessionId) {
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestProperty(REQ_HEADER_COOKIE, SESSION_ID + '=' + sessionId);
    }

    @Override
    public boolean isSuccessful(URLConnection connection) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        return httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    @Override
    public InputStream getErrorStream(URLConnection connection) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        return httpConnection.getErrorStream();
    }

}
