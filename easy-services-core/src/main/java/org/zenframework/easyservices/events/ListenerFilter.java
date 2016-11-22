package org.zenframework.easyservices.events;

public interface ListenerFilter<L extends Listener> {

    boolean accept(L listener);

}
