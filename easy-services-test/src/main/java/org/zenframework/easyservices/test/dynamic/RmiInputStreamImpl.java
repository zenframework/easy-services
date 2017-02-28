package org.zenframework.easyservices.test.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class RmiInputStreamImpl extends UnicastRemoteObject implements RmiInputStream {

    private static final long serialVersionUID = 1L;

    private final InputStream in;

    protected RmiInputStreamImpl(InputStream in) throws RemoteException {
        this.in = in;
    }

    @Override
    public byte[] read(int size) throws IOException {
        byte[] buf = new byte[size];
        int n = in.read(buf);
        return n < 0 ? null : n == size ? buf : Arrays.copyOf(buf, n);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

}
