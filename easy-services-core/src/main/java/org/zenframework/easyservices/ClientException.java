package org.zenframework.easyservices;

public class ClientException extends RuntimeException {

    private static final long serialVersionUID = -2101820360873511242L;

    public ClientException(String message) {
        super(message);
    }

    public ClientException(Throwable cause) {
        super(cause);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

}
