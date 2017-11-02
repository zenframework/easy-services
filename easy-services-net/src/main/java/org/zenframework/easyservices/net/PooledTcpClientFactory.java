package org.zenframework.easyservices.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class PooledTcpClientFactory implements TcpClientFactory {

    private final Map<String, ObjectPool<TcpClient>> pools = new HashMap<String, ObjectPool<TcpClient>>();
    private final TcpClientFactory tcpClientFactory;

    public PooledTcpClientFactory(final TcpClientFactory tcpClientFactory) {
        this.tcpClientFactory = tcpClientFactory;
    }

    @Override
    public TcpClient getTcpClient(String host, int port) throws IOException {
        try {
            final ObjectPool<TcpClient> pool = getPool(host, port);
            final TcpClient client = pool.borrowObject();
            return new TcpClient() {

                @Override
                public void close() throws IOException {
                    try {
                        pool.returnObject(this);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return client.getInputStream();
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    return client.getOutputStream();
                }

                @Override
                public String getHost() {
                    return client.getHost();
                }

                @Override
                public int getPort() {
                    return client.getPort();
                }
                
            };
        } catch (Exception e) {
            if (e instanceof IOException)
                throw (IOException) e;
            throw new IllegalStateException(e);
        }
    }

    private ObjectPool<TcpClient> getPool(final String host, final int port) {
        ObjectPool<TcpClient> pool;
        synchronized (pools) {
            pool = pools.get(host + ':' + port);
            if (pool == null) {
                pool = new GenericObjectPool<>(new BasePooledObjectFactory<TcpClient>() {

                    @Override
                    public TcpClient create() throws Exception {
                        return tcpClientFactory.getTcpClient(host, port);
                    }

                    @Override
                    public PooledObject<TcpClient> wrap(TcpClient obj) {
                        return new DefaultPooledObject<TcpClient>(obj) {

                            @Override
                            public void invalidate() {
                                super.invalidate();
                                try {
                                    this.getObject().close();
                                } catch (IOException e) {
                                    // Ignore
                                }
                            }
                            
                        };
                    }

                });
                pools.put(host + ':' + port, pool);
            }
        }
        return pool;
    }

}
