package org.zenframework.easyservices.util.cls;

public class FieldInfo implements Comparable<FieldInfo>, Cloneable {

    private final String name;
    private final ClassRef type;
    private final boolean readable;
    private final boolean writable;

    public FieldInfo(String name, ClassRef type, boolean readable, boolean writable) {
        this.name = name;
        this.type = type;
        this.readable = readable;
        this.writable = writable;
    }

    public String getName() {
        return name;
    }

    public ClassRef getType() {
        return type;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FieldInfo))
            return false;
        FieldInfo f = (FieldInfo) obj;
        return name.equals(f.getName()) && type.equals(f.getType());
    }

    @Override
    public String toString() {
        return access() + ' ' + name + " : " + type;
    }

    @Override
    public int compareTo(FieldInfo o) {
        return o == null ? 1 : name.compareTo(o.getName());
    }

    @Override
    public FieldInfo clone() {
        try {
            return (FieldInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    private String access() {
        if (readable) {
            if (writable)
                return "rw";
            return "r-";
        }
        if (writable)
            return "-w";
        return "--";
    }

}
