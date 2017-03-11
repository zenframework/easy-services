package org.zenframework.easyservices.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TreeObject extends TreeNode implements Map<String, TreeNode> {

    private final Map<String, TreeNode> map;

    public TreeObject() {
        this(new HashMap<String, TreeNode>());
    }

    public TreeObject(Map<String, TreeNode> map) {
        this.map = map;
    }

    public TreeObject(Collection<Map.Entry<String, ?>> entries) {
        this();
        for (Map.Entry<String, ?> entry : entries)
            put(entry.getKey(), entry.getValue() instanceof TreeNode ? (TreeNode) entry : new TreeValue(entry.getValue()));
    }

    @SafeVarargs
    public TreeObject(Map.Entry<String, ?>... entries) {
        this();
        for (Map.Entry<String, ?> entry : entries)
            put(entry.getKey(), entry.getValue() instanceof TreeNode ? (TreeNode) entry : new TreeValue(entry.getValue()));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public TreeNode get(Object key) {
        return map.get(key);
    }

    @Override
    public TreeNode put(String key, TreeNode value) {
        return map.put(key, value);
    }

    @Override
    public TreeNode remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends TreeNode> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<TreeNode> values() {
        return map.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, TreeNode>> entrySet() {
        return map.entrySet();
    }

}
