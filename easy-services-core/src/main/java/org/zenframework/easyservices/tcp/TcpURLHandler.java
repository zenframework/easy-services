package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.URLHandler;
import org.zenframework.easyservices.net.TcpURLConnection;
import org.zenframework.easyservices.util.URIUtil;

@SuppressWarnings("unchecked")
public class TcpURLHandler implements URLHandler {

    public static final String PROTOCOL = "tcp";

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void prepareConnection(URLConnection connection) {
        TcpURLConnection<TcpRequestHeader, TcpResponseHeader> tcpConnection = (TcpURLConnection<TcpRequestHeader, TcpResponseHeader>) connection;
        try {
            tcpConnection.setRequestHeader(getRequestHeader(connection.getURL().toURI()));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        tcpConnection.setResponseHeader(new TcpResponseHeader());
    }

    @Override
    public String getSessionId(URLConnection connection) throws IOException {
        TcpURLConnection<TcpRequestHeader, TcpResponseHeader> tcpConnection = (TcpURLConnection<TcpRequestHeader, TcpResponseHeader>) connection;
        tcpConnection.getInputStream();
        return tcpConnection.getResponseHeader().getSessionId();
    }

    @Override
    public void setSessionId(URLConnection connection, String sessionId) throws IOException {
        TcpURLConnection<TcpRequestHeader, TcpResponseHeader> tcpConnection = (TcpURLConnection<TcpRequestHeader, TcpResponseHeader>) connection;
        tcpConnection.getRequestHeader().setSessionId(sessionId);
    }

    @Override
    public boolean isSuccessful(URLConnection connection) throws IOException {
        TcpURLConnection<TcpRequestHeader, TcpResponseHeader> tcpConnection = (TcpURLConnection<TcpRequestHeader, TcpResponseHeader>) connection;
        tcpConnection.getInputStream();
        return tcpConnection.getResponseHeader().isSuccess();
    }

    @Override
    public InputStream getErrorStream(URLConnection connection) throws IOException {
        return connection.getInputStream();
    }

    private static TcpRequestHeader getRequestHeader(URI uri) {
        Map<String, List<String>> params = URIUtil.getParameters(uri, "UTF-8");
        String serviceName = uri.getPath().substring(1);
        List<String> methodParam = params.get("method");
        List<String> outParamsParam = params.get("outParameters");
        return new TcpRequestHeader(null, serviceName, methodParam != null && !methodParam.isEmpty() ? methodParam.get(0) : null, null,
                outParamsParam != null && !outParamsParam.isEmpty() && Boolean.parseBoolean(outParamsParam.get(0)));
    }

}
