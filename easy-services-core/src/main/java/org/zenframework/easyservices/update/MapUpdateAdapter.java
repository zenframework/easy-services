package org.zenframework.easyservices.update;

import java.util.Map;

@SuppressWarnings("rawtypes")
public class MapUpdateAdapter implements UpdateAdapter<Map> {

    @Override
    public Class<Map> getValueClass() {
        return Map.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(Map oldValue, Map newValue, ValueUpdater updater) {
        if (oldValue == newValue || newValue == null)
            return;
        if (oldValue == null)
            throw new UpdateException("Old value is null");
        oldValue.clear();
        oldValue.putAll(newValue);
    }

}
