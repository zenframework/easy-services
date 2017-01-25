package org.zenframework.easyservices.test.descriptor;

import java.util.Arrays;
import java.util.List;

import org.zenframework.easyservices.test.AbstractServiceTest;

public class DescriptorTest extends AbstractServiceTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getServiceRegistry().bind("/util", new CollectionUtilImpl());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getServiceRegistry().unbind("/util");
    }

    public void testDescriptors() throws Exception {
        CollectionUtil util = getClient(CollectionUtil.class, "/util");
        SimpleBean o1 = new SimpleBean("zxc", 3);
        SimpleBean o2 = new SimpleBean("asd", 1);
        SimpleBean o3 = new SimpleBean("qwe", 2);
        List<SimpleBean> list = Arrays.asList(o1, o2, o3);
        assertEquals("asd=1,qwe=2,zxc=3", util.concat(util.sort(list), ","));
    }

}
