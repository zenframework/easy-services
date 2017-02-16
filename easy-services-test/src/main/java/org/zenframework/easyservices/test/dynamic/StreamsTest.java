package org.zenframework.easyservices.test.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.zenframework.easyservices.test.AbstractServiceTest;

public class StreamsTest extends AbstractServiceTest {

    private File sourceFile;
    private File targetFile;

    public StreamsTest(boolean autoAliasing, String format) {
        super(autoAliasing, format);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceFile = createTestFile(File.createTempFile("easy-services-streams-test", ".in"), 1024 * 1024);
        targetFile = File.createTempFile("easy-services-streams-test", ".out");
        getServiceRegistry().bind("/streams", new StreamFactoryImpl(sourceFile, targetFile));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("/streams");
        sourceFile.delete();
        targetFile.delete();
    }

    @Test
    public void testRemoteStreams() throws Exception {
        StreamFactory streams = getClient(StreamFactory.class, "/streams");
        InputStream in = streams.getInputStream();
        OutputStream out = streams.getOuptputStream();
        copy(in, out);
        assertTrue(equals(sourceFile, targetFile));
    }

    @Test
    public void testLocalStreams() throws Exception {
        StreamFactory streams = (StreamFactory) getServiceRegistry().lookup("/streams");
        InputStream in = streams.getInputStream();
        OutputStream out = streams.getOuptputStream();
        copy(in, out);
        assertTrue(equals(sourceFile, targetFile));
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in, out);
        }
    }

    private static File createTestFile(File file, int size) throws IOException {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[8192];
        try {
            for (int i = 0; i < size / 8192; i++) {
                for (int j = 0; j < 8192; j++)
                    buf[j] = (byte) (j % 256);
                out.write(buf);
            }
            if (size % 8192 > 0) {
                for (int j = 0; j < size % 8192; j++)
                    buf[j] = (byte) (j % 256);
                out.write(buf, 0, size % 8192);
            }
        } finally {
            out.close();
        }
        return file;
    }

    private static boolean equals(File f1, File f2) throws IOException {
        InputStream in1 = new FileInputStream(f1);
        InputStream in2 = new FileInputStream(f2);
        try {
            return IOUtils.contentEquals(in1, in2);
        } finally {
            IOUtils.closeQuietly(in1, in2);
        }
    }

}
