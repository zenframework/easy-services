package org.zenframework.easyservices.update;

import java.util.Arrays;
import java.util.Random;

import junit.framework.TestCase;

public class ValueUpdaterImplTest extends TestCase {

    private static final Random RANDOM = new Random();

    public void testUpdate() throws Exception {
        ValueUpdater updater = new ValueUpdaterImpl();
        byte[] oldValue = getRandomData(1000);
        byte[] newValue = getRandomData(1000);
        updater.update(oldValue, newValue);
        assertTrue(Arrays.equals(oldValue, newValue));
    }

    private static byte[] getRandomData(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++)
            data[i] = (byte) RANDOM.nextInt(256);
        return data;
    }

}
