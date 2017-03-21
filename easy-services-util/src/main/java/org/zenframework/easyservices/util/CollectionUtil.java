package org.zenframework.easyservices.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CollectionUtil {

    private CollectionUtil() {}

    public static List<Object[]> combinations(Object[]... values) {
        return combinations(0, values);
    }

    private static List<Object[]> combinations(int start, Object[]... values) {
        List<Object[]> combinations = new ArrayList<Object[]>();
        List<Object[]> tailCombs = start < values.length - 1 ? combinations(start + 1, values) : Arrays.<Object[]> asList(new Object[0]);
        for (Object o : values[start]) {
            for (Object[] comb : tailCombs) {
                Object[] newComb = new Object[comb.length + 1];
                newComb[0] = o;
                System.arraycopy(comb, 0, newComb, 1, comb.length);
                combinations.add(newComb);
            }
        }
        return combinations;
    }

}
