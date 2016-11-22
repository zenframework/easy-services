package org.zenframework.easyservices.events;

import java.util.Date;
import java.util.UUID;

public class Event<T> {

    private final UUID id;
    private final Date created;
    private final T type;
    private final Object payload;

    public Event(UUID id, Date created, T type, Object payload) {
        this.id = id;
        this.created = created;
        this.type = type;
        this.payload = payload;
    }

    public Event(T type, Object payload) {
        this(UUID.randomUUID(), new Date(), type, payload);
    }

    public UUID getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public T getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    @SuppressWarnings("unchecked")
    public <P> P getPayload(Class<P> payloadClass) {
        return (P) payload;
    }

}
