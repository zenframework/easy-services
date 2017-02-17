package org.zenframework.easyservices.js.babel;

import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.util.config.Config;
import org.zenframework.easyservices.util.config.Configurable;

public class PooledBabel implements Babel, Configurable {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(PooledBabel.class);

    public static final String PARAM_POOL = "pool";

    private ObjectPool<Babel> pool = new DefaultBabelPool();

    public PooledBabel() {}

    public PooledBabel(ObjectPool<Babel> pool) {
        this.pool = pool;
    }

    public ObjectPool<Babel> getPool() {
        return pool;
    }

    public void setPool(ObjectPool<Babel> pool) {
        this.pool = pool;
    }

    @Override
    public String transform(String script, String... presets) throws Exception {
        Babel babel = pool.borrowObject();
        try {
            return babel.transform(script, presets);
        } finally {
            pool.returnObject(babel);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(Config config) {
        pool = (ObjectPool<Babel>) config.getInstance(PARAM_POOL, pool);
    }

    @Override
    public void destroy(Config config) {
        config.destroyInstances(pool);
    }

}
