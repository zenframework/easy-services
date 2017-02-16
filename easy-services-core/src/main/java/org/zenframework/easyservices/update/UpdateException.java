package org.zenframework.easyservices.update;

import org.zenframework.easyservices.ClientException;

public class UpdateException extends ClientException {

    private static final long serialVersionUID = 1L;

    public UpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateException(String message) {
        super(message);
    }

    public UpdateException(Throwable cause) {
        super(cause);
    }

}
