package org.zenframework.easyservices.test.dynamic;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.Remote;

public interface RmiInputStream extends Remote, Closeable {

    byte[] read(int n) throws IOException;

}
