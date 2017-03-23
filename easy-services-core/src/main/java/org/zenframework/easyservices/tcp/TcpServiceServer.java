package org.zenframework.easyservices.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.ServiceSession;
import org.zenframework.easyservices.SessionContextManager;
import org.zenframework.easyservices.impl.ServiceInvokerImpl;
import org.zenframework.easyservices.impl.SessionContextManagerImpl;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public class TcpServiceServer {

    private static final Logger LOG = LoggerFactory.getLogger(TcpServiceServer.class);

    private final Map<String, ServiceSession> sessions = new HashMap<String, ServiceSession>();

    private SessionContextManager sessionContextManager = new SessionContextManagerImpl();
    private ServiceInvoker serviceInvoker = new ServiceInvokerImpl();

    private final AtomicBoolean active = new AtomicBoolean(false);
    private final ServerSocket serverSocket;
    private final Thread serverThread;

    public TcpServiceServer(final int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (active.get()) {
                        Socket socket = serverSocket.accept();
                        if (active.get())
                            new ClientThread(socket).start();
                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                } finally {
                    IOUtils.closeQuietly(serverSocket);
                }
            }

        }, "ServiceServer-" + serverSocket.getLocalSocketAddress());
    }

    public void start() {
        active.set(true);
        serverThread.start();
    }

    public void stop() throws InterruptedException {
        active.set(false);
        try {
            new Socket(InetAddress.getLoopbackAddress(), serverSocket.getLocalPort()).close();
        } catch (IOException e) {}
        while (serverThread.isAlive())
            Thread.sleep(100);
    }

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

    private class ClientThread extends Thread {

        private final Socket socket;

        public ClientThread(Socket socket) {
            super("Client-" + socket.getInetAddress() + ':' + socket.getPort());
            this.socket = socket;
        }

        @Override
        public void run() {
            TcpRequestHeader header = new TcpRequestHeader();
            try {
                InputStream socketIn = socket.getInputStream();
                OutputStream socketOut = socket.getOutputStream();
                while (true) {
                    InputStream in = new BlockInputStream(socketIn);
                    OutputStream out = new BlockOutputStream(socketOut);
                    header.read(in);
                    if (header.getSessionId() == null || header.getSessionId().isEmpty())
                        header.setSessionId(UUID.randomUUID().toString());
                    TcpServiceRequest request = new TcpServiceRequest(getSession(header.getSessionId()), header, in);
                    TcpServiceResponse response = new TcpServiceResponse(header.getSessionId(), out);
                    serviceInvoker.invoke(request, response);
                }
            } catch (IOException e) {
                if (!(e instanceof EOFException))
                    LOG.error(e.getMessage(), e);
                IOUtils.closeQuietly(socket);
            }
        }

    }

}
