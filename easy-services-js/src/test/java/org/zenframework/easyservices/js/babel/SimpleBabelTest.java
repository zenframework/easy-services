package org.zenframework.easyservices.js.babel;

import junit.framework.TestCase;

public class SimpleBabelTest extends TestCase {

    public void testTransform() throws Exception {
        Babel babel = new SimpleBabel();
        String source = "<Hello s='abc' i={1} />";
        String transformed = babel.transform(source, Babel.PRESET_REACT);
        assertEquals("React.createElement(Hello, { s: 'abc', i: 1 });", transformed);
    }

}
