package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public class TcpURLConnection<REQ extends Header, RESP extends Header> extends URLConnection {

    private static final ThreadLocal<Socket> sockets = new ThreadLocal<Socket>();

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
        socket = sockets.get();
        if (socket == null || !socket.getInetAddress().getHostName().equals(url.getHost()) || socket.getPort() != url.getPort()) {
            socket = new SharedSocket(url.getHost(), url.getPort());
            sockets.set(socket);
        }
        socket = new Socket(url.getHost(), url.getPort());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (in == null) {
            in = new BlockInputStream(socket.getInputStream()) {

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        socket.close();
                    }
                }

            };
            if (responseHeader != null)
                responseHeader.read(in);
        }
        return in;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (out == null) {
            out = new BlockOutputStream(socket.getOutputStream());
            if (requestHeader != null)
                requestHeader.write(out);
        }
        return out;
    }

    public REQ getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(REQ requestHeader) {
        this.requestHeader = requestHeader;
    }

    public RESP getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(RESP responseHeader) {
        this.responseHeader = responseHeader;
    }

    private static class SharedSocket extends Socket {

        private final InputStream in = super.getInputStream();
        private final OutputStream out = super.getOutputStream();

        private SharedSocket(String host, int port) throws IOException {
            super(host, port);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return in;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return out;
        }

    }

}
