package org.zenframework.easyservices.compare;

public class ClassArrayComparator extends ArrayComparator<Class<?>> {

    public static final ClassArrayComparator INSTANCE = new ClassArrayComparator();

    public ClassArrayComparator() {
        super(ClassNameComparator.INSTANCE);
    }

}
