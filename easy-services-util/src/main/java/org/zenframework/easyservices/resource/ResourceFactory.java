package org.zenframework.easyservices.resource;

import java.io.IOException;

public interface ResourceFactory {

    Resource getResource(String path) throws IOException;

}
