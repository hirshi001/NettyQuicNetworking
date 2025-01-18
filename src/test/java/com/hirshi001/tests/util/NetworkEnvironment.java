package com.hirshi001.tests.util;

import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

public class NetworkEnvironment<Channels extends Enum<Channels>, Priority extends Enum<Priority>> {

    public final EventLoopGroup eventLoopGroup;
    public final Channel channel;
    public final ConnectionFactory<Channels, Priority> connectionFactory;

    public NetworkEnvironment(EventLoopGroup eventLoopGroup, Channel channel, ConnectionFactory<Channels, Priority> connectionFactory) {
        this.eventLoopGroup = eventLoopGroup;
        this.channel = channel;
        this.connectionFactory = connectionFactory;
    }

    public void close() throws InterruptedException {
        connectionFactory.closeAllConnections().await();
        channel.close().await();
        eventLoopGroup.shutdownGracefully().await();
    }


}
