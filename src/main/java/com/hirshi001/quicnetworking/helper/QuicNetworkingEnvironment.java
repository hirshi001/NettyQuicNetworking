package com.hirshi001.quicnetworking.helper;

import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.ConnectionHandler;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseCombiner;

import java.util.concurrent.TimeUnit;

public class QuicNetworkingEnvironment<Channels extends Enum<Channels>, Priority extends Enum<Priority>> {

    private final EventLoopGroup eventLoopGroup;
    private final Channel channel;
    private final ConnectionFactory<Channels, Priority> connectionFactory;

    QuicNetworkingEnvironment(EventLoopGroup eventLoopGroup, Channel channel, ConnectionHandler<Channels, Priority> connectionHandler, Class<Channels> channelsClass, Class<Priority> priorityClass) {
        this.eventLoopGroup = eventLoopGroup;
        this.channel = channel;
        this.connectionFactory = new ConnectionFactory<>(connectionHandler, eventLoopGroup, channelsClass, priorityClass);
    }

    public Promise<QuicNetworkingEnvironment<Channels, Priority>> closeServer() throws InterruptedException
    {
        EventLoop eventLoop = eventLoopGroup.next();

        PromiseCombiner promiseCombiner = new PromiseCombiner(eventLoop);
        Promise<Void> combinerFinish = eventLoop.newPromise();
        Promise<QuicNetworkingEnvironment<Channels, Priority>> promise = eventLoop.newPromise();

        combinerFinish.addListener(future -> promise.setSuccess(this));

        promiseCombiner.add((Future<?>) connectionFactory.closeAllConnections());
        promiseCombiner.add(channel.close());

        promiseCombiner.finish(combinerFinish);

        return promise;
    }

    public Future<?> shutdownGracefully() throws InterruptedException {
        return eventLoopGroup.shutdownGracefully();
    }

    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) throws InterruptedException {
        return eventLoopGroup.shutdownGracefully(quietPeriod, timeout, unit);
    }

}
