package org.zenframework.easyservices.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.Configurable;

public class FileResourceFactory implements ResourceFactory, Configurable {

    public static final String PARAM_ROOT = "root";

    private File root;

    public File getRoot() {
        return root;
    }

    public void setRoot(File root) {
        this.root = root;
    }

    @Override
    public void init(Config config) {
        root = config.getAbsolutePath(config.getParam(PARAM_ROOT).toString());
    }

    @Override
    public void destroy(Config config) {}

    @Override
    public Resource getResource(final String path) throws IOException {
        final File file = new File(root, path);
        return new Resource() {

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public boolean exists() {
                return file.exists();
            }

            @Override
            public long lastModified() {
                return file.lastModified();
            }

            @Override
            public InputStream openStream() throws IOException {
                return new FileInputStream(file);
            }

        };
    }

}
