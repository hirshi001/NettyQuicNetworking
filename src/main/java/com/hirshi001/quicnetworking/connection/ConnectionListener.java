package com.hirshi001.quicnetworking.connection;

import net.luminis.quic.QuicConnection;
import net.luminis.quic.server.ApplicationProtocolConnection;
import net.luminis.quic.server.ApplicationProtocolConnectionFactory;
import net.luminis.quic.server.ServerConnector;

public class ConnectionListener {

    private final ServerConnector connector;
    private final String applicationName;

    public ConnectionListener(ServerConnector.Builder builder, String applicationName) throws Exception {
        this.connector = builder.build();
        this.applicationName = applicationName;
        connector.registerApplicationProtocol(applicationName, new ListenerApplicationProtocolConnectionFactory());
    }

    public void start() {
        connector.start();
    }

    public String getApplicationName() {
        return applicationName;
    }

    static class ListenerApplicationProtocolConnectionFactory implements ApplicationProtocolConnectionFactory {

        @Override
        public boolean enableDatagramExtension() {
            return true;
        }

        @Override
        public ApplicationProtocolConnection createConnection(String protocol, QuicConnection quicConnection) {
            return null;
        }
    }

}
