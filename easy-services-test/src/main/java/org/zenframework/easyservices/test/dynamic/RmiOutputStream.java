package org.zenframework.easyservices.test.dynamic;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.Remote;

public interface RmiOutputStream extends Remote, Closeable {

    void write(byte[] buf) throws IOException;

}
