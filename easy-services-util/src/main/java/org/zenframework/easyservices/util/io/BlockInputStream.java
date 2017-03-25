package org.zenframework.easyservices.util.io;

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
        return prepareBlock() ? buf[pos++] & 0xFF : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if ((off | len | (off + len) | (b.length - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
        else if (len == 0)
            return 0;
        if (!prepareBlock())
            return -1;
        int read = 0;
        do {
            int part = Math.min(blockSize - pos, len - read);
            System.arraycopy(buf, pos, b, off + read, part);
            pos += part;
            read += part;
        } while (read < len && prepareBlock());
        return read;
    }

    @Override
    public int available() throws IOException {
        return blockSize < 0 || buf == null ? 0 : blockSize - pos;
    }

    @Override
    public void close() throws IOException {
        while (prepareBlock())
            pos = blockSize;
    }

    protected final int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    protected boolean prepareBlock() throws IOException {
        if (blockSize < 0)
            return false;
        if (buf != null && pos < blockSize)
            return true;
        blockSize = readInt();
        if (blockSize == 0)
            throw new IllegalStateException("Unexpected zero block length");
        if (blockSize > 0) {
            if (buf == null || blockSize > buf.length)
                buf = new byte[blockSize];
            in.read(buf, 0, blockSize);
            pos = 0;
        }
        return blockSize > 0;
    }

}
