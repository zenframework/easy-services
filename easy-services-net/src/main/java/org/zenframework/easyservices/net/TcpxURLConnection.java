package org.zenframework.easyservices.net;

import java.io.IOException;
import java.net.URL;

public class TcpxURLConnection<REQ extends Header, RESP extends Header> extends AbstractTcpURLConnection<REQ, RESP> {

    private final ThreadLocal<TcpClient> clients = new ThreadLocal<TcpClient>();

    public TcpxURLConnection(URL url) {
        super(url);
    }

    private REQ requestHeader;
    private RESP responseHeader;

    @Override
    public REQ getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(REQ requestHeader) {
        this.requestHeader = requestHeader;
    }

    @Override
    public RESP getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(RESP responseHeader) {
        this.responseHeader = responseHeader;
    }

    @Override
    protected TcpClient initClient(URL url) throws IOException {
        TcpClient client = clients.get();
        if (client == null || !client.getHost().equals(url.getHost()) || client.getPort() != url.getPort()) {
            client = new SimpleTcpClient(url.getHost(), url.getPort());
            clients.set(client);
        }
        return client;
    }

}
