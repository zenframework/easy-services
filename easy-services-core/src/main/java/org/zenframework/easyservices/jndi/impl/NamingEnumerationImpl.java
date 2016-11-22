package org.zenframework.easyservices.jndi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

public class NamingEnumerationImpl<T> implements NamingEnumeration<T> {

    public static interface EntryFilter {

        boolean accept(Map.Entry<String, Binding> entry);

    }

    public static interface ElementFactory<T> {

        T getElement(Map.Entry<String, Binding> entry);

    }

    private final List<Map.Entry<String, Binding>> entries;
    private final EntryFilter filter;
    private final ElementFactory<T> elementFactory;
    private int pos = 0;

    public NamingEnumerationImpl(Map<String, Binding> context, ElementFactory<T> elementFactory) {
        this(context, null, elementFactory);
    }

    public NamingEnumerationImpl(Map<String, Binding> context, EntryFilter filter, ElementFactory<T> elementFactory) {
        this.entries = new ArrayList<Map.Entry<String, Binding>>(context.entrySet());
        this.filter = filter;
        this.elementFactory = elementFactory;
    }

    @Override
    public boolean hasMoreElements() {
        return findNext();
    }

    @Override
    public T nextElement() {
        if (!findNext())
            throw new NoSuchElementException();
        return elementFactory.getElement(entries.get(++pos - 1));
    }

    @Override
    public T next() throws NamingException {
        return nextElement();
    }

    @Override
    public boolean hasMore() throws NamingException {
        return hasMoreElements();
    }

    @Override
    public void close() throws NamingException {}

    private boolean findNext() {
        while (pos < entries.size() && (filter == null || !filter.accept(entries.get(pos))))
            pos++;
        return pos < entries.size();
    }

}
