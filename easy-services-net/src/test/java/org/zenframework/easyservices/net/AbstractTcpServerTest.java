package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

import junit.framework.TestCase;

public abstract class AbstractTcpServerTest extends TestCase {

    protected static final TcpRequestHandler ECHO_HANDLER = new TcpRequestHandler() {

        @Override
        public boolean handleRequest(SocketAddress clientAddr, InputStream in, OutputStream out) throws IOException {
            in = new BlockInputStream(in);
            out = new BlockOutputStream(out);
            try {
                out.write(IOUtils.toByteArray(in));
            } finally {
                IOUtils.closeQuietly(in, out);
            }
            return false;
        }

    };

    protected static final int PORT = 9000;

    protected final boolean blocking;
    protected TcpServer server;

    public AbstractTcpServerTest(boolean blocking) {
        this.blocking = blocking;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        server = blocking ? new SimpleTcpServer(PORT) : new NioTcpServer(PORT);
        server.start();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    protected static String readText(InputStream in) throws IOException {
        StringBuilder str = new StringBuilder();
        byte[] buf = new byte[8192];
        for (int n = in.read(buf); n >= 0; n = in.read(buf))
            str.append(new String(buf, 0, n));
        return str.toString();
    }

}
