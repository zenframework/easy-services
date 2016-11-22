package org.zenframework.easyservices;

public class ServiceException extends Exception {

    private static final long serialVersionUID = -2101820360873511242L;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
