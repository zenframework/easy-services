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
import org.zenframework.easyservices.RequestContext;
import org.zenframework.easyservices.RequestMapper;

public class RequestMapperImpl implements RequestMapper {

    private static final String METHOD_PARAM = "method";
    private static final String ARGUMENTS_PARAM = "args";

    @Override
    public RequestContext getRequestContext(URI requestUri, String contextPath) throws IncorrectRequestException {
        if (contextPath == null)
            contextPath = "";
        RequestContext context = new RequestContext();
        Map<String, List<String>> params = splitQuery(requestUri);
        String path = requestUri.getPath();
        if (!path.startsWith(contextPath))
            throw new IncorrectRequestException(requestUri, "Incorrect context path '" + contextPath + "'");
        if (!params.containsKey(METHOD_PARAM))
            throw new IncorrectRequestException(requestUri,
                    "Can't get service method: parameter '" + METHOD_PARAM + "' is null");
        context.setServiceName(path.substring(contextPath.length()));
        context.setMethodName(params.get(METHOD_PARAM).get(0));
        if (params.containsKey(ARGUMENTS_PARAM))
            context.setArguments(params.get(ARGUMENTS_PARAM).get(0));
        return context;
    }

    @Override
    public URI getRequestURI(String serviceUrl, String methodName, String args)
            throws UnsupportedEncodingException, URISyntaxException {
        StringBuilder str = new StringBuilder();
        str.append(serviceUrl).append('?').append(METHOD_PARAM).append('=').append(methodName);
        if (args != null)
            str.append('&').append(ARGUMENTS_PARAM).append('=').append(URLEncoder.encode(args, "UTF-8"));
        return new URI(str.toString());
    }

    private static Map<String, List<String>> splitQuery(URI uri) throws IncorrectRequestException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = uri.getQuery().split("&");
        try {
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<String>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                        : null;
                query_pairs.get(key).add(value);
            }
            return query_pairs;
        } catch (UnsupportedEncodingException e) {
            throw new IncorrectRequestException(uri, "Can't parse query part", e);
        }
    }

}
