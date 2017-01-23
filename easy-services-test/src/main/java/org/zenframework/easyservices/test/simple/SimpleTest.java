package org.zenframework.easyservices.test.simple;

import org.zenframework.easyservices.test.AbstractServiceTest;

public class SimpleTest extends AbstractServiceTest {

    private static final String CONTEXT = "classpath:simple-context.xml";

    public SimpleTest() {
        super(CONTEXT);
    }

    public void testSimpleServices() throws Exception {
        assertEquals(3, getClient(Function.class, "/add").call(1, 2));
        assertEquals(1, getClient(Function.class, "/sub").call(3, 2));
    }

}
