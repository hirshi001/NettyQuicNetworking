package com.hirshi001.quicnetworking.channel;

import com.hirshi001.quicnetworking.channel.unreliable.UnreliableDatagramChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.incubator.codec.quic.QuicStreamPriority;
import io.netty.incubator.codec.quic.QuicStreamType;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

public class QChannelImpl implements QChannel {

    private final Connection<?, ?> connection;
    private final Enum<?> channelId;
    private int priority; // TODO: Learn default priority value

    private volatile io.netty.channel.Channel outChannel, inChannel;

    private ChannelHandler channelHandler;

    private final Object lock = new Object();


    private ByteBuf copyReference;


    public QChannelImpl(Connection<?, ?> connection, Enum<?> channelId) {
        this.connection = connection;
        this.channelId = channelId;
    }


    public Enum<?> getChannelId() {
        return channelId;
    }

    public void connectInputStream(io.netty.channel.Channel inputStream) {
        synchronized (lock) {
            this.inChannel = inputStream;
            if (channelHandler != null) {
                inChannel.pipeline().addLast(channelHandler);
            } else {
                inChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        synchronized (lock) {
                            if (msg instanceof ByteBuf in) {
                                copyReference = in;
                            }
                            if (channelHandler != null) {
                                ctx.pipeline().remove(this);
                                super.channelRead(ctx, msg);
                            }
                        }

                    }
                });
            }

        }
    }

    public void acceptDatagram(ByteBuf frame) {
        synchronized (lock) {
            if (inChannel == null) {
                inChannel = new UnreliableDatagramChannel(connection.getConnection(), channelId.ordinal());
                connection.getConnection().eventLoop().register(inChannel);

                if (channelHandler != null) {
                    inChannel.pipeline().addLast(channelHandler);
                }
            }
            inChannel.pipeline().fireChannelRead(frame);
        }
    }

    public io.netty.channel.Channel getOutChannel() {
        return outChannel;
    }

    public io.netty.channel.Channel getInChannel() {
        return inChannel;
    }


    @Override
    public Future<? extends io.netty.channel.Channel> openOutputStream(QChannelImpl.Reliability reliability) throws IllegalStateException {
        if (outChannel != null) {
            throw new IllegalStateException("Output stream already open");
        }
        if (reliability == null) {
            throw new IllegalArgumentException("Reliability cannot be null");
        }
        if (reliability == Reliability.RELIABLE) {
            return connection.getConnection().createStream(QuicStreamType.UNIDIRECTIONAL, new ChannelInboundHandlerAdapter() {
                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                    // write the channel id to the stream

                    ByteBuf buffer = Unpooled.directBuffer(4);
                    buffer.writeInt(channelId.ordinal());
                    ctx.channel().writeAndFlush(buffer);

                    synchronized (lock) {
                        outChannel = ctx.channel();
                        if (channelHandler != null) {
                            outChannel.pipeline().addLast(channelHandler);
                        }
                    }
                }
            });
        } else {
            synchronized (lock) {

                EventLoop eventLoop = connection.getConnection().eventLoop();
                Promise<UnreliableDatagramChannel> promise = eventLoop.newPromise();
                outChannel = new UnreliableDatagramChannel(connection.getConnection(), channelId.ordinal());
                connection.getConnection().eventLoop().register(outChannel)
                        .addListener((ChannelFuture f) -> {
                            if (f.isSuccess()) {
                                promise.setSuccess((UnreliableDatagramChannel) outChannel);
                            } else {
                                promise.setFailure(f.cause());
                            }
                        });
                outChannel.pipeline().addLast(channelHandler);

                return promise;

            }
        }
    }

    @Override
    public void setChannelHandler(ChannelHandler channelHandler) {
        synchronized (lock) {
            if (this.channelHandler != null) {
                throw new IllegalStateException("Channel handler already set");
            }
            if (channelHandler == null) {
                throw new IllegalArgumentException("Channel handler cannot be null");
            }

            this.channelHandler = channelHandler;
            if (inChannel != null) {
                inChannel.pipeline().addLast(channelHandler);
                if (copyReference != null) {
                    inChannel.pipeline().fireChannelRead(copyReference);
                }
            }
            if (outChannel != null) {
                outChannel.pipeline().addLast(channelHandler);
            }
        }
    }

    public void setChannelPriority(int priority) {
        synchronized (lock) {
            this.priority = priority;
            if (outChannel instanceof QuicStreamChannel quicStream) {
                quicStream.updatePriority(new QuicStreamPriority(priority, true));
            }
        }
    }

    public int getChannelPriority() {
        return priority;
    }
}
