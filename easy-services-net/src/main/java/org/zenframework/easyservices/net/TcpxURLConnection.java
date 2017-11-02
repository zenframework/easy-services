package org.zenframework.easyservices.net;

import java.net.URL;

public class TcpxURLConnection<REQ extends Header, RESP extends Header> extends AbstractTcpURLConnection<REQ, RESP> {

    public TcpxURLConnection(URL url) {
        super(url);
    }

    private REQ requestHeader;
    private RESP responseHeader;

    @Override
    public REQ getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(REQ requestHeader) {
        this.requestHeader = requestHeader;
    }

    @Override
    public RESP getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(RESP responseHeader) {
        this.responseHeader = responseHeader;
    }

}
