package org.zenframework.easyservices.util.config;

public class ConfigException extends RuntimeException {

    private static final long serialVersionUID = -7657471712646912622L;

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigException(String message) {
        super(message);
    }

}
