package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

import junit.framework.TestCase;

@RunWith(Parameterized.class)
public class TcpServerTest extends TestCase {

    private static final int DATA_SIZE = 150000;
    private static final int PORT = 9000;
    private static final int THREADS = 10;

    @Parameterized.Parameters(name = "#{index} blocking: {0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[] { true }, new Object[] { false });
    }

    private final byte[] source = new byte[DATA_SIZE];
    private final boolean blocking;
    private TcpServer server;

    public TcpServerTest(boolean blocking) {
        this.blocking = blocking;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        for (int i = 0; i < DATA_SIZE; i++)
            source[i] = (byte) (i % 255);
        server = blocking ? new SimpleTcpServer(PORT) : new NioTcpServer(PORT);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testEchoServer() throws Exception {
        server.setRequestHandler(new TcpRequestHandler() {

            @Override
            public boolean handleRequest(SocketAddress clientAddr, InputStream in, OutputStream out) throws IOException {
                System.out.println("Server: start request from " + clientAddr);
                in = new BlockInputStream(in);
                out = new BlockOutputStream(out);
                try {
                    System.out.println("Server: start reading ...");
                    String str = read(in);
                    System.out.println("Server: read '" + str + "'");
                    out.write(str.getBytes());
                    System.out.println("Server: write '" + str + "'");
                } finally {
                    System.out.println("Server: close streams ...");
                    IOUtils.closeQuietly(in, out);
                    System.out.println("Server: streams closed");
                }
                return false;
            }

        });
        server.start();
        
        Thread[] workers = new Thread[THREADS];
        final List<Throwable> errors = Collections.synchronizedList(new LinkedList<Throwable>());

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(new Runnable() {
        
                @Override
                public void run() {
                    Socket socket = null;
                    try {
                        socket = new Socket("localhost", 9000);
                        String data = "hello-" + UUID.randomUUID();
                        InputStream in = new BlockInputStream(socket.getInputStream());
                        OutputStream out = new BlockOutputStream(socket.getOutputStream());
                        System.out.println("Client: write data");
                        out.write(data.getBytes());
                        System.out.println("Client: closing out");
                        out.close();
                        System.out.println("Client: out closed. Start reading ...");
                        String str = read(in);
                        System.out.println("Client: read '" + str + "'. Closing in ...");
                        in.close();
                        System.out.println("Client: in closed");
                        assertTrue(str.equals(data));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        errors.add(e);
                    } finally {
                        IOUtils.closeQuietly(socket);
                    }
                }
                
            }, "TcpServerTestWorker-" + i);
            workers[i].start();
        }
        
        for (int i = 0; i < workers.length; i++)
            workers[i].join();

        server.stop();
        
        if (errors.size() > 0)
            fail("Some error has happened");
    }

    private static String read(InputStream in) throws IOException {
        StringBuilder str = new StringBuilder();
        byte[] buf = new byte[8192];
        for (int n = in.read(buf); n >= 0; n = in.read(buf))
            str.append(new String(buf, 0, n));
        return str.toString();
    }

}
