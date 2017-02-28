package org.zenframework.easyservices.test.dynamic;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiOutputStreamImpl extends UnicastRemoteObject implements RmiOutputStream {

    private static final long serialVersionUID = 1L;

    private final OutputStream out;

    protected RmiOutputStreamImpl(OutputStream out) throws RemoteException {
        this.out = out;
    }

    @Override
    public void write(byte[] buf) throws IOException {
        out.write(buf);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
