package org.zenframework.easyservices.util.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class TreeArray extends TreeNode implements List<TreeNode> {

    private final List<TreeNode> list;

    public TreeArray() {
        this(new ArrayList<TreeNode>());
    }

    public TreeArray(int size) {
        this(new ArrayList<TreeNode>(size));
    }

    public TreeArray(List<TreeNode> list) {
        this.list = list;
    }

    public TreeArray(Collection<Object> values) {
        this(new ArrayList<TreeNode>(values.size()));
        for (Object value : values)
            add(value instanceof TreeNode ? (TreeNode) value : new TreeValue(value));
    }

    public TreeArray(Object... values) {
        this(new ArrayList<TreeNode>(values.length));
        for (Object value : values)
            add(value instanceof TreeNode ? (TreeNode) value : new TreeValue(value));
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<TreeNode> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(TreeNode e) {
        return list.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends TreeNode> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends TreeNode> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public TreeNode get(int index) {
        return list.get(index);
    }

    @Override
    public TreeNode set(int index, TreeNode element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, TreeNode element) {
        list.add(index, element);
    }

    @Override
    public TreeNode remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<TreeNode> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<TreeNode> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<TreeNode> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

}