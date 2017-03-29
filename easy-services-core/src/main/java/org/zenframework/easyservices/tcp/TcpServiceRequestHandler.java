package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.SessionContextManager;
import org.zenframework.easyservices.impl.ServiceInvokerImpl;
import org.zenframework.easyservices.impl.SessionContextManagerImpl;
import org.zenframework.easyservices.net.TcpRequestHandler;

public class TcpServiceRequestHandler implements TcpRequestHandler {

    private final Map<String, ServiceSession> sessions = new HashMap<String, ServiceSession>();
    private final TcpRequestHeader header = new TcpRequestHeader();

    private SessionContextManager sessionContextManager = new SessionContextManagerImpl();
    private ServiceInvoker serviceInvoker = new ServiceInvokerImpl();

    private ServiceSession getSession(String sessionId) {
        synchronized (sessions) {
            ServiceSession session = sessions.get(sessionId);
            if (session == null) {
                session = new ServiceSession(sessionId, sessionContextManager.getSecureServiceRegistry(sessionId),
                        sessionContextManager.getSessionContextName(sessionId));
                sessions.put(sessionId, session);
            }
            return session;
        }
    }

    @Override
    public boolean handleRequest(SocketAddress clientAddr, InputStream in, OutputStream out) throws IOException {
        header.read(in);
        if (header.getSessionId() == null || header.getSessionId().isEmpty())
            header.setSessionId(UUID.randomUUID().toString());
        TcpServiceRequest request = new TcpServiceRequest(getSession(header.getSessionId()), header, in);
        TcpServiceResponse response = new TcpServiceResponse(header.getSessionId(), out);
        serviceInvoker.invoke(request, response);
        return true;
    }

}
