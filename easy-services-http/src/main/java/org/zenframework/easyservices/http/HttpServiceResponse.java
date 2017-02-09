package org.zenframework.easyservices.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.zenframework.easyservices.ServiceResponse;

public class HttpServiceResponse implements ServiceResponse {

    private final HttpServletResponse response;
    private final ErrorMapper errorMapper;

    public HttpServiceResponse(HttpServletResponse response, ErrorMapper errorMapper) {
        this.response = response;
        this.errorMapper = errorMapper;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public OutputStream getErrorStream(Throwable e) {
        return new ErrorOutputStream(errorMapper != null ? errorMapper.getStatus(e) : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private class ErrorOutputStream extends ByteArrayOutputStream {

        private final int status;

        private ErrorOutputStream(int status) {
            this.status = status;
        }

        @Override
        public void close() throws IOException {
            super.close();
            response.sendError(status, new String(toByteArray(), response.getCharacterEncoding()));
        }

    }

}
