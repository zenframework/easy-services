package org.zenframework.easyservices.serialize.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.descriptor.MethodDescriptor;
import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.Serializer;

import com.google.gson.Gson;

import junit.framework.TestCase;

public class JsonSerializerTest extends TestCase {

    private static final String TEST_JSON_ARR = "[{'str':'qwe','i':1}, {'str':'asd','i':2}]";
    private static final String TEST_JSON_OBJ = "{ a : {'str':'qwe','i':1}, b : {'str':'asd','i':2} }";
    private static final String TEST_JSON_OBJ2 = "{ a : [ {'str':'qwe','i':1} ], b : [ {'str':'asd','i':2} ] }";

    public void testStrToArgs() throws Exception {
        Serializer serializer = new JsonSerializer(new Class<?>[] { SimpleBean.class, SimpleBean.class }, null, null, new Gson());
        Object[] result = serializer.deserializeParameters(stream(TEST_JSON_ARR));
        assertTrue(result.length == 2);
        for (Object o : result)
            assertTrue(o instanceof SimpleBean);
    }

    public void testNullSerialization() throws Exception {
        Serializer serializer = new JsonSerializer(null, SimpleBean.class, null, new Gson());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize((Object) null, out);
        assertTrue(Arrays.equals("null".getBytes(), out.toByteArray()));
        assertNull(serializer.deserializeResult(stream("null"), true));
    }

    @SuppressWarnings("unchecked")
    public void testGenericListSerialization() throws Exception {
        Serializer serializer = new JsonSerializer(null, List.class, getMethodDescriptor(null, new ValueDescriptor(null, SimpleBean.class)),
                new Gson());
        List<SimpleBean> list = (List<SimpleBean>) serializer.deserializeResult(stream(TEST_JSON_ARR), true);
        assertEquals(new SimpleBean("qwe", 1), list.get(0));
        assertEquals(new SimpleBean("asd", 2), list.get(1));
    }

    @SuppressWarnings("unchecked")
    public void testGenericMapSerialization() throws Exception {
        Serializer serializer = new JsonSerializer(null, Map.class,
                getMethodDescriptor(null, new ValueDescriptor(null, String.class, SimpleBean.class)), new Gson());
        Map<String, SimpleBean> map = (Map<String, SimpleBean>) serializer.deserializeResult(stream(TEST_JSON_OBJ), true);
        assertEquals(new SimpleBean("qwe", 1), map.get("a"));
        assertEquals(new SimpleBean("asd", 2), map.get("b"));
    }

    @SuppressWarnings("unchecked")
    public void testComplexGenericSerialization() throws Exception {
        Serializer serializer = new JsonSerializer(null, Map.class,
                getMethodDescriptor(null, new ValueDescriptor(null, String.class, List.class, SimpleBean.class)), new Gson());
        Map<String, List<SimpleBean>> map = (Map<String, List<SimpleBean>>) serializer.deserializeResult(stream(TEST_JSON_OBJ2), true);
        assertEquals(Arrays.asList(new SimpleBean("qwe", 1)), map.get("a"));
        assertEquals(Arrays.asList(new SimpleBean("asd", 2)), map.get("b"));
    }

    public void testClassSerialization() throws Exception {
        Serializer serializer = new JsonSerializerFactory().getSerializer(null, Class.class, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(getClass(), out);
        assertEquals(getClass(), serializer.deserializeResult(new ByteArrayInputStream(out.toByteArray()), true));
    }

    private static InputStream stream(String data) {
        return new ByteArrayInputStream(data.getBytes());
    }

    private static MethodDescriptor getMethodDescriptor(ValueDescriptor[] paramDescriptors, ValueDescriptor returnDescriptor) {
        MethodDescriptor methodDescriptor = new MethodDescriptor(paramDescriptors != null ? paramDescriptors.length : 0);
        methodDescriptor.setParameterDescriptors(paramDescriptors);
        methodDescriptor.setReturnDescriptor(returnDescriptor);
        return methodDescriptor;
    }

    public static class SimpleBean {

        private String str;
        private int i;

        public SimpleBean() {}

        public SimpleBean(String str, int i) {
            this.str = str;
            this.i = i;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SimpleBean))
                return false;
            SimpleBean bean = (SimpleBean) obj;
            return i == bean.getI() && (str == bean.getStr() || str.equals(bean.str));
        }

    }

}
