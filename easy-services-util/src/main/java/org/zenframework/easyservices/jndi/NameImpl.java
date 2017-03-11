package org.zenframework.easyservices.jndi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.Name;

public class NameImpl implements Name {

    private static final long serialVersionUID = -8960153237756073197L;

    public static final String DELIMETER = "/";

    private final String name;
    private final List<String> components;

    private NameImpl(List<String> components) {
        StringBuilder str = new StringBuilder();
        this.components = components;
        for (int i = 0; i < components.size(); i++)
            str.append(components.get(i)).append(DELIMETER);
        if (str.length() > 0)
            str.setLength(str.length() - 1);
        this.name = str.toString();
    }

    private NameImpl(List<String> components, int start, int end) {
        StringBuilder str = new StringBuilder();
        this.components = new ArrayList<String>(end - start);
        for (int i = start; i < end; i++) {
            this.components.add(components.get(i));
            str.append(components.get(i)).append(DELIMETER);
        }
        if (str.length() > 0)
            str.setLength(str.length() - 1);
        this.name = str.toString();
    }

    public NameImpl() {
        this("");
    }

    public NameImpl(String name) {
        this.name = name;
        this.components = name.length() == 0 ? Collections.<String> emptyList() : Arrays.<String> asList(name.split("\\/"));
    }

    @Override
    public Object clone() {
        return new NameImpl(components, 0, components.size());
    }

    @Override
    public int compareTo(Object obj) {
        return this.name.compareTo(obj.toString());
    }

    @Override
    public int size() {
        return components.size();
    }

    @Override
    public boolean isEmpty() {
        return components.isEmpty();
    }

    @Override
    public Enumeration<String> getAll() {
        return new Enumeration<String>() {

            private int n = 0;

            @Override
            public boolean hasMoreElements() {
                return components.size() > n;
            }

            @Override
            public String nextElement() {
                return components.get(n++);
            }

        };
    }

    @Override
    public String get(int posn) {
        return components.get(posn);
    }

    @Override
    public Name getPrefix(int posn) {
        return new NameImpl(components, 0, posn);
    }

    @Override
    public Name getSuffix(int posn) {
        return new NameImpl(components, posn, components.size());
    }

    @Override
    public boolean startsWith(Name n) {
        if (n.size() > size())
            return false;
        for (int i = 0; i < n.size(); i++)
            if (!get(i).equals(n.get(i)))
                return false;
        return true;
    }

    @Override
    public boolean endsWith(Name n) {
        int shift = size() - n.size();
        if (shift < 0)
            return false;
        for (int i = 0; i < n.size(); i++)
            if (!get(i + shift).equals(n.get(i)))
                return false;
        return true;
    }

    @Override
    public Name addAll(Name suffix) throws InvalidNameException {
        List<String> components = new ArrayList<String>(size() + suffix.size());
        components.addAll(this.components);
        Enumeration<String> e = suffix.getAll();
        while (e.hasMoreElements())
            components.add(e.nextElement());
        return new NameImpl(components);
    }

    @Override
    public Name addAll(int posn, Name n) throws InvalidNameException {
        List<String> components = new ArrayList<String>(size() + n.size());
        for (int i = 0; i < posn; i++)
            components.add(get(i));
        Enumeration<String> e = n.getAll();
        while (e.hasMoreElements())
            components.add(e.nextElement());
        for (int i = posn; i < size(); i++)
            components.add(get(i));
        return new NameImpl(components);
    }

    @Override
    public Name add(String comp) throws InvalidNameException {
        List<String> components = new ArrayList<String>(size() + 1);
        components.addAll(this.components);
        components.add(comp);
        return new NameImpl(components);
    }

    @Override
    public Name add(int posn, String comp) throws InvalidNameException {
        List<String> components = new ArrayList<String>(size() + 1);
        for (int i = 0; i < posn; i++)
            components.add(get(i));
        components.add(comp);
        for (int i = posn; i < size(); i++)
            components.add(get(i));
        return new NameImpl(components);
    }

    @Override
    public Object remove(int posn) throws InvalidNameException {
        return components.remove(posn);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return name.equals(obj.toString());
    }

    @Override
    public String toString() {
        return name;
    }

}
