package org.zenframework.easyservices.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class URIUtil {

    private static final String DEFAULT_CHARSET = "UTF-8";

    private URIUtil() {}

    public static Map<String, List<String>> getParameters(URI uri, String charset) {
        return splitQuery(uri.getQuery(), charset);
    }

    public static Map<String, List<String>> splitQuery(String query, String charset) {
        if (charset == null)
            charset = DEFAULT_CHARSET;
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        final String[] pairs = query == null ? new String[0] : query.split("&");
        try {
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), charset) : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<String>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), charset) : null;
                query_pairs.get(key).add(value);
            }
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(charset);
        }
        return query_pairs;
    }

}
