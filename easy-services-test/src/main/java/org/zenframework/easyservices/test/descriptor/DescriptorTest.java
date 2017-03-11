package org.zenframework.easyservices.test.descriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.zenframework.easyservices.Environment;
import org.zenframework.easyservices.test.AbstractServiceTest;

@RunWith(Parameterized.class)
public class DescriptorTest extends AbstractServiceTest {

    @Parameterized.Parameters(name = "{index} format:{0}")
    public static Collection<Object[]> formats() {
        return Arrays.asList(new Object[][] { { "json" }, { "bin" } });
    }

    public DescriptorTest(String format) {
        Environment.setSerializationFormat(format);
        Environment.setOutParametersMode(true);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getServiceRegistry().bind("util", new CollectionUtilImpl());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("util");
    }

    @Test
    public void testTypeParameters() throws Exception {
        CollectionUtil util = getClient(CollectionUtil.class, "util");
        SimpleBean o1 = new SimpleBean("zxc", 3);
        SimpleBean o2 = new SimpleBean("asd", 1);
        SimpleBean o3 = new SimpleBean("qwe", 2);
        List<SimpleBean> list = Arrays.asList(o1, o2, o3);
        assertEquals("asd=1,qwe=2,zxc=3", util.concat(util.sortCopy(list), ","));
    }

    @Test
    public void testOutObjectList() throws Exception {
        CollectionUtil util = getClient(CollectionUtil.class, "util");
        SimpleBean o1 = new SimpleBean("zxc", 3);
        SimpleBean o2 = new SimpleBean("asd", 1);
        SimpleBean o3 = new SimpleBean("qwe", 2);
        List<SimpleBean> list = new ArrayList<SimpleBean>(Arrays.asList(o1, o2, o3));
        util.sortBeans(list);
        assertEquals("asd=1,qwe=2,zxc=3", util.concat(list, ","));
    }

    @Test
    public void testOutIntArray() throws Exception {
        CollectionUtil util = getClient(CollectionUtil.class, "util");
        int[] values = { 2, 1, 3 };
        util.sortInts(values);
        assertTrue(Arrays.equals(new int[] { 1, 2, 3 }, values));
    }

    @Test
    public void testOutObject() throws Exception {
        CollectionUtil util = getClient(CollectionUtil.class, "util");
        SimpleBean o = new SimpleBean("zxc", 3);
        util.clearBean(o);
        assertEquals(new SimpleBean("", 0), o);
    }

}
