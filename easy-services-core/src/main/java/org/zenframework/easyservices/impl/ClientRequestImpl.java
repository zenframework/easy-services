package org.zenframework.easyservices.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.zenframework.easyservices.URLHandler;
import org.zenframework.easyservices.util.io.BlockInputStream;
import org.zenframework.easyservices.util.io.BlockOutputStream;
import org.zenframework.easyservices.ClientRequest;
import org.zenframework.easyservices.ServiceLocator;

public class ClientRequestImpl implements ClientRequest {

    private final ClientFactoryImpl clientFactory;
    private final URLConnection urlConnection;
    private final URLHandler clientUrlHandler;
    private boolean successful;

    public ClientRequestImpl(ClientFactoryImpl clientFactory, ServiceLocator serviceLocator, String methodName) throws IOException {
        this.clientFactory = clientFactory;
        clientUrlHandler = clientFactory.getUrlHandler();
        urlConnection = getRequestURL(serviceLocator, methodName, clientFactory.isOutParametersMode()).openConnection();
        if (clientUrlHandler != null)
            clientUrlHandler.prepareConnection(urlConnection);
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
    }

    @Override
    public void writeRequestHeader() throws IOException {
        if (clientUrlHandler != null && clientFactory.getSessionId() != null)
            clientUrlHandler.setSessionId(urlConnection, clientFactory.getSessionId());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream out = urlConnection.getOutputStream();
        if (clientUrlHandler != null && !clientUrlHandler.isCacheInputSafe() && !clientFactory.isCacheInputSafe())
            out = new BlockOutputStream(out);
        return out;
    }

    @Override
    public void readResponseHeader() throws IOException {
        if (clientUrlHandler != null) {
            String sessionId = clientUrlHandler.getSessionId(urlConnection);
            if (sessionId != null)
                clientFactory.setSessionId(sessionId);
        }
        successful = clientUrlHandler == null || clientUrlHandler.isSuccessful(urlConnection);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream in = successful ? urlConnection.getInputStream() : clientUrlHandler.getErrorStream(urlConnection);
        if (clientUrlHandler != null && !clientUrlHandler.isCacheInputSafe() && !clientFactory.isCacheInputSafe())
            in = new BlockInputStream(in) {

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        this.in.close();
                    }
                }

            };
        return in;
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    private URL getRequestURL(ServiceLocator serviceLocator, String methodName, boolean outParametersMode) throws MalformedURLException {
        StringBuilder str = new StringBuilder();
        str.append(serviceLocator.getServiceUrl()).append("?method=").append(methodName);
        if (outParametersMode)
            str.append("&outParameters=true");
        return new URL(str.toString());
    }

}
