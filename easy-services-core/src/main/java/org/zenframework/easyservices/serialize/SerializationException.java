package org.zenframework.easyservices.serialize;

import org.zenframework.easyservices.ServiceException;

public class SerializationException extends ServiceException {

    private static final long serialVersionUID = -5368103071231339780L;

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
