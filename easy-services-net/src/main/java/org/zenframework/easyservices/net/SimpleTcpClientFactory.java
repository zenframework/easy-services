package org.zenframework.easyservices.net;

import java.io.IOException;

public class SimpleTcpClientFactory implements TcpClientFactory {

    @Override
    public TcpClient getTcpClient(String host, int port) throws IOException {
        return new SimpleTcpClient(host, port);
    }

}
