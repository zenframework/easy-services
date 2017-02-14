package org.zenframework.easyservices.test.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.zenframework.easyservices.ValueTransfer;
import org.zenframework.easyservices.annotations.Value;

public interface StreamFactory {

    @Value(transfer = ValueTransfer.REF)
    InputStream getInputStream() throws IOException;

    @Value(transfer = ValueTransfer.REF)
    OutputStream getOuptputStream() throws IOException;

}
