package org.zenframework.easyservices.util.compare;

import java.util.Comparator;

public class ComparableListComparator<T extends Comparable<T>> extends ListComparator<T> {

    public ComparableListComparator() {
        super(new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                if (o1 == o2)
                    return 0;
                if (o1 == null)
                    return -1;
                if (o2 == null)
                    return 1;
                return o1.compareTo(o2);
            }

        });
    }

}
