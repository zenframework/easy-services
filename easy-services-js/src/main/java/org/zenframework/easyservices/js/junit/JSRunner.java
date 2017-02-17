package org.zenframework.easyservices.js.junit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.zenframework.easyservices.js.env.Environment;
import org.zenframework.easyservices.js.util.JSUtil;

public class JSRunner extends Runner implements Filterable, Sortable {

    private final Class<?> cls;
    private final JSTestSuite testSuite;
    private final List<JSTestCase> testCases;

    public JSRunner(Class<?> cls) {
        this.cls = cls;
        testSuite = getTestSuite(cls);
        if (testSuite != null)
            testSuite.init();
        JSTests tests = cls.getAnnotation(JSTests.class);
        List<String> testNames = tests != null ? Arrays.asList(tests.value()) : Collections.<String> emptyList();
        testCases = findJSTests(testNames);
    }

    @Override
    public Description getDescription() {
        Description suite = Description.createSuiteDescription(cls);
        for (JSTestCase testCase : testCases) {
            List<JSTest> tests = testCase.getTests();
            Description desc = Description.createTestDescription(testCase.getJUnitName(), testCase.getJUnitName());
            suite.addChild(desc);
            for (JSTest test : tests) {
                Description methodDesc = Description.createTestDescription(testCase.getJUnitName(), test.getName());
                desc.addChild(methodDesc);
            }
        }
        return suite;
    }

    @Override
    public void run(RunNotifier notifier) {
        try {
            for (JSTestCase testCase : testCases) {
                Environment.setEnvironment(testCase.env);
                List<JSTest> tests = testCase.getTests();
                for (JSTest test : tests) {
                    Description desc = Description.createTestDescription(testCase.getJUnitName(), test.getName());
                    notifier.fireTestStarted(desc);
                    try {
                        if (testSuite != null)
                            testSuite.setUp();
                        test.run();
                        notifier.fireTestFinished(desc);
                    } catch (Throwable e) {
                        notifier.fireTestFailure(new Failure(desc, JSUtil.findCause(e)));
                    } finally {
                        try {
                            if (testSuite != null)
                                testSuite.tearDown();
                        } catch (Exception e) {
                            notifier.fireTestFailure(new Failure(desc, e));
                        }
                    }
                }
            }
        } finally {
            if (testSuite != null)
                testSuite.cleanUp();
        }
    }

    @Override
    public void sort(Sorter sorter) {
        //
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        //
    }

    private static JSTestSuite getTestSuite(Class<?> cls) {
        JSTestSuite testSuite = null;
        if (JSTestSuite.class.isAssignableFrom(cls)) {
            try {
                testSuite = (JSTestSuite) cls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return testSuite;
    }

    private static List<JSTestCase> findJSTests(List<String> testNames) {
        try {
            ScriptEngine engine = JSUtil.getBestJavaScriptEngine();
            List<JSTestCase> testCases = new ArrayList<JSTestCase>();
            for (String name : testNames)
                testCases.add(loadTestCase(engine, name));
            return testCases;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static JSTestCase loadTestCase(ScriptEngine engine, String url) throws ScriptException, IOException {
        Environment env = new Environment(engine, url);
        Environment.setEnvironment(env);
        env.evalUrl("classpath://script/env/Environment.js");
        env.evalUrl("classpath://script/junit/TestUtils.js");
        List<JSTest> tests = (List<JSTest>) env.evalUrl(url);
        return new JSTestCase(url, env, tests);
    }

}