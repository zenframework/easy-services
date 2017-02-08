package org.zenframework.easyservices;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

public interface RequestMapper {

    ServiceRequest getRequestContext(URI requestUri, String contextPath) throws IncorrectRequestException;

    URI getRequestURI(String serviceUrl, String methodName, String args)
            throws UnsupportedEncodingException, URISyntaxException;

}
