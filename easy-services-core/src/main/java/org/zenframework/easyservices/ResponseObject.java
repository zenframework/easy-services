package org.zenframework.easyservices;

import java.io.Serializable;

public class ResponseObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private Object result;
    private Object[] parameters;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

}
