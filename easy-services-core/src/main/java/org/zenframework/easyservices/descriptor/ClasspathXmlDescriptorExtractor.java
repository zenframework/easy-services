package org.zenframework.easyservices.descriptor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClasspathXmlDescriptorExtractor extends XmlDescriptorExtractor {

    public ClasspathXmlDescriptorExtractor(String path) {
        super(getUrls(path));
    }

    private static URL[] getUrls(String path) {
        List<URL> urls = new ArrayList<URL>();
        try {
            Enumeration<URL> e = ClasspathXmlDescriptorExtractor.class.getClassLoader().getResources(path);
            while (e.hasMoreElements())
                urls.add(e.nextElement());
            return urls.toArray(new URL[urls.size()]);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
