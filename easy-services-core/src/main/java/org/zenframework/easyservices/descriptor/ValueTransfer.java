package org.zenframework.easyservices.descriptor;

public enum ValueTransfer {

    DEFAULT(""), OUT(":O"), REF(":R");

    private final String marker;

    private ValueTransfer(String marker) {
        this.marker = marker;
    }

    public String getMarker() {
        return marker;
    }

    public static ValueTransfer forName(String name) {
        for (ValueTransfer value : values())
            if (value.name().equals(name.toUpperCase()))
                return value;
        throw new IllegalArgumentException();
    }

}
