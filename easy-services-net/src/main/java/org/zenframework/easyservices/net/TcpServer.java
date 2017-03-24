package org.zenframework.easyservices.net;

public interface TcpServer {

    TcpRequestHandler getRequestHandler();

    void setRequestHandler(TcpRequestHandler requestHandler);

    boolean isActive();

    void start();

    void stop() throws InterruptedException;

}
