package com.hirshi001.quicnetworking.connectionfactory.connectionhandler;

import com.hirshi001.quicnetworking.connection.Connection;

public interface ConnectionHandler<Channels extends Enum<Channels>, Priority extends Enum<Priority>> {

    void newConnection(Connection<Channels, Priority> connection);


}
