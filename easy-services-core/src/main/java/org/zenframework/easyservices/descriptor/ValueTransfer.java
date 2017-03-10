package org.zenframework.easyservices.descriptor;

public enum ValueTransfer {

    DEFAULT, OUT, REF;

    public static ValueTransfer forName(String name) {
        for (ValueTransfer value : values())
            if (value.name().equals(name.toUpperCase()))
                return value;
        throw new IllegalArgumentException();
    }

}
