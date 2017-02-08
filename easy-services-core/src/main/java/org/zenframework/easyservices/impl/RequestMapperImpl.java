package org.zenframework.easyservices.impl;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.IncorrectRequestException;
import org.zenframework.easyservices.ServiceRequest;
import org.zenframework.easyservices.RequestMapper;

public class RequestMapperImpl implements RequestMapper {

    public static final RequestMapperImpl INSTANCE = new RequestMapperImpl();

    private static final String PARAM_METHOD = "method";
    private static final String PARAM_ARGUMENTS = "args";

    @Override
    public ServiceRequest getRequestContext(URI requestUri, String contextPath) throws IncorrectRequestException {
        if (contextPath == null)
            contextPath = "";
        String path = requestUri.getPath();
        if (!path.startsWith(contextPath))
            throw new IncorrectRequestException(requestUri, "Incorrect context path '" + contextPath + "'");
        return getRequestContext(requestUri, contextPath, path, splitQuery(requestUri));
    }

    @Override
    public URI getRequestURI(String serviceUrl, String methodName, String args) throws UnsupportedEncodingException, URISyntaxException {
        StringBuilder str = new StringBuilder();
        str.append(serviceUrl).append('?').append(PARAM_METHOD).append('=').append(methodName);
        if (args != null)
            str.append('&').append(PARAM_ARGUMENTS).append('=').append(URLEncoder.encode(args, "UTF-8"));
        return new URI(str.toString());
    }

    protected ServiceRequest getRequestContext(URI requestUri, String contextPath, String path, Map<String, List<String>> params)
            throws IncorrectRequestException {
        ServiceRequest context = new ServiceRequest();
        context.setServiceName(path.substring(contextPath.length()));
        if (params.containsKey(PARAM_METHOD))
            context.setMethodName(params.get(PARAM_METHOD).get(0));
        if (params.containsKey(PARAM_ARGUMENTS))
            context.setArguments(params.get(PARAM_ARGUMENTS).get(0));
        return context;
    }

    private static Map<String, List<String>> splitQuery(URI uri) throws IncorrectRequestException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String query = uri.getQuery();
        final String[] pairs = query == null ? new String[0] : uri.getQuery().split("&");
        try {
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<String>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                query_pairs.get(key).add(value);
            }
            return query_pairs;
        } catch (UnsupportedEncodingException e) {
            throw new IncorrectRequestException(uri, "Can't parse query part", e);
        }
    }

}
