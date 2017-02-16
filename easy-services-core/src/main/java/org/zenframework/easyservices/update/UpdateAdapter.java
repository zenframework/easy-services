package org.zenframework.easyservices.update;

public interface UpdateAdapter<T> {

    Class<T> getValueClass();

    void update(T oldValue, T newValue, ValueUpdater updater);

}
