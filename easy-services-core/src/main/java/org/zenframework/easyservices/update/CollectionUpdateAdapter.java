package org.zenframework.easyservices.update;

import java.util.Collection;

@SuppressWarnings("rawtypes")
public class CollectionUpdateAdapter implements UpdateAdapter<Collection> {

    @Override
    public Class<Collection> getValueClass() {
        return Collection.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update(Collection oldValue, Collection newValue, ValueUpdater updater) {
        if (oldValue == newValue || newValue == null)
            return;
        if (oldValue == null)
            throw new UpdateException("Old value is null");
        oldValue.clear();
        oldValue.addAll(newValue);
    }

}
