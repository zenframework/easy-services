package org.zenframework.easyservices.util.tree;

import java.util.Map;

public abstract class TreeNode {

    public TreeArray toTreeArray() {
        return toTreeArray(this);
    }

    public TreeObject toTreeObject() {
        return toTreeObject(this);
    }

    public TreeValue toTreeValue() {
        return toTreeValue();
    }

    public TreeNode followReference() {
        return followReference(this);
    }

    public TreeNode copy() {
        return copy(this);
    }

    public boolean isTreeArray() {
        return isTreeArray(this);
    }

    public boolean isTreeObject() {
        return isTreeObject(this);
    }

    public boolean isTreeValue() {
        return isTreeValue(this);
    }

    public boolean isTreeReference() {
        return isTreeReference(this);
    }

    public static TreeArray toTreeArray(TreeNode node) {
        node = followReference(node);
        if (isTreeArray(node))
            return (TreeArray) node;
        throw new IllegalStateException("This is not an array.");
    }

    public static TreeObject toTreeObject(TreeNode node) {
        node = followReference(node);
        if (isTreeObject(node))
            return (TreeObject) node;
        throw new IllegalStateException("This is not an object.");
    }

    public static TreeValue toTreeValue(TreeNode node) {
        node = followReference(node);
        if (isTreeValue(node))
            return (TreeValue) node;
        throw new IllegalStateException("This is not a value.");
    }

    public static TreeNode followReference(TreeNode node) {
        while (node instanceof TreeReference)
            node = ((TreeReference) node).getReference();
        return node;
    }

    public static TreeNode copy(TreeNode node) {
        if (isTreeArray(node)) {
            TreeArray array = (TreeArray) node;
            TreeArray copy = new TreeArray(array.size());
            for (TreeNode n : array)
                copy.add(copy(n));
            return copy;
        } else if (isTreeObject(node)) {
            TreeObject object = (TreeObject) node;
            TreeObject copy = new TreeObject();
            for (Map.Entry<String, TreeNode> e : object.entrySet())
                copy.put(e.getKey(), copy(e.getValue()));
            return copy;
        } else if (isTreeValue(node)) {
            return new TreeValue(((TreeValue) node).getValue());
        } else if (isTreeReference(node)) {
            return new TreeReference(copy(((TreeReference) node).getReference()));
        } else {
            throw new IllegalStateException("Unknown tree node type: " + node.getClass());
        }
    }

    public static boolean isTreeArray(TreeNode node) {
        return node instanceof TreeArray;
    }

    public static boolean isTreeObject(TreeNode node) {
        return node instanceof TreeObject;
    }

    public static boolean isTreeValue(TreeNode node) {
        return node instanceof TreeValue;
    }

    public static boolean isTreeReference(TreeNode node) {
        return node instanceof TreeReference;
    }

}
