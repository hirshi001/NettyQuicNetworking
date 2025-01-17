package com.hirshi001.quicnetworking.connectionfactory.connectionhandler;

import com.hirshi001.quicnetworking.connection.Connection;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingPollableConnectionHandler<Channels extends Enum<Channels>, Priority extends Enum<Priority>> implements ConnectionHandler<Channels, Priority> {

    private final BlockingQueue<Connection<Channels, Priority>> connectionQueue;

    public BlockingPollableConnectionHandler() {
        this.connectionQueue = new LinkedBlockingQueue<>();
    }

    public BlockingPollableConnectionHandler(int capacity) {
        this.connectionQueue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public void acceptConnection(Connection<Channels, Priority> newConnection) {
        connectionQueue.add(newConnection);
    }

    public Connection<Channels, Priority> pollNewConnection() throws InterruptedException {
        return connectionQueue.take();
    }


    public Connection<Channels, Priority> pollNewConnection(long timeout, TimeUnit unit) throws InterruptedException {
        return connectionQueue.poll(timeout, unit);
    }
}
