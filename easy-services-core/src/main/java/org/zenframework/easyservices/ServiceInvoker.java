package org.zenframework.easyservices;

import java.io.IOException;

public interface ServiceInvoker {

    void invoke(ServiceRequest request, ServiceResponse response) throws IOException;

}
