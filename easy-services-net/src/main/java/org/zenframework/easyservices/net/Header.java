package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Header {

    void read(InputStream in) throws IOException;

    void write(OutputStream out) throws IOException;

}
