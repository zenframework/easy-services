package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TcpRequestHandler {

    void handleRequest(InputStream in, OutputStream out) throws IOException;

}
