package org.zenframework.easyservices.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTcpServer implements TcpServer {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleTcpServer.class);

    private final AtomicBoolean active = new AtomicBoolean(false);
    private final ServerSocket server;
    private final Thread serverThread;
    private TcpRequestHandler handler;

    public SimpleTcpServer(int port) throws IOException {
        this.server = new ServerSocket(port);
        this.serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (active.get()) {
                        Socket socket = server.accept();
                        if (active.get())
                            new ClientThread(socket).start();
                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                } finally {
                    IOUtils.closeQuietly(server);
                }
            }

        }, "SimpleTcpServer-" + server.getLocalSocketAddress());
    }

    public SimpleTcpServer(final int port, TcpRequestHandler handler) throws IOException {
        this(port);
        this.handler = handler;
    }

    @Override
    public TcpRequestHandler getRequestHandler() {
        return handler;
    }

    @Override
    public void setRequestHandler(TcpRequestHandler requestHandler) {
        this.handler = requestHandler;
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public void start() {
        active.set(true);
        serverThread.start();
    }

    @Override
    public void stop() throws InterruptedException {
        active.set(false);
        try {
            new Socket(InetAddress.getLoopbackAddress(), server.getLocalPort()).close();
        } catch (IOException e) {}
        while (serverThread.isAlive())
            Thread.sleep(100);
    }

    private class ClientThread extends Thread {

        private final Socket socket;

        public ClientThread(Socket socket) {
            super("Client-" + socket.getRemoteSocketAddress());
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                SocketAddress clientAddr = socket.getRemoteSocketAddress();
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                while (active.get() && handler.handleRequest(clientAddr, in, out))
                    ;
                in.read();
            } catch (IOException e) {
                if (!(e instanceof EOFException))
                    LOG.error(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(socket);
            }
        }

    }

}
