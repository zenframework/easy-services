package org.zenframework.easyservices.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class QueueInputStream extends InputStream {

    private final List<byte[]> queue = new LinkedList<byte[]>();
    private byte[] buf = null;
    private int pos;
    private boolean opened = true;

    @Override
    public int read() throws IOException {
        return prepareBuf() ? buf[pos++] & 0xFF : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if ((off | len | (off + len) | (b.length - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
        else if (len == 0)
            return 0;
        if (!prepareBuf())
            return -1;
        int read = 0;
        do {
            int part = Math.min(buf.length - pos, len - read);
            System.arraycopy(buf, pos, b, off + read, part);
            pos += part;
            read += part;
        } while (read < len && prepareBuf());
        return read;
    }

    @Override
    public int available() throws IOException {
        return !opened || buf == null ? 0 : buf.length - pos;
    }

    public void addBuffer(byte[] buf) {
        synchronized (queue) {
            queue.add(buf);
            queue.notify();
        }
    }

    public void finish() {
        opened = false;
        synchronized (queue) {
            queue.notify();
        }
    }

    private boolean prepareBuf() throws IOException {
        if (buf != null && pos < buf.length)
            return true;
        synchronized (queue) {
            if (opened && queue.isEmpty()) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
            }
            if (!queue.isEmpty()) {
                buf = queue.remove(0);
                pos = 0;
                return true;
            }
            return false;
        }
    }

}
