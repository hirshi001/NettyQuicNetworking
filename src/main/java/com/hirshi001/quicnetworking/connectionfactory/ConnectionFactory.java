package com.hirshi001.quicnetworking.connectionfactory;

import com.hirshi001.quicnetworking.connection.ConnectionImpl;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.ConnectionHandler;
import io.netty.channel.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicStreamChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ConnectionFactory<Channels extends Enum<Channels>, Priority extends Enum<Priority>> {

    private final ConnectionHandler<Channels, Priority> connectionHandler;
    private final Class<Channels> channelsClass;
    private final Class<Priority> priorityClass;
    private final Map<ChannelId, ConnectionImpl<Channels, Priority>> connectionMap;

    private Channel channel;

    public ConnectionFactory(ConnectionHandler<Channels, Priority> connectionHandler, Class<Channels> channelsClass, Class<Priority> priorityClass) {
        this.connectionHandler = connectionHandler;
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

    public void setChannel(Channel channel) {
        assert this.channel == null;
        this.channel = channel;
    }

    public Channel channel() {
        return channel;
    }

    public ChannelFuture close() {
        return channel.close();
    }

    public ChannelFuture close(ChannelPromise promise) {
        return channel.close(promise);
    }

    public ChannelFuture closeFuture() {
        return channel.closeFuture();
    }




}
