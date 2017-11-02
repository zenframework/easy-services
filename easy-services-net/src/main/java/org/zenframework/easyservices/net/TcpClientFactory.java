package org.zenframework.easyservices.net;

import java.io.IOException;

public interface TcpClientFactory {

    TcpClient getTcpClient(String host, int port) throws IOException;

}
