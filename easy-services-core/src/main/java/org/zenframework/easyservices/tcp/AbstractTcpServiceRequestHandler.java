package org.zenframework.easyservices.tcp;

import org.zenframework.easyservices.*;
import org.zenframework.easyservices.impl.ServiceInvokerImpl;
import org.zenframework.easyservices.impl.SessionContextManagerImpl;
import org.zenframework.easyservices.net.Header;
import org.zenframework.easyservices.net.TcpRequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractTcpServiceRequestHandler<H extends Header, REQ extends ServiceRequest, RESP extends ServiceResponse> implements TcpRequestHandler {

    private final Map<String, ServiceSession> sessions = new HashMap<String, ServiceSession>();

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
        H header = newHeader();
        header.read(in);
        String sessionId = getSessionId(header);
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            setSessionId(header, sessionId);
        }
        serviceInvoker.invoke(newServiceRequest(getSession(sessionId), in, header), newServiceResponse(sessionId, out));
        return isKeepConnection(header);
    }

    abstract protected H newHeader();

    abstract protected String getSessionId(H header);

    abstract protected void setSessionId(H header, String sessionId);

    abstract protected REQ newServiceRequest(ServiceSession session, InputStream in, H header) throws IOException;

    abstract protected RESP newServiceResponse(String sessionId, OutputStream out);

    abstract protected boolean isKeepConnection(H header);

}
