package org.zenframework.easyservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface ServiceRequest {

    public String getServiceName();

    public String getMethodName();

    public boolean isStringArgs();

    public String getArguments();

    public InputStream getInputStream() throws IOException;

    public Reader getReader() throws IOException;

}
