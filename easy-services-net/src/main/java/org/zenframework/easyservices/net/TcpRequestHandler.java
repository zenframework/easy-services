package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

public interface TcpRequestHandler {

    boolean handleRequest(SocketAddress clientAddr, InputStream in, OutputStream out) throws IOException;

}
