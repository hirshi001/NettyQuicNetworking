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

public class QuicNetworkingEnvironment<Channels extends Enum<Channels>, Priority extends Enum<Priority>> implements AutoCloseable{

    private final EventLoopGroup eventLoopGroup;
    private final EventLoop eventLoop;
    private final Channel channel;
    private final ConnectionFactory<Channels, Priority> connectionFactory;


    QuicNetworkingEnvironment(EventLoopGroup eventLoopGroup, Channel channel, ConnectionHandler<Channels, Priority> connectionHandler, Class<Channels> channelsClass, Class<Priority> priorityClass) {
        this.eventLoopGroup = eventLoopGroup;
        this.eventLoop = eventLoopGroup.next();
        this.channel = channel;
        this.connectionFactory = new ConnectionFactory<>(connectionHandler, eventLoopGroup, channelsClass, priorityClass);
    }

    @Override
    public void close() {
        closeAsync().awaitUninterruptibly();
    }

    public Future<?> closeAsync() {
        Promise<QuicNetworkingEnvironment<Channels, Priority>> promise = eventLoop.newPromise();

        eventLoop.execute(() -> {
            close0(promise);
        });

        return promise;
    }

    private void close0(Promise<QuicNetworkingEnvironment<Channels, Priority>> promise) {
        Promise<Void> aggregate = eventLoop.newPromise();
        PromiseCombiner combiner = new PromiseCombiner(eventLoop);

        combiner.add((Future<?>) connectionFactory.closeAllConnections());
        combiner.add(channel.close());

        combiner.finish(aggregate);

        aggregate.addListener(f -> {
            if (f.isSuccess()) {
                promise.setSuccess(this);
            } else {
                promise.setFailure(f.cause());
            }
        });
    }

    public Future<?> shutdownGracefully() throws InterruptedException {
        return eventLoopGroup.shutdownGracefully();
    }

    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) throws InterruptedException {
        return eventLoopGroup.shutdownGracefully(quietPeriod, timeout, unit);
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

}
