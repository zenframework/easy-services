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
import org.zenframework.easyservices.net.DefaultHeader;
import org.zenframework.easyservices.net.TcpRequestHandler;

public class TcpServiceRequestHandler implements TcpRequestHandler {

    private final Map<String, ServiceSession> sessions = new HashMap<String, ServiceSession>();
    private final DefaultHeader header = new DefaultHeader();

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
        String sessionId = header.getString(TcpURLHandler.HEADER_SESSION_ID);
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            header.setField(TcpURLHandler.HEADER_SESSION_ID, sessionId);
        }
        TcpServiceRequest request = new TcpServiceRequest(getSession(sessionId), in, header);
        TcpServiceResponse response = new TcpServiceResponse(sessionId, out);
        serviceInvoker.invoke(request, response);
        return false;
    }

}
