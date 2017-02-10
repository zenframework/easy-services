package org.zenframework.easyservices.test.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.zenframework.easyservices.test.TestContext;

import junit.framework.TestCase;

public class URLTest extends TestCase {

    private static final int PORT = TestContext.CONTEXT.getBean(int.class, "jettyPort");

    private Server server;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        server = new Server(PORT);
        server.setHandler(new AbstractHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse resp)
                    throws IOException, ServletException {
                Writer out = resp.getWriter();
                try {
                    if (req.getMethod().equalsIgnoreCase("GET")) {
                        out.write(req.getQueryString());
                    } else {
                        out.write(read(req.getReader()));
                    }
                } finally {
                    out.close();
                }
            }

        });
        server.start();
    }

    @Override
    public void tearDown() throws Exception {
        server.stop();
        super.tearDown();
    }

    public void testUrlGet() throws Exception {
        URL url = new URL("http://localhost:" + PORT + "/service?method=call&args=[]");
        assertEquals("method=call&args=[]", read(url.openStream()));
    }

    public void testUrlPost() throws Exception {
        URL url = new URL("http://localhost:" + PORT + "/service?method=call");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        Writer out = new OutputStreamWriter(conn.getOutputStream());
        out.write("[]");
        out.close();
        assertEquals("[]", read(conn.getInputStream()));
    }

    private static String read(InputStream in) throws IOException {
        try {
            byte[] buf = new byte[8192];
            StringBuilder str = new StringBuilder();
            for (int n = in.read(buf); n >= 0; n = in.read(buf))
                str.append(new String(buf, 0, n, "UTF-8"));
            return str.toString();
        } finally {
            in.close();
        }
    }

    private static String read(Reader in) throws IOException {
        try {
            char[] buf = new char[4096];
            StringBuilder str = new StringBuilder();
            for (int n = in.read(buf); n >= 0; n = in.read(buf))
                str.append(new String(buf, 0, n));
            return str.toString();
        } finally {
            in.close();
        }
    }

}
