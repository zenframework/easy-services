package org.zenframework.easyservices.websocket;

import java.io.IOException;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

public class ServiceEndpoint extends Endpoint {

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        final RemoteEndpoint.Basic remote = session.getBasicRemote();
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            
            @Override
            public void onMessage(String text) {
                try {
                    remote.sendText(text.toUpperCase());
                } catch (IOException ioe) {
                    // handle send failure here
                }
            }

        });
    }

}
