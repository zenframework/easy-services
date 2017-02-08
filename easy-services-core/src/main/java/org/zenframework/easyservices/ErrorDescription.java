package org.zenframework.easyservices;

import java.io.Serializable;
import java.util.Date;

public class ErrorDescription implements Serializable {

    private static final long serialVersionUID = 4727541366274200858L;

    private final long timestamp = new Date().getTime();
    private final String className;
    private final String message;

    public ErrorDescription(String className, String message) {
        this.className = className;
        this.message = message;
    }

    public ErrorDescription(Throwable e) {
        this(e.getClass().getName(), e.getMessage());
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getClassName() {
        return className;
    }

}
