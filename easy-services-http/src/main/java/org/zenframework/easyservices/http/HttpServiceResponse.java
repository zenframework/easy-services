package org.zenframework.easyservices.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

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
    public OutputStream getErrorOutputStream(final Throwable e) {
        return new ByteArrayOutputStream() {

            @Override
            public void close() throws IOException {
                super.close();
                response.sendError(getStatus(e), new String(toByteArray(), response.getCharacterEncoding()));
            }

        };
    }

    @Override
    public Writer getWriter() throws IOException {
        return response.getWriter();
    }

    @Override
    public Writer getErrorWriter(final Throwable e) throws IOException {
        return new StringWriter() {

            @Override
            public void close() throws IOException {
                super.close();
                response.sendError(getStatus(e), toString());
            }

        };
    }

    private int getStatus(Throwable e) {
        return errorMapper != null ? errorMapper.getStatus(e) : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

}
