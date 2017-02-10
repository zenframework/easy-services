package org.zenframework.easyservices.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.zenframework.commons.net.URIUtil;
import org.zenframework.easyservices.ServiceRequest;

public class HttpServiceRequest implements ServiceRequest {

    private static final String PARAM_METHOD = "method";
    private static final String PARAM_ARGUMENTS = "args";

    private final HttpServletRequest request;
    private final String servicesPath;
    private final Map<String, List<String>> parameters;

    public HttpServiceRequest(HttpServletRequest request, String servicesPath) {
        this.request = request;
        this.servicesPath = servicesPath;
        this.parameters = URIUtil.splitQuery(request.getQueryString(), request.getCharacterEncoding());
    }

    @Override
    public String getServiceName() {
        String path = request.getPathInfo();
        if (servicesPath.equals(request.getServletPath()))
            return path;
        if (!path.startsWith(servicesPath))
            throw new IllegalStateException("Incorrect services path " + path);
        return path.substring(servicesPath.length());
    }

    @Override
    public String getMethodName() {
        return getParameter(PARAM_METHOD);
    }

    @Override
    public boolean isStringArgs() {
        return request.getMethod().equalsIgnoreCase("GET");
    }

    @Override
    public String getArguments() {
        return getParameter(PARAM_ARGUMENTS);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public Reader getReader() throws IOException {
        return request.getReader();
    }

    public URI getRequestURI() throws URISyntaxException, UnsupportedEncodingException {
        StringBuffer str = request.getRequestURL();
        if (request.getQueryString() != null)
            str.append('?').append(URLEncoder.encode(request.getQueryString(), "UTF-8"));
        return new URI(str.toString());
    }

    public String getParameter(String name) {
        List<String> values = parameters.get(name);
        return values != null && values.size() > 0 ? values.get(0) : null;
    }

}
