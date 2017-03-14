package org.zenframework.easyservices.resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FilterResourceFactory implements ResourceFactory {

    private final Map<PathFilter, ResourceFactory> factories = new HashMap<PathFilter, ResourceFactory>();

    @Override
    public Resource getResource(String path) throws IOException {
        for (Map.Entry<PathFilter, ResourceFactory> entry : factories.entrySet())
            if (entry.getKey().accept(path))
                return entry.getValue().getResource(path);
        return null;
    }

    public void addFactory(PathFilter filter, ResourceFactory factory) {
        factories.put(filter, factory);
    }

    public void removeFactory(PathFilter filter) {
        factories.remove(filter);
    }

    public Map<PathFilter, ResourceFactory> getFactories() {
        return factories;
    }

    public void setFactories(Map<PathFilter, ResourceFactory> factories) {
        this.factories.clear();
        this.factories.putAll(factories);
    }

    public static interface PathFilter {

        boolean accept(String path);

    }

}
