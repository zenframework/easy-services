package org.zenframework.easyservices;

public interface ServiceRequest {

    public String getServiceName();

    public String getMethodName();

    //public boolean isStringArgs();

    public String getArguments();

    //public InputStream getInputStream() throws IOException;

    //public Reader getReader() throws IOException;

}
