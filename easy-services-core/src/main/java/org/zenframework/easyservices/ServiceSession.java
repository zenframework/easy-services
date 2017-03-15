package org.zenframework.easyservices;

import javax.naming.Context;

public interface ServiceSession {

    String getId();

    Context getServiceRegistry();

    String bindService(String localName, Object service);

    void unbindService(String name);

}
