package org.zenframework.easyservices.util.resource;

import java.io.IOException;

public interface ResourceFactory {

    Resource getResource(String path) throws IOException;

}
