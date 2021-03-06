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
import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.util.StringUtil;
import org.zenframework.easyservices.util.URIUtil;

public class HttpServiceRequest extends ServiceRequest {

    private static final String PARAM_METHOD = "method";
    private static final String PARAM_PARAMETERS = "params";
    private static final String PARAM_OUT_PARAMETERS_MODE = "outParameters";

    private final HttpServletRequest request;
    private final String servicesPath;
    private final Map<String, List<String>> parameters;

    public HttpServiceRequest(ServiceSession session, HttpServletRequest request, String servicesPath) {
        super(session);
        this.request = request;
        this.servicesPath = servicesPath;
        this.parameters = URIUtil.splitQuery(request.getQueryString(), request.getCharacterEncoding());
    }

    @Override
    public String getServiceName() {
        String path = request.getPathInfo();
        if (StringUtil.isNullOrEmpty(servicesPath) || servicesPath.equals(request.getServletPath()))
            return path;
        // TODO Paths concat & split
        if (!path.startsWith(servicesPath) && path.length() > servicesPath.length() && path.charAt(servicesPath.length()) == '/')
            throw new IllegalStateException("Incorrect services path " + path);
        return path.substring(servicesPath.length() + 1);
    }

    @Override
    public String getMethodName() {
        return getParameter(PARAM_METHOD);
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return null;
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
