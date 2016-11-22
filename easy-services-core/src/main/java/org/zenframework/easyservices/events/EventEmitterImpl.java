package org.zenframework.easyservices.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventEmitterImpl<L extends Listener> implements EventEmitter<L> {

    private static final Logger LOG = LoggerFactory.getLogger(EventEmitterImpl.class);

    private final Set<L> listeners = Collections.synchronizedSet(new HashSet<L>());

    @Override
    public void addListener(L listener) {
        if (listeners.add(listener))
            LOG.info("Listener " + listener + " added");
        else
            LOG.info("Listener " + listener + " already exists");
    }

    @Override
    public void removeListener(L listener) {
        if (listeners.remove(listener))
            LOG.info("Listener " + listener + " removed");
        else
            LOG.info("Listener " + listener + " does not exist");
    }

    public void publishEvent(final Event<?> event, boolean fork, final ListenerFilter<L> listenerFilter) {
        final Collection<L> listeners = new ArrayList<L>(this.listeners);
        Runnable task = new Runnable() {

            @Override
            public void run() {
                for (final L listener : listeners) {
                    if (listenerFilter == null || listenerFilter.accept(listener)) {
                        try {
                            listener.onEvent(event);
                        } catch (Throwable e) {
                            LOG.info("Listener " + listener + " onEvent() failed.", e);
                            removeListener(listener);
                        }
                    }
                }
            }

        };
        if (fork)
            new Thread(task, "Event-" + event.getType() + "-" + event.getId()).start();
        else
            task.run();
    }

}
