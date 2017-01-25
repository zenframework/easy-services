package org.zenframework.easyservices.test.dynamic;

import org.zenframework.easyservices.test.AbstractServiceTest;
import org.zenframework.easyservices.test.simple.Addition;
import org.zenframework.easyservices.test.simple.Function;
import org.zenframework.easyservices.test.simple.Substraction;

public class DynamicTest extends AbstractServiceTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CalculatorImpl functions = new CalculatorImpl();
        functions.getFunctions().put("add", new Addition());
        functions.getFunctions().put("sub", new Substraction());
        getServiceRegistry().bind("/calc", functions);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("/calc");
    }

    public void testDynamic() throws Exception {
        Calculator calc = getClient(Calculator.class, "/calc");
        Function add = calc.getFunction("add");
        Function sub = calc.getFunction("sub");
        assertEquals(3, add.call(1, 2));
        assertEquals(1, sub.call(3, 2));
        //assertEquals(3, calc.call(add, 1, 2));
        //assertEquals(1, calc.call(sub, 3, 2));
    }

}
