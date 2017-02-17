package org.zenframework.easyservices.js.junit;

@JSTests({
    "classpath://export/junit/ExampleTestOne.js", 
    "classpath://export/junit/ExampleTestTwo.js",
    "classpath://export/junit/TestFileUnderTest.js"
})
public class JSRunnerTest extends JSTestSuite {

}
