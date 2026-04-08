package com.hirshi001.quicnetworking.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public final class WriteOnceOnActiveHandler extends ChannelInboundHandlerAdapter {

    private final int value;
    private boolean done;

    public WriteOnceOnActiveHandler(int value) {
        this.value = value;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (!done) {
            done = true;

            ByteBuf buf = ctx.alloc().buffer(4);
            buf.writeInt(value);

            ctx.write(buf, ctx.voidPromise()); // no flush
        }

        ctx.pipeline().remove(this);
        ctx.fireChannelActive();
    }
}