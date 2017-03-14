package org.zenframework.easyservices.util.bean;

public class ConverterException extends Exception {

    private static final long serialVersionUID = 8767689245454234955L;

    public ConverterException(Object value, Class<?> clazz) {
        super("Can't convert '" + value + "' to " + clazz.getName());
    }

    public ConverterException(Object value, Class<?> clazz, Throwable cause) {
        super("Can't convert '" + value + "' to " + clazz.getName(), cause);
    }

}
