package org.zenframework.easyservices.descriptor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import junit.framework.TestCase;

public class DescriptorUtilTest extends TestCase {

    public void testGetAllAssignableFrom() throws Exception {
        List<Class<?>> classes = new ArrayList<Class<?>>(DescriptorUtil.getAllAssignableFrom(CC.class));
        System.out.println("Classes: " + toString(classes));
        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                Class<?> a = classes.get(i);
                Class<?> b = classes.get(j);
                assertFalse(a.getSimpleName() + " < " + b.getSimpleName(), b.isAssignableFrom(a));
            }
        }
    }

    public static interface IA {}

    public static interface IB extends IA {}

    public static interface IC {}

    public static interface ID {}

    public static interface IE extends IC, ID {}

    public static class CA implements IA {}

    public static class CB extends CA implements IB {}

    public static class CC extends CB implements IE {}

    private static String toString(List<Class<?>> list) {
        return CollectionUtils.collect(list, new Transformer<Class<?>, String>() {

            @Override
            public String transform(Class<?> input) {
                return input.getSimpleName();
            }

        }).toString();
    }

}
