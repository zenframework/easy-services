package org.zenframework.easyservices.test;

import java.util.Arrays;
import java.util.Collection;

import javax.naming.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zenframework.easyservices.ClientException;
import org.zenframework.easyservices.ClientFactory;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.jndi.JNDIHelper;

import junit.framework.TestCase;

@RunWith(Parameterized.class)
public abstract class AbstractServiceTest extends TestCase {

    @Parameterized.Parameters(name = "{index} autoAliasing:{0} format:{1}")
    public static Collection<Object[]> formats() {
        return Arrays.asList(new Object[][] { { true, "json" }, { true, "bin" }, { false, "json" }, { false, "bin" } });
    }

    private final boolean autoAliasing;
    private final String format;

    private HttpServer server = TestContext.CONTEXT.getBean(HttpServer.class);

    public AbstractServiceTest(boolean autoAliasing, String format) {
        this.autoAliasing = autoAliasing;
        this.format = format;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Environment.setAutoAliasing(autoAliasing);
        Environment.setDuplicateMethodNamesSafe(!autoAliasing);
        Environment.setDefaultFormat(format);
        server.start();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    protected static <T> T getClient(Class<T> serviceClass, String serviceName) throws ClientException {
        return TestContext.CONTEXT.getBean(ClientFactory.class).getClient(serviceClass, serviceName);
    }

    protected static Context getServiceRegistry() {
        return JNDIHelper.getDefaultContext();
    }

}
