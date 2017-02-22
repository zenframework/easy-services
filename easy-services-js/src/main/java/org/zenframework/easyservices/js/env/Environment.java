package org.zenframework.easyservices.js.env;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.IOUtils;

public class Environment {

    private static ThreadLocal<Environment> THREAD_LOCAL = new ThreadLocal<Environment>();
    private static List<Thread> THREADS = new LinkedList<Thread>();

    private final ScriptContext context = new SimpleScriptContext();
    private final ScriptEngine engine;

    public final Window window;

    public Environment(ScriptEngine engine, String uri) {
        this.engine = engine;
        this.window = new Window(new Location(uri));
    }

    public Object evalUrl(String url) throws ScriptException, IOException {
        return engine.eval(new InputStreamReader(openUrl(url)), context);
    }

    public Object evalScript(String script) throws ScriptException {
        return engine.eval(script, context);
    }

    public Object evalScript(Reader in) throws ScriptException {
        return engine.eval(in, context);
    }

    public Bindings getBindings() {
        return context.getBindings(ScriptContext.ENGINE_SCOPE);
    }

    public InputStream openUrl(String url) throws IOException {
        try {
            URI uri = new URI(url);
            if (uri.getScheme() == null)
                url = (window.location.origin != null ? window.location.origin : "file:/") + '/' + url;
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        return new URL(url).openStream();
    }

    public String readUrl(String url) throws IOException {
        InputStream in = openUrl(url);
        try {
            return IOUtils.toString(in, "UTF-8");
        } finally {
            in.close();
        }
    }

    public static Thread newThread(final Runnable run, final String name) {
        final Thread thread = new Thread(name) {

            @Override
            public void run() {
                try {
                    run.run();
                } finally {
                    synchronized (THREADS) {
                        THREADS.remove(this);
                    }
                }
            }

        };
        synchronized (THREADS) {
            THREADS.add(thread);
        }
        return thread;
    }

    public static void join() {
        for (Thread t = getFirstThread(); t != null; t = getFirstThread())
            try {
                t.join();
            } catch (InterruptedException e) {}
    }

    public static void setEnvironment(Environment env) {
        THREAD_LOCAL.set(env);
    }

    public static Environment getEnvironment() {
        return THREAD_LOCAL.get();
    }

    private static Thread getFirstThread() {
        synchronized (THREADS) {
            return THREADS.isEmpty() ? null : THREADS.get(0);
        }
    }

}
