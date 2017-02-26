package org.zenframework.easyservices.update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

public class ValueUpdaterImplTest extends TestCase {

    private static final Random RANDOM = new Random();

    private final ValueUpdater updater = new ValueUpdaterImpl();

    public void testUpdateByteArray() throws Exception {
        byte[] oldValue = getRandomData(1000);
        byte[] newValue = getRandomData(1000);
        updater.update(oldValue, newValue);
        assertTrue(Arrays.equals(oldValue, newValue));
    }

    public void testUpdateList() throws Exception {
        List<Integer> oldValue = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        List<Integer> newValue = Arrays.asList(3, 2, 1);
        updater.update(oldValue, newValue);
        assertEquals(newValue, oldValue);
    }

    public void testUpdateMap() throws Exception {
        Map<String, Integer> oldValue = new HashMap<String, Integer>();
        oldValue.put("qwe", 1);
        oldValue.put("asd", 2);
        Map<String, Integer> newValue = new HashMap<String, Integer>();
        newValue.put("zxc", 3);
        updater.update(oldValue, newValue);
        assertEquals(newValue, oldValue);
    }

    public void testUpdateBean() throws Exception {
        SimpleBean oldValue = new SimpleBean(1, "qwe");
        SimpleBean newValue = new SimpleBean(2, "asd");
        updater.update(oldValue, newValue);
        assertTrue(oldValue.a == newValue.a && oldValue.getB() == newValue.getB());
    }

    private static byte[] getRandomData(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++)
            data[i] = (byte) RANDOM.nextInt(256);
        return data;
    }

    public static class SimpleBean {

        public int a;
        private String b;

        public SimpleBean() {}

        public SimpleBean(int a, String b) {
            this.a = a;
            this.b = b;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

    }

}
