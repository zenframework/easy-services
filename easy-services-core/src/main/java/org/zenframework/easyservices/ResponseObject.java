package org.zenframework.easyservices;

import java.io.Serializable;

public class ResponseObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object result = null;
    private Object[] parameters = null;

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
