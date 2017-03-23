package org.zenframework.easyservices.util.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class BlockInputStream extends InputStream {

    protected final InputStream in;
    protected byte buf[] = null;
    protected int blockSize = 0, pos = 0;

    public BlockInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        if (blockSize < 0)
            return -1;
        if (pos >= blockSize) {
            readBlock();
            if (blockSize < 0)
                return -1;
        }
        return buf[pos++] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (blockSize < 0)
            return -1;
        if ((off | len | (off + len) | (b.length - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
        else if (len == 0)
            return 0;
        if (pos >= blockSize) {
            readBlock();
            if (blockSize < 0)
                return -1;
        }
        int read = 0;
        while (read < len && blockSize >= 0) {
            int part = Math.min(blockSize - pos, len - read);
            System.arraycopy(buf, pos, b, off + read, part);
            pos += part;
            read += part;
            if (pos >= blockSize)
                readBlock();
        }
        return read;
    }

    @Override
    public int available() throws IOException {
        return blockSize < 0 || buf == null ? 0 : blockSize - pos;
    }

    @Override
    public void close() throws IOException {
        while (blockSize >= 0)
            readBlock();
    }

    protected final int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    protected void readBlock() throws IOException {
        blockSize = readInt();
        if (blockSize > 0) {
            if (buf == null || blockSize > buf.length)
                buf = new byte[blockSize];
            in.read(buf, 0, blockSize);
            pos = 0;
        }
    }

}
