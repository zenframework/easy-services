package org.zenframework.easyservices.config;

public interface Configurable {

    void init(Config config);

    void destroy(Config config);

}
