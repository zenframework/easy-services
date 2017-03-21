package org.zenframework.easyservices.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

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
        in.close();
        out.close();
        assertTrue(Arrays.equals(source, out.toByteArray()));
    }

    public void testBlockStreamsSmallBuf() throws Exception {
        InputStream in = new BlockInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream(DATA_SIZE);
        byte[] buf = new byte[SMALL_BUF_SIZE];
        for (int n = in.read(buf); n >= 0; n = in.read(buf))
            out.write(buf, 0, n);
        in.close();
        out.close();
        assertTrue(Arrays.equals(source, out.toByteArray()));
    }

    public void testBlockStreamsLargeBuf() throws Exception {
        InputStream in = new BlockInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream out = new ByteArrayOutputStream(DATA_SIZE);
        byte[] buf = new byte[LARGE_BUF_SIZE];
        for (int n = in.read(buf); n >= 0; n = in.read(buf))
            out.write(buf, 0, n);
        in.close();
        out.close();
        assertTrue(Arrays.equals(source, out.toByteArray()));
    }

}
