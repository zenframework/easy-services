package org.zenframework.easyservices;

public interface ServiceRequestFilter {

    void filterRequest(ServiceRequest request);

    void filterContext(ServiceRequest request, InvocationContext context);

}
