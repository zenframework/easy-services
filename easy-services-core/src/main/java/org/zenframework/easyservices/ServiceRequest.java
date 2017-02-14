package org.zenframework.easyservices;

import java.io.IOException;
import java.io.InputStream;

public interface ServiceRequest {

    public String getServiceName();

    public String getMethodName();

    public void cacheInput() throws IOException;

    public InputStream getInputStream() throws IOException;

}
