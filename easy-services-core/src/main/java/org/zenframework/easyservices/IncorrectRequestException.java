package org.zenframework.easyservices;

import java.net.URI;

public class IncorrectRequestException extends ServiceException {

    private static final long serialVersionUID = 1L;

    public IncorrectRequestException(URI requestUri) {
        super(getMessage(requestUri, null));
    }

    public IncorrectRequestException(URI requestUri, String message) {
        super(getMessage(requestUri, message));
    }

    public IncorrectRequestException(URI requestUri, String message, Throwable cause) {
        super(getMessage(requestUri, message), cause);
    }

    private static String getMessage(URI requestUri, String message) {
        StringBuilder str = new StringBuilder();
        str.append("Can't call service at '").append(requestUri).append("'");
        if (message != null)
            str.append(". ").append(message);
        return str.toString();
    }

}
