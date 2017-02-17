package org.zenframework.easyservices.util.cls;

public class ClassRef implements Comparable<ClassRef>, Cloneable {

    private ClassInfo classInfo;
    private int arrayDimension;

    public ClassRef() {}

    public ClassRef(String name) {
        int arrayDimension = 0;
        while (name.endsWith("[]")) {
            name = name.substring(0, name.length() - 2);
            arrayDimension++;
        }
        try {
            this.classInfo = ClassInfo.getClassRef(Class.forName(name)).getClassInfo();
            this.arrayDimension = arrayDimension;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Name '" + name + "' is not a class name", e);
        }
    }

    public ClassRef(ClassInfo classInfo) {
        this(classInfo, 0);
    }

    public ClassRef(ClassInfo classInfo, int arrayDimension) {
        this.classInfo = classInfo;
        this.arrayDimension = arrayDimension;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    public int getArrayDimension() {
        return arrayDimension;
    }

    public void setArrayDimension(int arrayDimension) {
        this.arrayDimension = arrayDimension;
    }

    public String getName() {
        StringBuilder str = new StringBuilder(classInfo.getName().length() + arrayDimension * 2).append(classInfo.getName());
        for (int i = 0; i < arrayDimension; i++)
            str.append("[]");
        return str.toString();
    }

    @Override
    public int hashCode() {
        return classInfo.hashCode() ^ arrayDimension;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ClassRef))
            return false;
        ClassRef ref = (ClassRef) obj;
        return classInfo.equals(ref.getClassInfo()) && arrayDimension == ref.getArrayDimension();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(ClassRef o) {
        int compare = classInfo.compareTo(o.getClassInfo());
        if (compare != 0)
            return compare;
        return Integer.compare(arrayDimension, o.getArrayDimension());
    }

    @Override
    public ClassRef clone() {
        try {
            return (ClassRef) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
