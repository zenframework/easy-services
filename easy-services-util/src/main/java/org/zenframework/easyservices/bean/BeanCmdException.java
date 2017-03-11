package org.zenframework.easyservices.bean;

public class BeanCmdException extends Exception {

    private static final long serialVersionUID = 3139093921133408679L;

    public BeanCmdException(String message) {
        super(message);
    }

    public BeanCmdException(Throwable cause) {
        super(cause);
    }

    public BeanCmdException(String message, Throwable cause) {
        super(message, cause);
    }

}
