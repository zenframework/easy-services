package org.zenframework.easyservices.update;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ValueUpdaterImpl implements ValueUpdater {

    public static final ValueUpdaterImpl INSTANCE = new ValueUpdaterImpl();

    private final Collection<UpdateAdapter> adapters = Arrays.<UpdateAdapter> asList(new CollectionUpdateAdapter(), new MapUpdateAdapter(),
            new ReflectiveUpdateAdapter());

    @Override
    public void update(Object oldValue, Object newValue) {
        if (oldValue.getClass().isArray() && newValue.getClass().isArray()) {
            int length = Array.getLength(oldValue);
            for (int i = 0; i < length; i++)
                Array.set(oldValue, i, Array.get(newValue, i));
        } else {
            UpdateAdapter adapter = findAdapter(oldValue.getClass());
            adapter.update(oldValue, newValue, this);
        }
    }

    public Collection<UpdateAdapter> getAdapters() {
        return adapters;
    }

    public void setAdapters(Collection<UpdateAdapter> adapters) {
        this.adapters.clear();
        this.adapters.addAll(adapters);
    }

    private UpdateAdapter findAdapter(Class<?> cls) {
        UpdateAdapter candidate = null;
        for (UpdateAdapter adapter : adapters) {
            if (adapter.getValueClass().isAssignableFrom(cls)
                    && (candidate == null || candidate.getValueClass().isAssignableFrom(adapter.getValueClass())))
                candidate = adapter;
        }
        if (candidate == null)
            throw new UpdateException("Update adapter for " + cls + " not found");
        return candidate;
    }

}
