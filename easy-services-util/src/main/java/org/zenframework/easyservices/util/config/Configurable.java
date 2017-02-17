package org.zenframework.easyservices.util.config;

public interface Configurable {

    void init(Config config);

    void destroy(Config config);

}
