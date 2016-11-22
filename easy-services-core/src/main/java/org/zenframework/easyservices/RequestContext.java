package org.zenframework.easyservices;

public class RequestContext {

    private String serviceName;
    private String methodName;
    private String arguments;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(1024).append(serviceName).append('.').append(methodName).append('(');
        if (arguments != null)
            str.append(arguments);
        return str.append(')').toString();
    }

}
