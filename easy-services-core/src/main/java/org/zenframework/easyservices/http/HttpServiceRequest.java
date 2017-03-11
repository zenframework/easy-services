package org.zenframework.easyservices.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.zenframework.easyservices.ServiceRequest;
import org.zenframework.easyservices.net.URIUtil;

public class HttpServiceRequest extends ServiceRequest {

    private static final String PARAM_METHOD = "method";
    private static final String PARAM_PARAMETERS = "params";
    private static final String PARAM_OUT_PARAMETERS_MODE = "outParameters";

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
    public boolean isOutParametersMode() {
        String outParams = getParameter(PARAM_OUT_PARAMETERS_MODE);
        return outParams != null && Boolean.parseBoolean(outParams);
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

    @Override
    protected InputStream internalGetInputStream() throws IOException {
        return parameters.containsKey(PARAM_PARAMETERS) ? new ByteArrayInputStream(getParameter(PARAM_PARAMETERS).getBytes())
                : request.getInputStream();
    }

}
