package org.zenframework.easyservices.http;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.zenframework.easyservices.ServiceResponse;

public class HttpServiceResponse extends ServiceResponse {

    private final HttpServletResponse response;
    private final HttpErrorMapper errorMapper;

    public HttpServiceResponse(HttpServletResponse response, HttpErrorMapper errorMapper) {
        this.response = response;
        this.errorMapper = errorMapper;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public void sendSuccess() throws IOException {}

    @Override
    public void sendError(Throwable e) {
        response.setStatus(getStatus(e));
    }

    private int getStatus(Throwable e) {
        return errorMapper != null ? errorMapper.getStatus(e) : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

}
