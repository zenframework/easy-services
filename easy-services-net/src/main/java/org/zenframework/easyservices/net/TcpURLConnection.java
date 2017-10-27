package org.zenframework.easyservices.net;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.util.URIUtil;

public class TcpURLConnection extends AbstractTcpURLConnection<DefaultHeader, DefaultHeader> {

    private final DefaultHeader responseHeader = new DefaultHeader();

    public TcpURLConnection(URL url) {
        super(url);
    }

    @Override
    public String getHeaderField(String name) {
        List<String> values = responseHeader.getFields().get(name);
        return values == null || values.isEmpty() ? null : values.get(values.size() - 1);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return responseHeader.getFields();
    }

    @Override
    public String getHeaderFieldKey(int n) {
        if (n >= responseHeader.getFields().size())
            throw new IndexOutOfBoundsException("" + n + " > " + (responseHeader.getFields().size() - 1));
        Iterator<String> keys = responseHeader.getFields().keySet().iterator();
        for (int i = 0; i < n - 1; i++)
            keys.next();
        return keys.next();
    }

    @Override
    public String getHeaderField(int n) {
        if (n >= responseHeader.getFields().size())
            throw new IndexOutOfBoundsException("" + n + " > " + (responseHeader.getFields().size() - 1));
        Iterator<List<String>> it = responseHeader.getFields().values().iterator();
        for (int i = 0; i < n - 1; i++)
            it.next();
        List<String> values = it.next();
        return values == null || values.isEmpty() ? null : values.get(values.size() - 1);
    }

    @Override
    public DefaultHeader getRequestHeader() {
        try {
            String path = getURL().getPath();
            if (path != null)
                setRequestProperty(DefaultHeader.PATH, path);
            for (Map.Entry<String, List<String>> field : URIUtil.getParameters(getURL().toURI(), "UTF-8").entrySet())
                for (String value : field.getValue())
                    addRequestProperty(field.getKey(), value);
            return new DefaultHeader(getRequestProperties());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public DefaultHeader getResponseHeader() {
        return responseHeader;
    }

    @Override
    protected TcpClient initClient(URL url) throws IOException {
        return new SimpleTcpClient(url.getHost(), url.getPort());
    }

}
