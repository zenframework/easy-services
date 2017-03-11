package org.zenframework.easyservices.compare;

import java.util.Comparator;
import java.util.List;

public class ListComparator<T extends Comparable<T>> implements Comparator<List<T>> {

    private final Comparator<T> elementComparator;

    public ListComparator(Comparator<T> elementComparator) {
        this.elementComparator = elementComparator;
    }

    @Override
    public int compare(List<T> o1, List<T> o2) {
        if (o1 == o2)
            return 0;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return 1;
        int min = Math.min(o1.size(), o2.size());
        for (int i = 0; i < min; i++) {
            int compare = elementComparator.compare(o1.get(i), o2.get(i));
            if (compare != 0)
                return compare;
        }
        return Integer.compare(o1.size(), o2.size());
    }

}
