package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SimpleTcpClient implements TcpClient {

    private final String host;
    private final Socket socket;

    public SimpleTcpClient(String host, int port) throws IOException {
        this.host = host;
        this.socket = new Socket(host, port);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return socket.getPort();
    }

}
