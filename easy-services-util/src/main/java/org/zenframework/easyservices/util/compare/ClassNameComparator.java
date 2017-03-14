package org.zenframework.easyservices.util.compare;

import java.util.Comparator;

public class ClassNameComparator implements Comparator<Class<?>> {

    public static final ClassNameComparator INSTANCE = new ClassNameComparator();

    @Override
    public int compare(Class<?> o1, Class<?> o2) {
        if (o1 == o2)
            return 0;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return 1;
        return o1.getCanonicalName().compareTo(o2.getCanonicalName());
    }

}
