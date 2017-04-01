package sun.net.www.protocol.tcpx;

import java.io.IOException;
import java.net.URL;

import org.zenframework.easyservices.net.TcpxURLConnection;

public class Handler extends java.net.URLStreamHandler {

    @SuppressWarnings("rawtypes")
    @Override
    protected java.net.URLConnection openConnection(URL u) throws IOException {
        return new TcpxURLConnection(u);
    }

}
