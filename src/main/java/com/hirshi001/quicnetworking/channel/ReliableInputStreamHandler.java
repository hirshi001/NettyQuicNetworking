package com.hirshi001.quicnetworking.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ReliableInputStreamHandler extends ChannelInboundHandlerAdapter {

    private final QChannelImpl channel;

    public ReliableInputStreamHandler(QChannelImpl channel) {
        super();
        this.channel = channel;
    }

    private void fireInChannel(Runnable runnable) {
        var eventLoop = channel.eventLoop();
        if (eventLoop.inEventLoop())
            runnable.run();
        else
            eventLoop.execute(runnable);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        fireInChannel(() -> channel.fireInputStreamInactive());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            fireInChannel(() -> channel.pipeline().fireChannelRead(msg));
        } catch (Throwable t) {
            ReferenceCountUtil.release(msg);
            throw t;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        fireInChannel(() -> channel.pipeline().fireChannelReadComplete());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        fireInChannel(() -> channel.pipeline().fireUserEventTriggered(evt));
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        fireInChannel(() -> channel.pipeline().fireChannelWritabilityChanged());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        fireInChannel(() -> channel.pipeline().fireExceptionCaught(cause));
    }
}
