package org.zenframework.easyservices.util;

import junit.framework.TestCase;

public class CollectionUtilTest extends TestCase {

    public void testCombinations() {
        assertEquals(2, CollectionUtil.combinations(new Object[] { "a", "b" }).size());
        assertEquals(1, CollectionUtil.combinations(new Object[] { "a" }, new Object[] { true }, new Object[] { 1 }).size());
        assertEquals(4, CollectionUtil.combinations(new Object[] { "a", "b" }, new Object[] { true, false }).size());
        assertEquals(12, CollectionUtil.combinations(new Object[] { "a", "b" }, new Object[] { true, false }, new Object[] { 1, 2, 3 }).size());
    }

}
