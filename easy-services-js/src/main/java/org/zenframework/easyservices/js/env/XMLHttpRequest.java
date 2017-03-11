package org.zenframework.easyservices.js.env;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.debug.TimeChecker;

public class XMLHttpRequest {

    private static final Logger LOG = LoggerFactory.getLogger(XMLHttpRequest.class);

    public static final int STATE_UNINITIALIZED = 0;
    public static final int STATE_LOADING = 1; // not used
    public static final int STATE_LOADED = 2; // not used
    public static final int STATE_INTERACTIVE = 3; // not used
    public static final int STATE_COMPLETE = 4;

    public volatile Runnable onreadystatechange = null;
    public volatile int readyState = STATE_UNINITIALIZED;
    public volatile String responseText = null;
    public volatile String responseXML = null;
    public volatile int status = 0;
    public volatile String statusText = null;

    private Request request;
    private Thread runner = null;

    public void open(final Object method, final Object url) {
        open(method, url, true, null, null);
    }

    public void open(final Object method, final Object url, final Object async) {
        open(method, url, async, null, null);
    }

    public void open(final Object method, final Object url, final Object async, Object userName) {
        open(method, url, async, userName, null);
    }

    public void open(Object method, Object url, Object async, Object userName, Object password) {
        if (method == null || url == null)
            throw new IllegalArgumentException(getClass().getSimpleName() + ".open(): Method and URL required");
        if (!method.toString().toUpperCase().equals("GET"))
            throw new IllegalArgumentException("GET method supported only");
        request = new Request(method.toString(), url.toString(), toBoolean(async), toString(userName), toString(password));
    }

    public void send(String body) {
        final TimeChecker time = LOG.isDebugEnabled() ? new TimeChecker("GET " + request.url + ", " + (request.async ? "async" : "sync"), LOG) : null;
        final Environment env = Environment.getEnvironment();
        Runnable run = new Runnable() {

            @Override
            public void run() {
                try {
                    InputStream in = env.openUrl(request.url);
                    try {
                        readyState = STATE_LOADED;
                        if (onreadystatechange != null)
                            onreadystatechange.run();
                        responseText = IOUtils.toString(in, "UTF-8");
                        readyState = STATE_COMPLETE;
                        if (onreadystatechange != null)
                            onreadystatechange.run();
                    } finally {
                        if (time != null)
                            time.printDifference(responseText);
                        in.close();
                    }
                } catch (IOException e) {
                    status = 500;
                    responseText = e.getMessage();
                    readyState = STATE_COMPLETE;
                    if (onreadystatechange != null)
                        onreadystatechange.run();
                    throw new RuntimeException(e);
                }
            }

        };
        if (request.async) {
            Environment.newThread(run, request.method.toUpperCase() + '-' + request.url).start();
        } else {
            run.run();
        }
    }

    public void send() {
        send(null);
    }

    public void abort() {
        if (runner != null) {
            runner.interrupt();
            runner = null;
        }
    }

    /**
     * Устанавливает заголовок name запроса со значением value. Если заголовок с таким name уже есть - он заменяется. Например,
     * xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded')
     * getAllResponseHeaders()
     * Возвращает строку со всеми HTTP-заголовками ответа сервера.
     **/
    public void setRequestHeader(String name, String value) {
        // TODO setRequestHeader(name, value)
    }

    /**
     * Возвращает значение заголовка ответа сервера с именем headerName.
     */
    public String getResponseHeader(String headerName) {
        return "";
    }

    private static class Request {

        final String method;
        final String url;
        final boolean async;
        @SuppressWarnings("unused")
        final String userName;
        @SuppressWarnings("unused")
        final String password;

        public Request(String method, String url, boolean async, String userName, String password) {
            this.method = method;
            this.url = url;
            this.async = async;
            this.userName = userName;
            this.password = password;
        }

    }

    private static boolean toBoolean(Object o) {
        if (o == null)
            return false;
        if (o instanceof Boolean)
            return (Boolean) o;
        if (o instanceof Number)
            return ((Number) o).intValue() != 0;
        return Boolean.parseBoolean(o.toString());
    }

    private static String toString(Object o) {
        return o == null ? null : o.toString();
    }

}
