package org.zenframework.easyservices.net;

public class Environment {

    public static final String PROP_TCP_CLIENT_DEFAULT_POOLED = "easyservices.net.tcp.client.default.pooled";
    public static final String PROP_TCP_CLIENT_DEFAULT_BLOCKING = "easyservices.net.tcp.client.default.blocking";

    private static final String DEFAULT_TCP_CLIENT_DEFAULT_POOLED = "false";
    private static final String DEFAULT_TCP_CLIENT_DEFAULT_BLOCKING = "true";

    private static boolean tcpClientDefaultPooled = Boolean
            .parseBoolean(System.getProperty(PROP_TCP_CLIENT_DEFAULT_POOLED, DEFAULT_TCP_CLIENT_DEFAULT_POOLED));
    private static boolean tcpClientDefaultBlocking = Boolean
            .parseBoolean(System.getProperty(PROP_TCP_CLIENT_DEFAULT_BLOCKING, DEFAULT_TCP_CLIENT_DEFAULT_BLOCKING));
    private static TcpClientFactory tcpClientFactory = null;

    private Environment() {}

    public static boolean isTcpClientDefaultPooled() {
        if (tcpClientDefaultPooled) {
            try {
                Class.forName("org.apache.commons.pool2.ObjectPool");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Can't initialize commons-pool2. Is it in classpath?", e);
            }
        }
        return tcpClientDefaultPooled;
    }

    public static void setTcpClientDefaultPooled(boolean tcpClientDefaultPooled) {
        Environment.tcpClientDefaultPooled = tcpClientDefaultPooled;
    }

    public static boolean isTcpClientDefaultBlocking() {
        return tcpClientDefaultBlocking;
    }

    public static void setTcpClientDefaultBlocking(boolean tcpClientDefaultBlocking) {
        Environment.tcpClientDefaultBlocking = tcpClientDefaultBlocking;
    }

    public static TcpClientFactory getTcpClientFactory() {
        if (tcpClientFactory == null)
            tcpClientFactory = tcpClientDefaultPooled
                    ? new PooledTcpClientFactory(tcpClientDefaultBlocking ? new SimpleTcpClientFactory() : new NioTcpClientFactory())
                    : tcpClientDefaultBlocking ? new SimpleTcpClientFactory() : new NioTcpClientFactory();
        return tcpClientFactory;
    }

    public static void setTcpClientFactory(TcpClientFactory tcpClientFactory) {
        Environment.tcpClientFactory = tcpClientFactory;
    }

}
