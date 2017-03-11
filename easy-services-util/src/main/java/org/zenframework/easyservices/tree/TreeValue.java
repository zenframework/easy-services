package org.zenframework.easyservices.tree;

public class TreeValue extends TreeNode {

    private Object value;

    public TreeValue() {}

    public TreeValue(Object value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<?> valueType) {
        return (T) value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
