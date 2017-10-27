package org.zenframework.easyservices.test.dynamic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.junit.Test;
import org.zenframework.easyservices.test.TestUtil;

import junit.framework.TestCase;

public class StreamsBenchmark extends TestCase {

    private Registry registry;
    private StreamFactory[] streams;
    private File[] sourceFiles;
    private File[] targetFiles;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        streams = new StreamFactory[StreamsTest.THREADS];
        sourceFiles = new File[StreamsTest.THREADS];
        targetFiles = new File[StreamsTest.THREADS];
        for (int i = 0; i < StreamsTest.THREADS; i++) {
            sourceFiles[i] = StreamsUtil.createTestFile("easy-services-streams-test-" + i, ".in", StreamsTest.SIZE_K * 1024);
            targetFiles[i] = StreamsUtil.getTempFile("easy-services-streams-test-" + i, ".out");
            streams[i] = new StreamFactoryImpl(sourceFiles[i], targetFiles[i]);
            getRmiRegistry().rebind("streams-" + i, streams[i]);
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        for (int i = 0; i < StreamsTest.THREADS; i++) {
            getRmiRegistry().unbind("streams-" + i);
            sourceFiles[i].delete();
            targetFiles[i].delete();
        }
        closeRegistry();
    }

    @Test
    public void testLocalStreams() throws Exception {
        TestUtil.runMultiThreads(new TestUtil.Runnable() {

            @Override
            public void run(int n) throws IOException {
                InputStream in = streams[n].getInputStream();
                OutputStream out = streams[n].getOutputStream();
                StreamsUtil.copy(in, out);
                assertTrue(StreamsUtil.equals(sourceFiles[n], targetFiles[n]));
            }

        }, StreamsTest.THREADS);
        for (int i = 0; i < StreamsTest.THREADS; i++)
            assertTrue(StreamsUtil.equals(sourceFiles[i], targetFiles[i]));
    }

    @Test
    public void testRmiStreams() throws Exception {
        TestUtil.runMultiThreads(new TestUtil.Runnable() {

            @Override
            public void run(int n) throws IOException, NotBoundException {
                StreamFactory streams = (StreamFactory) getRmiRegistry().lookup("streams-" + n);
                RmiInputStream in = streams.getRmiInputStream();
                RmiOutputStream out = streams.getRmiOutputStream();
                StreamsUtil.copy(in, out);
            }

        }, StreamsTest.THREADS);
        for (int i = 0; i < StreamsTest.THREADS; i++)
            assertTrue(StreamsUtil.equals(sourceFiles[i], targetFiles[i]));
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
