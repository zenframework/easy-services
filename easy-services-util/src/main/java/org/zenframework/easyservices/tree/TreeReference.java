package org.zenframework.easyservices.tree;

public class TreeReference extends TreeNode {

    private TreeNode reference;

    public TreeReference() {}

    public TreeReference(TreeNode reference) {
        this.reference = reference;
    }

    public TreeNode getReference() {
        return reference;
    }

    public void setReference(TreeNode reference) {
        this.reference = reference;
    }

}
