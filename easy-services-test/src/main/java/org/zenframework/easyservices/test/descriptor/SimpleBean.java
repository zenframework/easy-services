package org.zenframework.easyservices.test.descriptor;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public class SimpleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private int value;

    public SimpleBean() {}

    public SimpleBean(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return (name == null ? 0 : name.hashCode()) ^ value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleBean))
            return false;
        SimpleBean o = (SimpleBean) obj;
        return StringUtils.equals(name, o.getName()) && value == o.getValue();
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }

}
