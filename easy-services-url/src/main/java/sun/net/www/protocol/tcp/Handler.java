package sun.net.www.protocol.tcp;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

public class Handler extends java.net.URLStreamHandler {

    @Override
    protected java.net.URLConnection openConnection(URL u) throws IOException {
        return new java.net.URLConnection(u) {

            private Socket socket;

            @Override
            public void connect() throws IOException {
                socket = new Socket(url.getHost(), url.getPort());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FilterInputStream(socket.getInputStream()) {

                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            socket.close();
                        }
                    }

                };
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return socket.getOutputStream();
            }

        };
    }

}
