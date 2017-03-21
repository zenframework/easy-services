package sun.net.www.protocol.tcp;

import java.io.IOException;
import java.net.URL;

import org.zenframework.easyservices.net.TcpURLConnection;

public class Handler extends java.net.URLStreamHandler {

    @SuppressWarnings("rawtypes")
    @Override
    protected java.net.URLConnection openConnection(URL u) throws IOException {
        TcpURLConnection tcpConnection = new TcpURLConnection(u);
        tcpConnection.connect();
        return tcpConnection;
    }

}
