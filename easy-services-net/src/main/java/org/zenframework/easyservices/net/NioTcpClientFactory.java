package org.zenframework.easyservices.net;

import java.io.IOException;

public class NioTcpClientFactory implements TcpClientFactory {

    @Override
    public TcpClient getTcpClient(String host, int port) throws IOException {
        return new NioTcpClient(host, port);
    }

}
