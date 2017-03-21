package org.zenframework.easyservices.js.babel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.Configurable;
import org.zenframework.easyservices.js.util.JSUtil;
import org.zenframework.easyservices.util.StringUtil;
import org.zenframework.easyservices.util.debug.TimeChecker;

public class SimpleBabel implements Babel, Configurable {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleBabel.class);

    private static final String BABEL = "script/babel/babel.min.js";

    private final ScriptEngine engine = JSUtil.getBestJavaScriptEngine();

    public SimpleBabel() {
        try {
            init(new InputStreamReader(Babel.class.getClassLoader().getResourceAsStream(BABEL)));
        } catch (Exception e) {
            throw new RuntimeException("Can't initialize Babel", e);
        }
    }

    @Override
    public void init(Config config) {}

    @Override
    public void destroy(Config config) {}

    @Override
    public String transform(String script, String... presets) throws ScriptException {
        TimeChecker time = null;
        if (LOG.isDebugEnabled())
            time = new TimeChecker("TRANSFORM " + StringUtils.left(script, 50).replace("\r", "").replace("\n", " ") + " ...", LOG);
        StringBuilder str = new StringBuilder(script.length() + 100);
        str.append("Babel.transform(\"").append(StringUtil.escape(script)).append("\", { presets: [");
        for (String preset : presets)
            str.append("'").append(preset).append("', ");
        if (presets.length > 0)
            str.setLength(str.length() - 2);
        str.append("] }).code");
        String result = engine.eval(str.toString()).toString();
        if (time != null)
            time.printDifference();
        return result;
    }

    private void init(Reader babelReader) throws ScriptException, IOException {
        TimeChecker time = null;
        if (LOG.isDebugEnabled())
            time = new TimeChecker("INIT " + this, LOG);
        try {
            engine.eval(babelReader);
        } finally {
            babelReader.close();
            if (time != null)
                time.printDifference();
        }
    }

}
