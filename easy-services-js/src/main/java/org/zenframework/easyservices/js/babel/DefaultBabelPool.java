package org.zenframework.easyservices.js.babel;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.zenframework.easyservices.config.Config;
import org.zenframework.easyservices.config.Configurable;

public class DefaultBabelPool extends GenericObjectPool<Babel> implements Configurable {

    public static final String PARAM_POOL_SIZE = "pool.size";

    public static final int DEFAULT_POOL_SIZE = 1;


    public DefaultBabelPool() {
        super(new BabelPooledObjectFactory());
    }


    @Override
    public void init(Config config) {
        setMaxTotal(config.getParam(PARAM_POOL_SIZE, DEFAULT_POOL_SIZE));
    }


    @Override
    public void destroy(Config config) {
        clear();
    }

}
