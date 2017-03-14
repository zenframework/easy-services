package org.zenframework.easyservices.resource.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.WebappConfig;
import org.zenframework.easyservices.resource.Resource;
import org.zenframework.easyservices.resource.ResourceFactory;

public class ResourceFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceFilter.class);

    public static final String PARAM_RESOURCE_FACTORY = "resources";

    private ResourceFactory resourceFactory;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Config config = new WebappConfig(filterConfig);
        LOG.debug(config.toString(true));
        resourceFactory = config.getInstance(PARAM_RESOURCE_FACTORY);
    }

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (isApplicable(request)) {
            Resource resource = resourceFactory.getResource(getRelativePath((HttpServletRequest) request));
            if (resource.exists()) {
                response.setContentType(getContentType((HttpServletRequest) request));
                InputStream in = resource.openStream();
                OutputStream out = response.getOutputStream();
                try {
                    IOUtils.copy(in, out);
                } finally {
                    IOUtils.closeQuietly(in, out);
                }
                return;
            }
        }

        chain.doFilter(request, response);

    }

    protected boolean isApplicable(ServletRequest request) {
        return true;
    }

    protected String getContentType(HttpServletRequest request) {
        return request.getServletContext().getMimeType(request.getRequestURI());
    }

    protected static String getRelativePath(HttpServletRequest req) {
        return req.getRequestURI().substring(req.getContextPath().length());
    }

}
