package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class AbstractTcpURLConnection<REQ extends Header, RESP extends Header> extends URLConnection {

    private TcpClient client;
    private InputStream in;
    private OutputStream out;

    public AbstractTcpURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() throws IOException {
        if (!connected) {
            client = initClient(url);
            connected = true;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        Header responseHeader = getResponseHeader();
        connect();
        if (in == null) {
            in = client.getInputStream();
            if (responseHeader != null)
                responseHeader.read(in);
        }
        return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        Header requestHeader = getRequestHeader();
        connect();
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

    abstract public REQ getRequestHeader();

    abstract public RESP getResponseHeader();

    abstract protected TcpClient initClient(URL url) throws IOException;

}
