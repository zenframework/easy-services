package org.zenframework.easyservices.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.util.io.QueueInputStream;

public class NioTcpServer implements TcpServer {

    private static final Logger LOG = LoggerFactory.getLogger(NioTcpServer.class);

    private static final ThreadFactory SERVER_THREAD_FACTORY = new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "NioTcpServer");
        }

    };

    private static CompletionHandler<AsynchronousSocketChannel, ConnectionAttachment> CONN_HANDLER = new CompletionHandler<AsynchronousSocketChannel, ConnectionAttachment>() {

        @Override
        public void completed(final AsynchronousSocketChannel client, final ConnectionAttachment connAttach) {
            if (!connAttach.server.isOpen())
                return;
            connAttach.server.accept(connAttach, this);
            final ReadAttachment readAttach = new ReadAttachment(client, new QueueInputStream(), ByteBuffer.allocate(8192));
            client.read(readAttach.buf, readAttach, READ_HANDLER);
            try {
                final SocketAddress clientAddr = client.getRemoteAddress();
                final OutputStream out = new ClientOutputStream(client);
                connAttach.executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        Thread.currentThread().setName("Client-" + clientAddr.toString());
                        try {
                            while (connAttach.handler.handleRequest(clientAddr, readAttach.in, out))
                                ;
                        } catch (IOException e) {
                            if (!(e instanceof EOFException))
                                LOG.error(e.getMessage(), e);
                            IOUtils.closeQuietly(client);
                        }
                    }

                });
            } catch (IOException e) {
                LOG.error("Start client thread failed", e);
            }
        }

        @Override
        public void failed(Throwable e, ConnectionAttachment attach) {
            if (!(e instanceof AsynchronousCloseException))
                LOG.error("Accept connection failed", e);
            IOUtils.closeQuietly(attach.server);
        }

    };

    private static CompletionHandler<Integer, ReadAttachment> READ_HANDLER = new CompletionHandler<Integer, ReadAttachment>() {

        @Override
        public void completed(Integer result, ReadAttachment attach) {
            if (result == -1) {
                attach.in.finish();
                IOUtils.closeQuietly(attach.client);
            } else {
                if (result > 0) {
                    byte bytes[] = new byte[result];
                    attach.buf.rewind();
                    attach.buf.get(bytes, 0, result);
                    attach.in.addBuffer(bytes);
                }
                attach.buf.rewind();
                attach.client.read(attach.buf, attach, READ_HANDLER);
            }
        }

        @Override
        public void failed(Throwable e, ReadAttachment attach) {
            if (!(e instanceof AsynchronousCloseException))
                LOG.error("Client R/W failed", e);
            attach.in.finish();
            IOUtils.closeQuietly(attach.client);
        }

    };

    private final AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor(SERVER_THREAD_FACTORY));
    private final AsynchronousServerSocketChannel server;
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();
    private TcpRequestHandler handler;

    public NioTcpServer(int port) throws IOException {
        server = AsynchronousServerSocketChannel.open(group);
        server.bind(new InetSocketAddress(port));
    }

    public NioTcpServer(final int port, TcpRequestHandler handler) throws IOException {
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
        return server.isOpen();
    }

    @Override
    public void start() {
        server.accept(new ConnectionAttachment(server, clientExecutor, handler), CONN_HANDLER);
    }

    @Override
    public void stop() throws InterruptedException {
        IOUtils.closeQuietly(server);
        try {
            group.shutdownNow();
        } catch (IOException e) {}
        group.awaitTermination(5, TimeUnit.SECONDS);
        clientExecutor.shutdown();
    }

    private static class ConnectionAttachment {

        final AsynchronousServerSocketChannel server;
        final ExecutorService executor;
        final TcpRequestHandler handler;

        public ConnectionAttachment(AsynchronousServerSocketChannel server, ExecutorService executor, TcpRequestHandler handler) {
            this.server = server;
            this.executor = executor;
            this.handler = handler;
        }

    }

    private static class ReadAttachment {

        final AsynchronousSocketChannel client;
        final QueueInputStream in;
        final ByteBuffer buf;

        public ReadAttachment(AsynchronousSocketChannel client, QueueInputStream in, ByteBuffer buf) {
            this.client = client;
            this.in = in;
            this.buf = buf;
        }

    }

    private static class ClientOutputStream extends OutputStream {

        private final AsynchronousSocketChannel client;

        ClientOutputStream(AsynchronousSocketChannel client) {
            this.client = client;
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[] { (byte) b });
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            client.write(ByteBuffer.wrap(b, off, len));
        }

    }

}
