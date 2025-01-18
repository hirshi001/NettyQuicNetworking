package com.hirshi001.quicnetworking.message.channelhandlers;

import com.hirshi001.quicnetworking.message.Message;
import com.hirshi001.quicnetworking.message.MessageHolder;
import com.hirshi001.quicnetworking.message.messageregistry.MessageRegistry;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class AsyncMessageHandler extends SimpleChannelInboundHandler<Message> {

    private final MessageRegistry registry;

    public AsyncMessageHandler(MessageRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        MessageHolder<Message> messageHolder = registry.getMessageHolder(msg);
        messageHolder.handler.handle(ctx, msg);
    }

}
