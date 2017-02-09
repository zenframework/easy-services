package org.zenframework.easyservices.test.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamFactoryImpl implements StreamFactory {

    private final int inputStreamSize;

    public StreamFactoryImpl(int inputStreamSize) {
        this.inputStreamSize = inputStreamSize;
    }

    @Override
    public InputStream getInputStream() {
        return new VoidInputStream(inputStreamSize);
    }

    @Override
    public OutputStream getOuptputStream() {
        return new VoidOutputStream();
    }

    private static class VoidInputStream extends InputStream {

        private int size;

        private VoidInputStream(int size) {
            this.size = size;
        }

        @Override
        public int read() throws IOException {
            return size-- > 0 ? 0 : -1;
        }

    }

    private static class VoidOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {}

    }

}
