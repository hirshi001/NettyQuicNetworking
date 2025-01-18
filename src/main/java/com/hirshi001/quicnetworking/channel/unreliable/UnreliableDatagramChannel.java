package com.hirshi001.quicnetworking.channel.unreliable;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.incubator.codec.quic.QuicChannel;

import java.net.SocketAddress;

public class UnreliableDatagramChannel extends AbstractChannel implements Channel {
    private static final ChannelMetadata METADATA = new ChannelMetadata(false);

    private final ChannelConfig config;
    private final int id;

    public UnreliableDatagramChannel(QuicChannel parent, int channelId) {
        super(parent);
        this.id = channelId;
        this.config = new DefaultChannelConfig(this);
    }

    public int getChannelId() {
        return id;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new UnreliableDatagramUnsafe();
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return true;
    }

    @Override
    protected SocketAddress localAddress0() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        return;
    }

    @Override
    protected void doDisconnect() throws Exception {
        return;
    }

    @Override
    protected void doClose() throws Exception {
        return;
    }

    @Override
    protected void doBeginRead() throws Exception {
        parent().read();
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        while (!in.isEmpty()) {
            ByteBuf buffer = (ByteBuf) in.current();

            // write the channel id to be the first 4 bytes
            ByteBuf channelIdBuffer = parent().alloc().buffer(4);
            channelIdBuffer.writeInt(id);
            channelIdBuffer.writeBytes(buffer);
            parent().writeAndFlush(channelIdBuffer);

            in.remove();
        }
    }


    @Override
    public ChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return parent().isOpen();
    }

    @Override
    public boolean isActive() {
        return parent().isActive();
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    private final class UnreliableDatagramUnsafe extends AbstractUnsafe {
        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            promise.setFailure(new UnsupportedOperationException());
        }

        @Override
        public void close(ChannelPromise promise) {
            promise.setSuccess();
        }
    }
}
