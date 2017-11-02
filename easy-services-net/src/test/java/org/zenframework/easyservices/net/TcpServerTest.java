package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zenframework.easyservices.util.ThreadUtil;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;
import org.zenframework.easyservices.util.thread.Task;

@RunWith(Parameterized.class)
public class TcpServerTest extends AbstractTcpServerTest {

    private static final int DATA_SIZE = 15000;
    private static final int THREADS = 100;

    @Parameterized.Parameters(name = "#{index} server-blocking: {0}, client-blocking: {1}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[] { false, false }, new Object[] { false, true }, new Object[] { true, false }, new Object[] { true, true });
    }

    private final boolean clientBlocking;
    private final byte[] data = new byte[DATA_SIZE];

    public TcpServerTest(boolean serverBlocking, boolean clientBlocking) {
        super(serverBlocking);
        this.clientBlocking = clientBlocking;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        for (int i = 0; i < DATA_SIZE; i++)
            data[i] = (byte) (i % 255);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testEchoServer() throws Exception {
        server.setRequestHandler(ECHO_HANDLER);
        ThreadUtil.runMultiThreadTask(new Task() {

            @Override
            public void run(int taskNumber) throws Exception {
                TcpClient client = getClient("localhost", PORT, clientBlocking);
                InputStream in = new BlockInputStream(client.getInputStream());
                OutputStream out = new BlockOutputStream(client.getOutputStream());
                try {
                    out.write(data);
                    out.close();
                    assertTrue(Arrays.equals(data, IOUtils.toByteArray(in)));
                    in.close();
                } finally {
                    IOUtils.closeQuietly(client);
                }
            }

        }, THREADS, "TcpServerTestWorker");
    }

    private static TcpClient getClient(String host, int port, boolean blocking) throws IOException {
        return blocking ? new SimpleTcpClient(host, port) : new NioTcpClient(host, port);
    }

}
