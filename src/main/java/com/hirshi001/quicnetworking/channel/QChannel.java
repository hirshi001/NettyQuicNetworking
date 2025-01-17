package com.hirshi001.quicnetworking.channel;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;


public interface QChannel {

    enum Reliability {
        UNRELIABLE,
        RELIABLE
    }

    void setChannelHandler(ChannelHandler handler) throws IllegalStateException;

    Future<? extends io.netty.channel.Channel> openOutputStream(QChannelImpl.Reliability reliability) throws IllegalStateException;

    io.netty.channel.Channel getOutChannel();

    io.netty.channel.Channel getInChannel() ;

    default ChannelFuture write(Object msg) {
        return getOutChannel().write(msg);
    }

    default ChannelFuture write(Object msg, ChannelPromise promise) {
       return getOutChannel().write(msg, promise);
    }

    default ChannelFuture writeAndFlush(Object msg) {
        return getOutChannel().writeAndFlush(msg);
    }

    default ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return getOutChannel().writeAndFlush(msg, promise);
    }

    default QChannel flush() {
        getOutChannel().flush();
        return this;
    }




}
