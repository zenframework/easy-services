package sun.net.www.protocol.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Handler extends java.net.URLStreamHandler {

    @Override
    protected java.net.URLConnection openConnection(URL u) throws IOException {
        return new java.net.URLConnection(u) {

            @Override
            public void connect() throws IOException {}

            @Override
            public InputStream getInputStream() throws IOException {
                InputStream in = getClass().getClassLoader().getResourceAsStream(url.getHost() + url.getPath());
                if (in == null)
                    throw new IOException("Resource " + url + " not found");
                return in;
            }

        };
    }

}
