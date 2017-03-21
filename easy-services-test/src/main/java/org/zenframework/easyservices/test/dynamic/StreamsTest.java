package org.zenframework.easyservices.test.dynamic;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.test.AbstractServiceTest;
import org.zenframework.easyservices.util.CollectionUtil;

@RunWith(Parameterized.class)
public class StreamsTest extends AbstractServiceTest {

    public static final int SIZE_K = 1024;

    @Parameterized.Parameters(name = "#{index} protocol/format: {0}, secure: {1}, autoAliasing: {2}, size: {3}K")
    public static Collection<Object[]> params() {
        return CollectionUtil.combinations(arr("http/json", "tcp/bin"), arr(false, true), arr(true, false), arr(SIZE_K));
    }

    private final int size;

    private File sourceFile;
    private File targetFile;

    public StreamsTest(String protocolFormat, boolean securityEnabled, boolean autoAliasing, int size) {
        super(protocolFormat);
        Environment.setSecurityEnabled(securityEnabled);
        Environment.setAutoAliasing(autoAliasing);
        Environment.setDuplicateMethodNamesSafe(!autoAliasing);
        Environment.setOutParametersMode(true);
        this.size = size * 1024;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sourceFile = StreamsUtil.createTestFile("easy-services-streams-test", ".in", size);
        targetFile = StreamsUtil.getTempFile("easy-services-streams-test", ".out");
        getServiceRegistry().bind("streams", new StreamFactoryImpl(sourceFile, targetFile));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("streams");
        sourceFile.delete();
        targetFile.delete();
    }

    @Test
    public void testEasyServicesStreams() throws Exception {
        StreamFactory streams = getClient(StreamFactory.class, "streams");
        InputStream in = streams.getInputStream();
        OutputStream out = streams.getOutputStream();
        StreamsUtil.copy(in, out);
        try {
            in.read();
            fail("read should fail");
        } catch (ServiceException e) {}
        try {
            out.write(0);
            fail("write should fail");
        } catch (ServiceException e) {}
        assertTrue(StreamsUtil.equals(sourceFile, targetFile));
    }

}
