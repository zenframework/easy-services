package org.zenframework.easyservices.http;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.zenframework.easyservices.ServiceRequest;

public class HttpServiceRequest implements ServiceRequest {

    private static final String PARAM_METHOD = "method";
    private static final String PARAM_ARGUMENTS = "args";

    private final HttpServletRequest request;
    private final String servicesPath;

    public HttpServiceRequest(HttpServletRequest request, String servicesPath) {
        this.request = request;
        this.servicesPath = servicesPath;
    }

    @Override
    public String getServiceName() {
        String path = request.getPathInfo();
        if (!path.startsWith(servicesPath))
            throw new IllegalStateException("Incorrect servies path " + path);
        return path.substring(servicesPath.length());
    }

    @Override
    public String getMethodName() {
        return request.getParameter(PARAM_METHOD);
    }

    /*@Override
    public boolean isStringArgs() {
        return request.getMethod().equalsIgnoreCase("GET");
    }*/

    @Override
    public String getArguments() {
        return request.getParameter(PARAM_ARGUMENTS);
    }

    /*@Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public Reader getReader() throws IOException {
        return request.getReader();
    }*/

    public URI getRequestURI() throws URISyntaxException, UnsupportedEncodingException {
        StringBuffer str = request.getRequestURL();
        if (request.getQueryString() != null)
            str.append('?').append(URLEncoder.encode(request.getQueryString(), "UTF-8"));
        return new URI(str.toString());
    }

}
