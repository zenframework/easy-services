package org.zenframework.easyservices.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;

import org.zenframework.easyservices.util.config.Config;
import org.zenframework.easyservices.util.config.Configurable;

public class ClasspathResourceFactory implements ResourceFactory, Configurable {

    private static final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    public static final String PARAM_CLASS_LOADER = "classLoader";
    public static final String PARAM_ROOT = "root";

    public static final ClassLoader DEFAULT_CLASS_LOADER = ClasspathResourceFactory.class.getClassLoader();
    public static final String DEFAULT_ROOT = "export/";

    private ClassLoader classLoader = DEFAULT_CLASS_LOADER;
    private String basePath = DEFAULT_ROOT;

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void init(Config config) {
        classLoader = (ClassLoader) config.getInstance(PARAM_CLASS_LOADER, classLoader);
        basePath = config.getParam(PARAM_ROOT, DEFAULT_ROOT);
        if (!basePath.endsWith("/"))
            basePath += '/';
    }

    @Override
    public void destroy(Config config) {}

    @Override
    public Resource getResource(String path) {
        final String resourcePath = path == null ? "" : path.startsWith("/") ? path.substring(1) : path;
        final URL url = classLoader.getResource(basePath + resourcePath);
        return new Resource() {

            @Override
            public String getPath() {
                return resourcePath;
            }

            @Override
            public boolean exists() {
                return url != null;
            }

            @Override
            public long lastModified() {
                return runtimeMXBean.getStartTime();
            }

            @Override
            public InputStream openStream() throws IOException {
                return url.openStream();
            }

        };
    }

}
