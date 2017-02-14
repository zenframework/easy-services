package org.zenframework.easyservices;

public enum ValueTransfer {

    DEFAULT, IN, OUT, IN_OUT, REF;

    public static ValueTransfer forName(String name) {
        for (ValueTransfer value : values())
            if (value.name().equals(name.toUpperCase()))
                return value;
        throw new IllegalArgumentException();
    }

}
