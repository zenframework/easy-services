package org.zenframework.easyservices.util.string;

public class ParameterizedString {

    private final String string;
    private final Object[] args;

    public ParameterizedString(String string, Object... args) {
        this.string = string;
        this.args = args;
    }

    @Override
    public String toString() {
        return StringUtil.getStringWithArgs(string, args);
    }

}
