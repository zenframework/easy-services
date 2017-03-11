package org.zenframework.easyservices.js.babel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.Configurable;
import org.zenframework.easyservices.resource.FileResourceFactory;
import org.zenframework.easyservices.resource.Resource;
import org.zenframework.easyservices.resource.ResourceFactory;

public class BabelResourceFactory implements ResourceFactory, Configurable {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(BabelResourceFactory.class);

    public static final String PARAM_SOURCE_RESOURCE_FACTORY = "source.resources";
    public static final String PARAM_SOURCE_EXTENSIONS = "source.extensions";
    public static final String PARAM_TARGET_EXTENSION = "target.extension";
    public static final String PARAM_BABEL = "babel";
    public static final String PARAM_PRESETS = "presets";
    public static final String PARAM_CHARSET = "charset";

    public static final String[] DEFAULT_SOURCE_EXTENSIONS = { "jsx" };
    public static final String DEFAULT_TARGET_EXTENSION = "js";
    public static final String[] DEFAULT_PRESETS = { "react" };
    public static final String DEFAULT_CHARSET = "UTF-8";

    private ResourceFactory sources = new FileResourceFactory();
    private String sourceExtensions[] = DEFAULT_SOURCE_EXTENSIONS;
    private String targetExtension = DEFAULT_TARGET_EXTENSION;
    private Babel babel = new PooledBabel();
    private String presets[] = DEFAULT_PRESETS;
    private Charset charset = Charset.forName(DEFAULT_CHARSET);

    public ResourceFactory getSources() {
        return sources;
    }

    public void setSources(ResourceFactory sources) {
        this.sources = sources;
    }

    public String[] getSourceExtensions() {
        return sourceExtensions;
    }

    public void setSourceExtensions(String[] sourceExtensions) {
        this.sourceExtensions = sourceExtensions;
    }

    public String getTargetExtension() {
        return targetExtension;
    }

    public void setTargetExtension(String targetExtension) {
        this.targetExtension = targetExtension;
    }

    public Babel getBabel() {
        return babel;
    }

    public void setBabel(Babel babel) {
        this.babel = babel;
    }

    public String[] getPresets() {
        return presets;
    }

    public void setPresets(String[] presets) {
        this.presets = presets;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void init(Config config) {
        sources = config.getInstance(PARAM_SOURCE_RESOURCE_FACTORY, sources);
        sourceExtensions = config.getParam(PARAM_SOURCE_EXTENSIONS, DEFAULT_SOURCE_EXTENSIONS);
        targetExtension = config.getParam(PARAM_TARGET_EXTENSION, DEFAULT_TARGET_EXTENSION);
        babel = config.getInstance(PARAM_BABEL, babel);
        presets = config.getParam(PARAM_PRESETS, DEFAULT_PRESETS);
        charset = Charset.forName(config.getParam(PARAM_CHARSET, DEFAULT_CHARSET));
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(sources, babel);
    }

    @Override
    public Resource getResource(String path) throws IOException {
        String pathNoExt = trimExt(path);
        Resource source = findSource(pathNoExt);
        return new BabelResource(source, pathNoExt + targetExtension);
    }

    private static String trimExt(String path) {
        return path.substring(0, path.length() - FilenameUtils.getExtension(path).length());
    }

    private Resource findSource(String pathNoExt) throws IOException {
        for (String ext : sourceExtensions) {
            String path = pathNoExt + ext;
            Resource resource = sources.getResource(path);
            if (resource.exists())
                return resource;
        }
        return null;
    }

    private String read(InputStream in) throws IOException {
        byte buf[] = new byte[8192];
        StringBuilder str = new StringBuilder();
        try {
            for (int n = in.read(buf); n >= 0; n = in.read(buf))
                str.append(new String(buf, 0, n, charset));
            return str.toString();
        } finally {
            in.close();
        }
    }

    private class BabelResource implements Resource {

        private final Resource source;
        private final String path;

        private BabelResource(Resource source, String path) {
            this.source = source;
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public boolean exists() {
            return source != null && source.exists();
        }

        @Override
        public long lastModified() {
            return source != null ? source.lastModified() : -1L;
        }

        @Override
        public InputStream openStream() throws IOException {
            if (source == null)
                throw new IOException(path + " does not exist");
            try {
                return new ByteArrayInputStream(babel.transform(read(source.openStream()), presets).getBytes(charset));
            } catch (Exception e) {
                throw new IOException("Can't transform " + source.getPath(), e);
            }
        }

    }

}
