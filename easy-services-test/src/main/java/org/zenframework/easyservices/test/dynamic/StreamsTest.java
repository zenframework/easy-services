package org.zenframework.easyservices.test.dynamic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.test.AbstractServiceTest;
import org.zenframework.easyservices.util.CollectionUtil;
import org.zenframework.easyservices.util.ThreadUtil;
import org.zenframework.easyservices.util.debug.TimeChecker;
import org.zenframework.easyservices.util.thread.Task;

@RunWith(Parameterized.class)
public class StreamsTest extends AbstractServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(StreamsTest.class);

    public static final int SIZE_K = 64;
    public static final int THREADS = 16;

    @Parameterized.Parameters(name = "#{index} protocol/format: {0}, secure: {1}, autoAliasing: {2}, size: {3}K, threads: {4}")
    public static Collection<Object[]> params() {
        return CollectionUtil.combinations(arr("http/json", "tcp/bin", "tcpx/bin"), arr(false, true), arr(true, false), arr(SIZE_K), arr(THREADS));
    }

    private final int size;
    private final int threads;

    private File[] sourceFiles;
    private File[] targetFiles;

    public StreamsTest(String protocolFormat, boolean securityEnabled, boolean autoAliasing, int size, int threads) {
        super(protocolFormat);
        Environment.setSecurityEnabled(securityEnabled);
        Environment.setAutoAliasing(autoAliasing);
        Environment.setDuplicateMethodNamesSafe(!autoAliasing);
        Environment.setOutParametersMode(true);
        this.size = size * 1024;
        this.threads = threads;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceFiles = new File[threads];
        targetFiles = new File[threads];
        for (int i = 0; i < threads; i++) {
            sourceFiles[i] = StreamsUtil.createTestFile("easy-services-streams-test-" + i, ".in", size);
            targetFiles[i] = StreamsUtil.getTempFile("easy-services-streams-test-" + i, ".out");
            getServiceRegistry().bind("streams-" + i, new StreamFactoryImpl(sourceFiles[i], targetFiles[i]));
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        for (int i = 0; i < threads; i++) {
            getServiceRegistry().unbind("streams-" + i);
            sourceFiles[i].delete();
            targetFiles[i].delete();
        }
    }

    @Test
    public void testEasyServicesStreams() throws Exception {
        ThreadUtil.runMultiThreadTask(new Task() {

            @Override
            public void run(int n) throws IOException {
                StreamFactory streams = getClient(StreamFactory.class, "streams-" + n);
                InputStream in = streams.getInputStream();
                OutputStream out = streams.getOutputStream();
                TimeChecker time = new TimeChecker("TEST EASY-SERVICES STREAMS (" + Thread.currentThread().getName() + ")", LOG);
                StreamsUtil.copy(in, out);
                time.printDifference();
                try {
                    in.read();
                    fail("read should fail");
                } catch (Exception e) {}
                try {
                    out.write(0);
                    fail("write should fail");
                } catch (Exception e) {}
            }

        }, threads, "TestWorker");
        for (int i = 0; i < threads; i++)
            assertTrue(StreamsUtil.equals(sourceFiles[i], targetFiles[i]));
    }

}
