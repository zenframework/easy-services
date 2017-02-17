package org.zenframework.easyservices.js.util;

import java.lang.reflect.Field;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JSUtil {

    private JSUtil() {}

    public static ScriptEngine getBestJavaScriptEngine() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("nashorn"); // nashorn
        if (engine == null)
            engine = factory.getEngineByName("JavaScript"); // rhino
        return engine;
    }

    public static Throwable findCause(Throwable e) {
        if (isNashornException(e))
            return e.getCause() != null ? e.getCause() : e;
        if (isRhinoException(e)) {
            return extractActualExceptionFromRhino(e);
        }
        return e;
    }

    public static boolean isRhinoException(Throwable e) {
        return e.getClass().getSimpleName().contains("JavaScript");
    }

    public static boolean isNashornException(Throwable e) {
        return e.getClass().getSimpleName().contains("ECMA");
    }

    private static Throwable extractActualExceptionFromRhino(Throwable e) {
        try {
            Field f = e.getClass().getDeclaredField("value");
            f.setAccessible(true);
            Object javascriptWrapper = f.get(e);
            Field javaThrowable = javascriptWrapper.getClass().getDeclaredField("javaObject");
            javaThrowable.setAccessible(true);
            Throwable t = (Throwable) javaThrowable.get(javascriptWrapper);
            return t;
        } catch (Exception e1) {
            throw new RuntimeException(e);
        }
    }

}
