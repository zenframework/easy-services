package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class TcpURLConnection<REQ extends Header, RESP extends Header> extends URLConnection {

    private Socket socket;
    private REQ requestHeader;
    private RESP responseHeader;
    private InputStream in;
    private OutputStream out;

    public TcpURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        socket = new Socket(url.getHost(), url.getPort());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (in == null) {
            in = socket.getInputStream();
            if (responseHeader != null)
                responseHeader.read(in);
        }
        return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (out == null) {
            out = socket.getOutputStream();
            if (requestHeader != null)
                requestHeader.write(out);
        }
        return out;
    }

    public Socket getSocket() {
        return socket;
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
