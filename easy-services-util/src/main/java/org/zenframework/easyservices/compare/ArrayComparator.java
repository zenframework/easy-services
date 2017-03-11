package org.zenframework.easyservices.compare;

import java.util.Comparator;

public class ArrayComparator<T> implements Comparator<T[]> {

    private final Comparator<T> elementComparator;

    public ArrayComparator(Comparator<T> elementComparator) {
        this.elementComparator = elementComparator;
    }

    @Override
    public int compare(T[] o1, T[] o2) {
        if (o1 == o2)
            return 0;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return 1;
        int min = Math.min(o1.length, o2.length);
        for (int i = 0; i < min; i++) {
            int compare = elementComparator.compare(o1[i], o2[i]);
            if (compare != 0)
                return compare;
        }
        return Integer.compare(o1.length, o2.length);
    }

}
