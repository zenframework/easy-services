package org.zenframework.easyservices.test.dynamic;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.junit.Test;

import junit.framework.TestCase;

public class StreamsBenchmark extends TestCase {

    private StreamFactory streams;
    private Registry registry;
    private File sourceFile;
    private File targetFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceFile = StreamsUtil.createTestFile(File.createTempFile("easy-services-streams-test", ".in"), StreamsTest.SIZE_K * 1024);
        targetFile = File.createTempFile("easy-services-streams-test", ".out");
        streams = new StreamFactoryImpl(sourceFile, targetFile);
        getRmiRegistry().rebind("streams", streams);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getRmiRegistry().unbind("streams");
        closeRegistry();
        sourceFile.delete();
        targetFile.delete();
    }

    @Test
    public void testLocalStreams() throws Exception {
        InputStream in = streams.getInputStream();
        OutputStream out = streams.getOutputStream();
        StreamsUtil.copy(in, out);
        assertTrue(StreamsUtil.equals(sourceFile, targetFile));
    }

    @Test
    public void testRmiStreams() throws Exception {
        StreamFactory streams = (StreamFactory) getRmiRegistry().lookup("streams");
        RmiInputStream in = streams.getRmiInputStream();
        RmiOutputStream out = streams.getRmiOutputStream();
        StreamsUtil.copy(in, out);
        assertTrue(StreamsUtil.equals(sourceFile, targetFile));
    }

    private Registry getRmiRegistry() throws RemoteException {
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

}
