package com.hirshi001.quicnetworking.connectionfactory.connectionhandler;

import com.hirshi001.quicnetworking.connection.Connection;

public class ConnectionEvent<Channels extends Enum<Channels>, Priority extends Enum<Priority>> {
    public Connection<Channels, Priority> connection;
    public ConnectionEventType type;
    public Object data;


    public ConnectionEvent(Connection<Channels, Priority> connection, ConnectionEventType type) {
        this(connection, type, null);
    }

    public ConnectionEvent(Connection<Channels, Priority> connection, ConnectionEventType type, Object data) {
        this.connection = connection;
        this.type = type;
        this.data = data;
    }
}
