package org.zenframework.easyservices.impl;

import java.util.HashMap;
import java.util.Map;

import org.zenframework.easyservices.update.ByteArrayUpdater;
import org.zenframework.easyservices.update.UpdateAdapter;
import org.zenframework.easyservices.update.UpdateException;
import org.zenframework.easyservices.update.ValueUpdater;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ValueUpdaterImpl implements ValueUpdater {

    private final Map<Class, UpdateAdapter> adapters = getDefaultAdapters();

    @Override
    public void update(Object oldValue, Object newValue) {
        UpdateAdapter adapter = findAdapter(oldValue.getClass());
        adapter.update(oldValue, newValue, this);
    }

    public Map<Class, UpdateAdapter> getAdapters() {
        return adapters;
    }

    public void setAdapters(Map<Class, UpdateAdapter> adapters) {
        this.adapters.clear();
        this.adapters.putAll(adapters);
    }

    private UpdateAdapter findAdapter(Class<?> cls) {
        Map.Entry<Class, UpdateAdapter> candidate = null;
        for (Map.Entry<Class, UpdateAdapter> entry : adapters.entrySet()) {
            if (entry.getKey().isAssignableFrom(cls) && (candidate == null || candidate.getKey().isAssignableFrom(entry.getKey())))
                candidate = entry;
        }
        if (candidate == null)
            throw new UpdateException("Update adapter for " + cls + " not found");
        return candidate.getValue();
    }

    private static Map<Class, UpdateAdapter> getDefaultAdapters() {
        Map<Class, UpdateAdapter> adapters = new HashMap<Class, UpdateAdapter>();
        adapters.put(byte[].class, new ByteArrayUpdater());
        return adapters;
    }

}
