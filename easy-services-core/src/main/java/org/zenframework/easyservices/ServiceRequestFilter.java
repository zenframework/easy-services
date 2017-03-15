package org.zenframework.easyservices;

import org.zenframework.easyservices.impl.InvocationContext;

public interface ServiceRequestFilter {

    void filterRequest(ServiceRequest request);

    void filterContext(ServiceRequest request, InvocationContext context);

}
