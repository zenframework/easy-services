package org.zenframework.easyservices;

public class ServiceResponse {

    private String serializedResult;
    private Throwable error;

    public ServiceResponse(String serializedResult, Throwable error) {
        this.serializedResult = serializedResult;
        this.error = error;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public String getSerializedResult() {
        return serializedResult;
    }

    public void setSerializedResult(String serializedResult) {
        this.serializedResult = serializedResult;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

}
