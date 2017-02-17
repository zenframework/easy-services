package org.zenframework.easyservices.test.dynamic;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.test.AbstractServiceTest;
import org.zenframework.easyservices.test.simple.Addition;
import org.zenframework.easyservices.test.simple.Function;
import org.zenframework.easyservices.test.simple.Substraction;

@RunWith(Parameterized.class)
public class DynamicTest extends AbstractServiceTest {

    @Parameterized.Parameters(name = "{index} format:{0}")
    public static Collection<Object[]> formats() {
        return Arrays.asList(new Object[][] { { "json" }, { "bin" } });
    }

    public DynamicTest(String format) {
        Environment.setDefaultFormat(format);
    }

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

    @Test
    public void testDynamic() throws Exception {
        Calculator calc = getClient(Calculator.class, "/calc");
        Function add = calc.getFunction("add");
        Function sub = calc.getFunction("sub");
        assertEquals(3, calc.call(add, 1, 2));
        assertEquals(1, calc.call(sub, 3, 2));
    }

}
