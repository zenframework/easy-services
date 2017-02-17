package org.zenframework.easyservices.js.junit;

import java.util.List;

import org.zenframework.easyservices.js.env.Environment;

public class JSTestCase {

    public final String name;
    public final Environment env;
    public final List<JSTest> tests;

    public JSTestCase(String name, Environment env, List<JSTest> tests) {
        this.name = name;
        this.env = env;
        this.tests = tests;
    }

    public String getJUnitName() {
        return name; //name.replaceAll("(.*)\\.(.*)", "$2.$1");
    }

    public String getName() {
        return name;
    }

    public List<JSTest> getTests() {
        return tests;
    }

}