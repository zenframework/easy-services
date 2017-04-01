package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.net.TcpxURLConnection;
import org.zenframework.easyservices.util.URIUtil;

public class TcpxURLHandler extends AbstractTcpURLHandler<TcpxURLConnection<TcpxRequestHeader, TcpxResponseHeader>> {

    public static final String PROTOCOL = "tcpx";

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public void prepareConnection(TcpxURLConnection<TcpxRequestHeader, TcpxResponseHeader> connection) {
        try {
            connection.setRequestHeader(getRequestHeader(connection.getURL().toURI()));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        connection.setResponseHeader(new TcpxResponseHeader());
    }

    @Override
    public String getSessionId(TcpxURLConnection<TcpxRequestHeader, TcpxResponseHeader> connection) throws IOException {
        return connection.getResponseHeader().getSessionId();
    }

    @Override
    public void setSessionId(TcpxURLConnection<TcpxRequestHeader, TcpxResponseHeader> connection, String sessionId) throws IOException {
        connection.getRequestHeader().setSessionId(sessionId);
    }

    @Override
    public boolean isSuccessful(TcpxURLConnection<TcpxRequestHeader, TcpxResponseHeader> connection) throws IOException {
        return connection.getResponseHeader().isSuccess();
    }

    private static TcpxRequestHeader getRequestHeader(URI uri) {
        Map<String, List<String>> params = URIUtil.getParameters(uri, "UTF-8");
        String serviceName = uri.getPath().substring(1);
        List<String> methodParam = params.get("method");
        List<String> outParamsParam = params.get("outParameters");
        return new TcpxRequestHeader(null, serviceName, methodParam != null && !methodParam.isEmpty() ? methodParam.get(0) : null, null,
                outParamsParam != null && !outParamsParam.isEmpty() && Boolean.parseBoolean(outParamsParam.get(0)), false);
    }

}
