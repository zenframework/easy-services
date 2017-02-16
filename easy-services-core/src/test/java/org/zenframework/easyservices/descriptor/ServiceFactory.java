package org.zenframework.easyservices.descriptor;

import java.io.InputStream;

public interface ServiceFactory {

    Service getService(String name);

    InputStream getInputStream();

}
