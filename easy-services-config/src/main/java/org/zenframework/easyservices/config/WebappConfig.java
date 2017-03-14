package org.zenframework.easyservices.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.config.AbstractConfig;

public class WebappConfig extends AbstractConfig {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(WebappConfig.class);

    private final ServletConfig servletConfig;
    private final FilterConfig filterConfig;

    public WebappConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
        this.filterConfig = null;
    }

    public WebappConfig(FilterConfig filterConfig) {
        this.servletConfig = null;
        this.filterConfig = filterConfig;
    }

    public ServletConfig getServletConfig() {
        return servletConfig;
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    @Override
    public boolean isEmpty() {
        Enumeration<String> names = servletConfig != null ? servletConfig.getInitParameterNames() : filterConfig.getInitParameterNames();
        return !names.hasMoreElements();
    }

    @Override
    public List<String> getNames() {
        List<String> list = new ArrayList<String>();
        Enumeration<String> names = servletConfig != null ? servletConfig.getInitParameterNames() : filterConfig.getInitParameterNames();
        while (names.hasMoreElements())
            list.add(names.nextElement());
        return list;
    }

    @Override
    public String getParam(String name) {
        return servletConfig != null ? servletConfig.getInitParameter(name) : filterConfig.getInitParameter(name);
    }

    @Override
    public void setParam(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getAbsolutePath(String relativePath) {
        File file = new File(relativePath);
        if (file.isAbsolute())
            return file;
        return new File((servletConfig != null ? servletConfig.getServletContext() : filterConfig.getServletContext()).getRealPath(relativePath));
    }

}
