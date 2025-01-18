package com.hirshi001.quicnetworking.message.channelhandlers;

import com.hirshi001.quicnetworking.message.Message;
import com.hirshi001.quicnetworking.message.messageregistry.MessageRegistry;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class PollableMessageHandler extends SimpleChannelInboundHandler<Message> {

    private final BlockingQueue<MessageContext<Message>> messageQueue = new LinkedBlockingQueue<>();


    private final MessageRegistry registry;

    public PollableMessageHandler(MessageRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        messageQueue.add(new MessageContext<>(ctx, msg, registry));
    }

    /**
     * Polls the next message from the queue, returning null if no message is available.
     *
     * @return the next message, or null if no message is available
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> MessageContext<T> poll() {
        return (MessageContext<T>) messageQueue.poll();
    }

    /**
     * Retrieves and removes the next message from the queue, waiting if necessary until a message becomes available.
     *
     * @return the next message
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> MessageContext<T> take() throws InterruptedException {
        return (MessageContext<T>) messageQueue.take();
    }

    /**
     * Retrieves and removes the next message from the queue, waiting if necessary until a message becomes available or the specified timeout elapses.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the next message, or null if the specified waiting time elapses
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    @SuppressWarnings("unchecked")
    public <T extends Message> MessageContext<T> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return (MessageContext<T>) messageQueue.poll(timeout, unit);
    }

    @Override
    protected void ensureNotSharable() {
        super.ensureNotSharable();
    }
}
