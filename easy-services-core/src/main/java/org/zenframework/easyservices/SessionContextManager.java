package org.zenframework.easyservices;

import javax.naming.Context;
import javax.naming.Name;

public interface SessionContextManager {

    Context getSecureServiceRegistry(String sessionId);

    Name getSessionContextName(String sessionId);

}
