package org.zenframework.easyservices.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.ServiceInvoker;
import org.zenframework.easyservices.impl.ServiceInvokerImpl;

public class ServiceServer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceServer.class);

    private ServiceInvoker serviceInvoker = new ServiceInvokerImpl();

    private int port;

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                new Thread(new Client(socket), "Client-" + socket.getInetAddress() + ':' + socket.getPort()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Client implements Runnable {

        private final Socket socket;

        public Client(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                serviceInvoker.invoke(new SocketServiceRequest(null, socket.getInputStream()), new SocketServiceResponse(socket.getOutputStream()));
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(socket);
            }
        }

    }

}
