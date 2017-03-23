package org.zenframework.easyservices.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public class TcpServer {

    private static final Logger LOG = LoggerFactory.getLogger(TcpServer.class);

    private final AtomicBoolean active = new AtomicBoolean(false);
    private final ServerSocket serverSocket;
    private final Thread serverThread;
    private TcpRequestHandler handler;

    public TcpServer(final int port, TcpRequestHandler handler) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.handler = handler;
        this.serverThread = new Thread(new Runnable() {

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
            try {
                InputStream socketIn = socket.getInputStream();
                OutputStream socketOut = socket.getOutputStream();
                while (true)
                    handler.handleRequest(new BlockInputStream(socketIn), new BlockOutputStream(socketOut));
            } catch (IOException e) {
                if (!(e instanceof EOFException))
                    LOG.error(e.getMessage(), e);
                IOUtils.closeQuietly(socket);
            }
        }

    }

}
