package org.zenframework.easyservices.js.export.lib;

import org.zenframework.easyservices.js.HttpServer;
import org.zenframework.easyservices.js.junit.JSTestSuite;
import org.zenframework.easyservices.js.junit.JSTests;

@JSTests(value = { "classpath://export/lib/require/RequireTest.js", "http://localhost:10000/lib/require/RequireTest.js" })
public class RequireTest extends JSTestSuite {

    private final HttpServer server = new HttpServer();

    @Override
    protected void init() {
        super.setUp();
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void cleanUp() {
        super.tearDown();
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
