package org.zenframework.easyservices;

import java.net.URI;

public interface ServiceInvoker {

    String invoke(URI requestUri, String contextPath, ErrorHandler errorHandler);

}
