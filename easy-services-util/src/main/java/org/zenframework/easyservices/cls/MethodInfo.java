package org.zenframework.easyservices.cls;

import java.util.Collections;
import java.util.List;

import org.zenframework.easyservices.compare.ComparableListComparator;

public class MethodInfo implements Comparable<MethodInfo>, Cloneable {

    private static final ComparableListComparator<ClassRef> PARAM_TYPES_COMPARATOR = new ComparableListComparator<ClassRef>();

    private final String name;
    private final List<ClassRef> parameterTypes;
    private final ClassRef returnType;

    public MethodInfo(String name, List<ClassRef> parameterTypes, ClassRef returnType) {
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public List<ClassRef> getParameterTypes() {
        return Collections.unmodifiableList(parameterTypes);
    }

    public ClassRef getReturnType() {
        return returnType;
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ parameterTypes.hashCode() ^ returnType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof MethodInfo))
            return false;
        MethodInfo m = (MethodInfo) obj;
        return name.equals(m.getName()) && PARAM_TYPES_COMPARATOR.compare(parameterTypes, m.getParameterTypes()) == 0
                && returnType.equals(m.getReturnType());
    }

    @Override
    public String toString() {
        String params = parameterTypes.toString();
        return name + '(' + params.substring(1, params.length() - 1) + ") : " + returnType;
    }

    @Override
    public int compareTo(MethodInfo o) {
        if (o == null)
            return 1;
        int result = name.compareTo(o.getName());
        if (result != 0)
            return result;
        result = PARAM_TYPES_COMPARATOR.compare(parameterTypes, o.getParameterTypes());
        if (result != 0)
            return result;
        return returnType.compareTo(o.getReturnType());
    }

    @Override
    protected MethodInfo clone() {
        try {
            return (MethodInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}
