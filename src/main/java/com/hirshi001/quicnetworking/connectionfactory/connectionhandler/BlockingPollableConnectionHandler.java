package com.hirshi001.quicnetworking.connectionfactory.connectionhandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingPollableConnectionHandler<Channels extends Enum<Channels>, Priority extends Enum<Priority>> implements ConnectionHandler<Channels, Priority> {

    private final BlockingQueue<ConnectionEvent<Channels, Priority>> connectionQueue;

    public BlockingPollableConnectionHandler() {
        this.connectionQueue = new LinkedBlockingQueue<>();
    }

    public BlockingPollableConnectionHandler(int capacity) {
        this.connectionQueue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public void newEvent(ConnectionEvent<Channels, Priority> event) {
        connectionQueue.add(event);
    }

    public ConnectionEvent<Channels, Priority> pollNewEvent() throws InterruptedException {
        return connectionQueue.take();
    }


    public ConnectionEvent<Channels, Priority> pollNewEvent(long timeout, TimeUnit unit) throws InterruptedException {
        return connectionQueue.poll(timeout, unit);
    }
}
