package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zenframework.easyservices.util.io.QueueInputStream;

public class NioTcpClient implements TcpClient {

    private static final Logger LOG = LoggerFactory.getLogger(NioTcpClient.class);

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

    private final AsynchronousSocketChannel client;
    private final String host;
    private final int port;
    private final QueueInputStream in;
    private final ClientOutputStream out;

    public NioTcpClient(String host, int port) throws IOException {
        this.client = AsynchronousSocketChannel.open();
        this.host = host;
        this.port = port;
        in = new QueueInputStream();
        out = new ClientOutputStream(client);
        Future<Void> result = client.connect(new InetSocketAddress(host, port));
        try {
            result.get();
        } catch (Exception e) {
            client.close();
        }
        ReadAttachment readAttach = new ReadAttachment(client, in, ByteBuffer.allocate(8192));
        client.read(readAttach.buf, readAttach, READ_HANDLER);
    }

    @Override
    public InputStream getInputStream() {
        return in;
    }

    @Override
    public OutputStream getOutputStream() {
        return out;
    }

    @Override
    public void close() throws IOException {
        in.finish();
        client.close();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
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
