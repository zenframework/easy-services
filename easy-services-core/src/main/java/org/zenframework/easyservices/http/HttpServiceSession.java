package org.zenframework.easyservices.http;

import javax.naming.Context;
import javax.naming.Name;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.zenframework.easyservices.ServiceSession;

public class HttpServiceSession extends ServiceSession implements HttpSessionBindingListener {

    public HttpServiceSession(String id, Context serviceRegistry, Name sessionContextName) {
        super(id, serviceRegistry, sessionContextName);
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {}

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        invalidate();
    }

}
