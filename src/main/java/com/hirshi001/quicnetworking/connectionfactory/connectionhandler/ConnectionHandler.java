package com.hirshi001.quicnetworking.connectionfactory.connectionhandler;

public interface ConnectionHandler<Channels extends Enum<Channels>, Priority extends Enum<Priority>> {

    void newEvent(ConnectionEvent<Channels, Priority> event);


}
