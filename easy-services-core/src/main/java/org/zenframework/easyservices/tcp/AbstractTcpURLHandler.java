package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.URLHandler;
import org.zenframework.easyservices.net.AbstractTcpURLConnection;
import org.zenframework.easyservices.net.Header;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public abstract class AbstractTcpURLHandler<CONN extends AbstractTcpURLConnection<? extends Header, ? extends Header>> implements URLHandler<CONN> {

    @Override
    public OutputStream getOutputStream(CONN connection) throws IOException {
        return new BlockOutputStream(connection.getOutputStream());
    }

    @Override
    public InputStream getInputStream(final CONN connection) throws IOException {
        return new BlockInputStream(connection.getInputStream()) {

            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    connection.getClient().close();
                }
            }

        };
    }

}
