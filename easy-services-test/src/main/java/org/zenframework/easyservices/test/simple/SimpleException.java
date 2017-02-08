package org.zenframework.easyservices.test.simple;

public class SimpleException extends Exception {

    private static final long serialVersionUID = 1L;

    public SimpleException() {
        super();
    }

    public SimpleException(String message, Throwable cause) {
        super(message, cause);
    }

    public SimpleException(String message) {
        super(message);
    }

    public SimpleException(Throwable cause) {
        super(cause);
    }

}
