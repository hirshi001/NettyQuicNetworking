package com.hirshi001.quicnetworking.connectionfactory.connectionhandler;

import com.hirshi001.quicnetworking.connection.Connection;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PollableConnectionHandler<Channels extends Enum<Channels>, Priority extends Enum<Priority>> implements ConnectionHandler<Channels, Priority> {

    private final Queue<Connection<Channels, Priority>> connectionQueue;

    public PollableConnectionHandler() {
        this.connectionQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void acceptConnection(Connection<Channels, Priority> newConnection) {
        connectionQueue.add(newConnection);
    }

    public Connection<Channels, Priority> pollNewConnection() {
        return connectionQueue.poll();
    }
}
