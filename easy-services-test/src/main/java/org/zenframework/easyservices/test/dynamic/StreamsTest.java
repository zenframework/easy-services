package org.zenframework.easyservices.test.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.test.AbstractServiceTest;

@RunWith(Parameterized.class)
public class StreamsTest extends AbstractServiceTest {

    private static final int SIZE_K = 1024;

    @Parameterized.Parameters(name = "{index} autoAliasing: {0}, format: {1}, size: {2}K")
    public static Collection<Object[]> formats() {
        return Arrays
                .asList(new Object[][] { /*{ true, "json", SIZE_K }, { true, "bin", SIZE_K },*/ { false, "json", SIZE_K }/*, { false, "bin", SIZE_K }*/ });
    }

    private final int size;

    private Registry registry;
    private File sourceFile;
    private File targetFile;

    public StreamsTest(boolean autoAliasing, String format, int size) {
        Environment.setAutoAliasing(autoAliasing);
        Environment.setDuplicateMethodNamesSafe(!autoAliasing);
        Environment.setSerializationFormat(format);
        Environment.setOutParametersMode(true);
        this.size = size * 1024;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceFile = createTestFile(File.createTempFile("easy-services-streams-test", ".in"), size);
        targetFile = File.createTempFile("easy-services-streams-test", ".out");
        StreamFactory factory = new StreamFactoryImpl(sourceFile, targetFile);
        getServiceRegistry().bind("/streams", factory);
        getRegistry().rebind("/streams", factory);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("/streams");
        getRegistry().unbind("/streams");
        closeRegistry();
        sourceFile.delete();
        targetFile.delete();
    }

    @Test
    public void testEasyServicesStreams() throws Exception {
        StreamFactory streams = getClient(StreamFactory.class, "/streams");
        InputStream in = streams.getInputStream();
        OutputStream out = streams.getOutputStream();
        copy(in, out);
        try {
            in.read();
            fail("read should fail");
        } catch (ServiceException e) {}
        try {
            out.write(0);
            fail("write should fail");
        } catch (ServiceException e) {}
        assertTrue(equals(sourceFile, targetFile));
    }

    @Test
    public void testLocalStreams() throws Exception {
        StreamFactory streams = (StreamFactory) getServiceRegistry().lookup("/streams");
        InputStream in = streams.getInputStream();
        OutputStream out = streams.getOutputStream();
        copy(in, out);
        assertTrue(equals(sourceFile, targetFile));
    }

    @Test
    public void testRmiStreams() throws Exception {
        StreamFactory streams = (StreamFactory) getRegistry().lookup("/streams");
        RmiInputStream in = streams.getRmiInputStream();
        RmiOutputStream out = streams.getRmiOutputStream();
        copy(in, out);
        assertTrue(equals(sourceFile, targetFile));
    }

    private Registry getRegistry() throws RemoteException {
        if (registry == null)
            registry = LocateRegistry.createRegistry(12345);
        return registry;
    }

    private void closeRegistry() throws RemoteException {
        if (registry != null) {
            UnicastRemoteObject.unexportObject(registry, true);
            registry = null;
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            IOUtils.copy(in, out);
        } finally {
            IOUtils.closeQuietly(in, out);
        }
    }

    private static void copy(RmiInputStream in, RmiOutputStream out) throws IOException {
        try {
            for (byte[] buf = in.read(8192); buf != null; buf = in.read(8192))
                out.write(buf);
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
