package org.zenframework.easyservices.test.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.easyservices.test.simple.Function;

public class CalculatorImpl implements Calculator {

    private final Map<String, Function> functions = new HashMap<String, Function>();

    @Override
    public Function getFunction(String name) {
        return getFunctions().get(name);
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    public void setFunctions(Map<String, Function> functions) {
        this.functions.clear();
        this.functions.putAll(functions);
    }

    @Override
    public int call(Function function, int a, int b) {
        return function.call(a, b);
    }

}
