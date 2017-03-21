package org.zenframework.easyservices.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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

public class TcpServiceServer {

    private static final Logger LOG = LoggerFactory.getLogger(TcpServiceServer.class);

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

    private class ClientThread extends Thread {

        private final Socket socket;

        public ClientThread(Socket socket) {
            super("Client-" + socket.getInetAddress() + ':' + socket.getPort());
            this.socket = socket;
        }

        @Override
        public void run() {
            InputStream in = null;
            TcpRequestHeader header = null;
            try {
                in = new BlockInputStream(socket.getInputStream());
                header = new TcpRequestHeader();
                header.read(in);
            } catch (EOFException e) {} catch (IOException e) {
                IOUtils.closeQuietly(in);
            }
            if (header != null) {
                try {
                    String sessionId = header.getSessionId();
                    if (sessionId == null || sessionId.isEmpty())
                        sessionId = UUID.randomUUID().toString();
                    serviceInvoker.invoke(
                            new TcpServiceRequest(new ServiceSession(sessionId, sessionContextManager.getSecureServiceRegistry(sessionId),
                                    sessionContextManager.getSessionContextName(sessionId)), header, in),
                            new TcpServiceResponse(sessionId, socket.getOutputStream()));
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                    IOUtils.closeQuietly(socket);
                }
            }
        }

    }

}
