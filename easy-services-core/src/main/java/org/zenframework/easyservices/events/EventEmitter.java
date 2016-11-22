package org.zenframework.easyservices.events;

public interface EventEmitter<L extends Listener> {

    void addListener(L listener);

    void removeListener(L listener);

}
