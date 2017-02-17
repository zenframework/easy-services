package org.zenframework.easyservices.js.babel;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class BabelPooledObjectFactory extends BasePooledObjectFactory<Babel> {

    @Override
    public Babel create() throws Exception {
        return new SimpleBabel();
    }

    @Override
    public PooledObject<Babel> wrap(Babel babel) {
        return new DefaultPooledObject<Babel>(babel);
    }

}