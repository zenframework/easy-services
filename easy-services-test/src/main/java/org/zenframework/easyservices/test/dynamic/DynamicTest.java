package org.zenframework.easyservices.test.dynamic;

import java.util.Collection;

import javax.naming.NamingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.ServiceException;
import org.zenframework.easyservices.test.AbstractServiceTest;
import org.zenframework.easyservices.test.simple.Addition;
import org.zenframework.easyservices.test.simple.Function;
import org.zenframework.easyservices.test.simple.Substraction;
import org.zenframework.easyservices.util.CollectionUtil;
import org.zenframework.easyservices.util.JNDIUtil;

@RunWith(Parameterized.class)
public class DynamicTest extends AbstractServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicTest.class);

    @Parameterized.Parameters(name = "#{index} protocol/format: {0}, secure: {1}")
    public static Collection<Object[]> params() {
        return CollectionUtil.combinations(arr("http/json", "tcp/bin"), arr(true, false));
    }

    public DynamicTest(String protocolFormat, boolean securityEnabled) {
        super(protocolFormat);
        Environment.setSecurityEnabled(securityEnabled);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        CalculatorImpl functions = new CalculatorImpl();
        functions.getFunctions().put("add", new Addition());
        functions.getFunctions().put("sub", new Substraction());
        getServiceRegistry().bind("calc", functions);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("calc");
    }

    @Test
    public void testCallDynamic() throws Exception {
        Calculator calc = getClient(Calculator.class, "calc");
        Function add = calc.getFunction("add");
        Function sub = calc.getFunction("sub");
        assertEquals(3, calc.call(add, 1, 2));
        assertEquals(1, calc.call(sub, 3, 2));
    }

    @Test
    public void testCloseByParam() throws Exception {
        logContext("REGISTRY after init");
        Calculator calc = getClient(Calculator.class, "calc");
        Function add = calc.getFunction("add");
        logContext("REGISTRY after get dynamic");
        calc.close(add);
        logContext("REGISTRY after close dynamic");
        try {
            add.call(1, 2);
            fail("Closed dynamic service can't be called");
        } catch (Throwable e) {
            assertTrue("Expected ServiceException, caught " + e, e instanceof ServiceException);
        }
    }

    @Test
    public void testCloseByMethod() throws Exception {
        Calculator calc = getClient(Calculator.class, "calc");
        Function add = calc.getFunction("add");
        add.close();
        try {
            add.call(1, 2);
            fail("Closed dynamic service can't be called");
        } catch (Throwable e) {
            assertTrue("Expected ServiceException, caught " + e, e instanceof ServiceException);
        }
    }

    private void logContext(String title) throws NamingException {
        LOG.info(JNDIUtil.printContext(new StringBuilder().append(title), getServiceRegistry(), "", 1).toString());
    }

}
