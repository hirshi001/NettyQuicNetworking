package com.hirshi001.quicnetworking.message.channelhandlers;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.message.Message;
import com.hirshi001.quicnetworking.message.messageregistry.MessageRegistry;
import io.netty.channel.ChannelHandlerContext;

public class MessageContext<T extends Message> {

    public ChannelHandlerContext ctx;
    public T msg;
    public MessageRegistry registry;

    public MessageContext(ChannelHandlerContext ctx, T msg, MessageRegistry registry) {
        this.ctx = ctx;
        this.msg = msg;
        this.registry = registry;
    }

    public MessageContext() {
    }

    public void set(ChannelHandlerContext ctx, T msg, MessageRegistry registry) {
        this.ctx = ctx;
        this.msg = msg;
        this.registry = registry;
    }

    public void set(MessageContext<T> messageContext) {
        this.ctx = messageContext.ctx;
        this.msg = messageContext.msg;
        this.registry = messageContext.registry;
    }


    /**
     * Handles the packet if the packet handler is not null
     */
    public final void handle() {
        registry.getMessageHolder(msg).handler.handle(ctx, msg);
    }


}
