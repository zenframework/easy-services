package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class TcpURLConnection<REQ extends Header, RESP extends Header> extends URLConnection {

    private TcpClient client;
    private REQ requestHeader;
    private RESP responseHeader;
    private InputStream in;
    private OutputStream out;

    public TcpURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        client = new SimpleTcpClient(url.getHost(), url.getPort());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (in == null) {
            in = client.getInputStream();
            if (responseHeader != null)
                responseHeader.read(in);
        }
        return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (out == null) {
            out = client.getOutputStream();
            if (requestHeader != null)
                requestHeader.write(out);
        }
        return out;
    }

    public TcpClient getClient() {
        return client;
    }

    public REQ getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(REQ requestHeader) {
        this.requestHeader = requestHeader;
    }

    public RESP getResponseHeader() throws IOException {
        getInputStream();
        return responseHeader;
    }

    public void setResponseHeader(RESP responseHeader) {
        this.responseHeader = responseHeader;
    }

}
