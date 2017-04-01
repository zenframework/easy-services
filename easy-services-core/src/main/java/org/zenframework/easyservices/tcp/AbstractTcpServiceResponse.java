package org.zenframework.easyservices.tcp;

import java.io.IOException;
import java.io.OutputStream;

import org.zenframework.easyservices.ServiceResponse;
import org.zenframework.easyservices.net.Header;
import org.zenframework.easyservices.util.io.BlockOutputStream;

public abstract class AbstractTcpServiceResponse<H extends Header> extends ServiceResponse {

    protected final H header;
    protected final OutputStream out;

    protected AbstractTcpServiceResponse(H header, OutputStream out) {
        this.header = header;
        this.out = out;
    }

    @Override
    protected final OutputStream getInternalOutputStream() throws IOException {
        header.write(out);
        return new BlockOutputStream(out);
    }

}
