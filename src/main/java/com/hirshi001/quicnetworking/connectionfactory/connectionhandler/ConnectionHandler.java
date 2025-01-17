package com.hirshi001.quicnetworking.connectionfactory.connectionhandler;

import com.hirshi001.quicnetworking.connection.Connection;

@FunctionalInterface
public interface ConnectionHandler<Channels extends Enum<Channels>, Priority extends Enum<Priority>> {

    void acceptConnection(Connection<Channels, Priority> newConnection);

}
