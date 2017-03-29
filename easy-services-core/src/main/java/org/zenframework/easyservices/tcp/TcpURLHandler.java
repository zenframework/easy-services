package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.URLHandler;
import org.zenframework.easyservices.net.TcpURLConnection;
import org.zenframework.easyservices.util.URIUtil;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public class TcpURLHandler implements URLHandler<TcpURLConnection<TcpRequestHeader, TcpResponseHeader>> {

    public static final String PROTOCOL = "tcp";

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void prepareConnection(TcpURLConnection<TcpRequestHeader, TcpResponseHeader> connection) {
        try {
            connection.setRequestHeader(getRequestHeader(connection.getURL().toURI()));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        connection.setResponseHeader(new TcpResponseHeader());
    }

    @Override
    public String getSessionId(TcpURLConnection<TcpRequestHeader, TcpResponseHeader> connection) throws IOException {
        return connection.getResponseHeader().getSessionId();
    }

    @Override
    public void setSessionId(TcpURLConnection<TcpRequestHeader, TcpResponseHeader> connection, String sessionId) throws IOException {
        connection.getRequestHeader().setSessionId(sessionId);
    }

    @Override
    public boolean isSuccessful(TcpURLConnection<TcpRequestHeader, TcpResponseHeader> connection) throws IOException {
        return connection.getResponseHeader().isSuccess();
    }

    @Override
    public OutputStream getOutputStream(TcpURLConnection<TcpRequestHeader, TcpResponseHeader> connection) throws IOException {
        return new BlockOutputStream(connection.getOutputStream());
    }

    @Override
    public InputStream getInputStream(TcpURLConnection<TcpRequestHeader, TcpResponseHeader> connection) throws IOException {
        return new BlockInputStream(connection.getInputStream());
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
