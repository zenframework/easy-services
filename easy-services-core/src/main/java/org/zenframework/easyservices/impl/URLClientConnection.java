package org.zenframework.easyservices.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.zenframework.easyservices.ClientURLHandler;
import org.zenframework.easyservices.ClientConnection;
import org.zenframework.easyservices.ServiceLocator;

public class URLClientConnection implements ClientConnection {

    private final URLClientFactory clientFactory;
    private final URLConnection urlConnection;
    private final ClientURLHandler clientUrlHandler;
    private boolean successful;

    public URLClientConnection(URLClientFactory clientFactory, ServiceLocator serviceLocator, String methodName)
            throws IOException {
        this.clientFactory = clientFactory;
        URL url = getServiceURL(serviceLocator, methodName, clientFactory.isOutParametersMode());
        urlConnection = url.openConnection();
        clientUrlHandler = clientFactory.getClientUrlHandler();
        if (clientUrlHandler != null && clientFactory.getSessionId() != null)
            clientUrlHandler.setSessionId(urlConnection, clientFactory.getSessionId());
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return urlConnection.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (clientUrlHandler != null) {
            String sessionId = clientUrlHandler.getSessionId(urlConnection);
            if (sessionId != null)
                clientFactory.setSessionId(sessionId);
        }
        successful = clientUrlHandler == null || !clientUrlHandler.isError(urlConnection);
        return successful ? urlConnection.getInputStream() : clientUrlHandler.getErrorStream(urlConnection);
    }

    @Override
    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public void close() {}

    private URL getServiceURL(ServiceLocator serviceLocator, String methodName, boolean outParametersMode) throws MalformedURLException {
        StringBuilder str = new StringBuilder();
        str.append(serviceLocator.getServiceUrl()).append("?method=").append(methodName);
        if (outParametersMode)
            str.append("&outParameters=true");
        return new URL(str.toString());
    }

}
