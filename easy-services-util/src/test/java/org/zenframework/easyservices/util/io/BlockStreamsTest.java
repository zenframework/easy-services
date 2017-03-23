package org.zenframework.easyservices.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

public class BlockStreamsTest extends TestCase {

    private static final int DATA_SIZE = 100000;
    private static final int BLOCK_SIZE = 8192;
    private static final int SMALL_BUF_SIZE = 222;
    private static final int LARGE_BUF_SIZE = 15000;

    private static final Random RANDOM = new Random();

    private final byte[] source = new byte[DATA_SIZE];
    private byte[] data;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(DATA_SIZE * 2);
        OutputStream out = new BlockOutputStream(bytes, BLOCK_SIZE);
        for (int i = 0; i < DATA_SIZE; i++) {
            source[i] = (byte) RANDOM.nextInt(255);
            out.write(source[i]);
        }
        out.close();
        data = bytes.toByteArray();
    }

    public void testBlockStreamsByByte() throws Exception {
        InputStream in = new BlockInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream(DATA_SIZE);
        for (int c = in.read(); c >= 0; c = in.read())
            out.write(c);
        out.close();
        in.close();
        assertTrue(Arrays.equals(source, out.toByteArray()));
    }

    public void testBlockStreamsSmallBuf() throws Exception {
        InputStream in = new BlockInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream(DATA_SIZE);
        byte[] buf = new byte[SMALL_BUF_SIZE];
        for (int n = in.read(buf); n >= 0; n = in.read(buf))
            out.write(buf, 0, n);
        out.close();
        in.close();
        assertTrue(Arrays.equals(source, out.toByteArray()));
    }

    public void testBlockStreamsLargeBuf() throws Exception {
        InputStream in = new BlockInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream(DATA_SIZE);
        byte[] buf = new byte[LARGE_BUF_SIZE];
        for (int n = in.read(buf); n >= 0; n = in.read(buf))
            out.write(buf, 0, n);
        out.close();
        in.close();
        assertTrue(Arrays.equals(source, out.toByteArray()));
    }

    @SuppressWarnings("resource")
    public void testBlockStreamsOverSocket() throws Exception {
        final ServerSocket server = new ServerSocket(10000);
        new Thread("Server") {

            @Override
            public void run() {
                try {
                    final Socket client = server.accept();
                    new Thread("Client") {

                        @Override
                        public void run() {
                            try {
                                for (int i = 0; i < 10; i++) {
                                    ObjectInputStream in = new ObjectInputStream(new BlockInputStream(client.getInputStream()));
                                    ObjectOutputStream out = new ObjectOutputStream(new BlockOutputStream(client.getOutputStream(), BLOCK_SIZE));
                                    out.writeObject(in.readObject());
                                    in.close();
                                    out.close();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                IOUtils.closeQuietly(client);
                            }
                        }

                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }.start();

        Socket socket = new Socket("localhost", 10000);
        try {
            for (int i = 0; i < 10; i++) {
                ObjectOutputStream out = new ObjectOutputStream(new BlockOutputStream(socket.getOutputStream(), BLOCK_SIZE));
                out.writeObject(source);
                out.close();
                ObjectInputStream in = new ObjectInputStream(new BlockInputStream(socket.getInputStream()));
                byte[] echo = (byte[]) in.readObject();
                in.close();
                assertTrue(Arrays.equals(source, echo));
            }
        } finally {
            socket.close();
        }
    }

}
