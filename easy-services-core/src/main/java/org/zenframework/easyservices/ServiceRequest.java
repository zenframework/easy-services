package org.zenframework.easyservices;

import java.io.IOException;
import java.io.InputStream;

public interface ServiceRequest {

    public String getServiceName();

    public String getMethodName();

    public boolean isArgsByParameter();

    public byte[] getArguments();

    public InputStream getInputStream() throws IOException;

}
