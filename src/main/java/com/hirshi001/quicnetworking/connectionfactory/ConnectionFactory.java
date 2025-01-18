package com.hirshi001.quicnetworking.connectionfactory;

import com.hirshi001.quicnetworking.connection.ConnectionImpl;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.ConnectionHandler;
import io.netty.channel.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseCombiner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionFactory<Channels extends Enum<Channels>, Priority extends Enum<Priority>> {

    private final ConnectionHandler<Channels, Priority> connectionHandler;
    private final Class<Channels> channelsClass;
    private final Class<Priority> priorityClass;
    private final Map<ChannelId, ConnectionImpl<Channels, Priority>> connectionMap;

    private final EventLoopGroup service;

    public ConnectionFactory(ConnectionHandler<Channels, Priority> connectionHandler, EventLoopGroup service, Class<Channels> channelsClass, Class<Priority> priorityClass) {
        this.connectionHandler = connectionHandler;
        this.service = service;
        this.channelsClass = channelsClass;
        this.priorityClass = priorityClass;
        this.connectionMap = new ConcurrentHashMap<>();
    }

    public ChannelHandler handler() {
        return new ChannelInboundHandlerAdapter(){
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
                QuicChannel quicChannel = (QuicChannel) ctx.channel();
                quicChannel.pipeline().remove(this);
                ConnectionImpl<Channels, Priority> connection = new ConnectionImpl<>(channelsClass, priorityClass, (QuicChannel) ctx.channel());
                connectionMap.put(ctx.channel().id(), connection);
                connectionHandler.acceptConnection(connection);
            }

            @Override
            public boolean isSharable() {
                return true;
            }
        };
    }

    public ChannelHandler streamHandler() {
        return new ChannelInitializer<QuicStreamChannel>(){
            @Override
            protected void initChannel(QuicStreamChannel ch) throws Exception {
                ConnectionImpl<Channels, Priority> connection = connectionMap.get(ch.parent().id());
                if(connection == null){
                    System.out.println("Connection not found");
                    ch.close();
                    return;
                }
                connection.acceptStream(ch);
            }

            @Override
            public boolean isSharable() {
                return super.isSharable();
            }
        };
    }

    @SuppressWarnings("deprecation")
    public Promise<?> closeAllConnections(){
        PromiseCombiner promiseCombiner = new PromiseCombiner();
        for (ConnectionImpl<Channels, Priority> connection : connectionMap.values()) {
            promiseCombiner.add(connection.close());
        }
        Promise<Void> future = new DefaultPromise<>(service.next());
        promiseCombiner.finish(future);

        return future;

    }
}
