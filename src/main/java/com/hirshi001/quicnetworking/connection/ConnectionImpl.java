package com.hirshi001.quicnetworking.connection;

import com.hirshi001.quicnetworking.channel.QChannelImpl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicStreamChannel;


public class ConnectionImpl<Channels extends Enum<Channels>, Priority extends Enum<Priority>> implements Connection<Channels, Priority> {

    QuicChannel connection;

    QChannelImpl[] channels;

    private final Class<Channels> channelsEnum;
    private final Class<Priority> priorityEnum;

    public ConnectionImpl(Class<Channels> channelsEnum, Class<Priority> priorityEnum, QuicChannel connection) {
        assert channelsEnum.isEnum();
        assert priorityEnum.isEnum();

        this.channelsEnum = channelsEnum;
        this.priorityEnum = priorityEnum;
        this.connection = connection;

        this.channels = new QChannelImpl[channelsEnum.getEnumConstants().length];
        for (Channels channel : channelsEnum.getEnumConstants()) {
            channels[channel.ordinal()] = new QChannelImpl(this, channel);
        }

        connection.pipeline().addLast(quicConnectionHandler());
    }

    public Class<Channels> getChannelsEnum() {
        return channelsEnum;
    }

    public Class<Priority> getPriorityEnum() {
        return priorityEnum;
    }

    public void acceptStream(QuicStreamChannel channel) {
        assert channel != null;
        assert channel.parent() == connection;
        channel.pipeline().addLast(quicStreamHandler());
    }


    private ChannelHandler quicConnectionHandler() {
        return new ChannelInboundHandlerAdapter() {

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof ByteBuf in) {
                    if (in.readableBytes() < 4)
                        return;
                    int channelId = in.readInt();
                    if (channelId < 0 || channelId >= channels.length) {
                        // bad stream, likely a bad connection
                        // TODO: either report this or close the connection
                        ctx.close();
                        return;
                    }
                    channels[channelId].acceptDatagram(in);
                } else {
                    super.channelRead(ctx, msg);
                }
            }

            @Override
            public boolean isSharable() {
                return true;
            }

            /*
            @Override
            protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
                super.channelRead(ctx, msg);
                ByteBuf in = msg;
                System.out.println("Decoding datagram");
                if (in.readableBytes() < 4)
                    return;
                int channelId = in.readInt();
                if (channelId < 0 || channelId >= channels.length) {
                    System.out.println("Invalid channel id: " + channelId);
                    return;
                }
                channels[channelId].acceptDatagram(in);
            }

             */
        };
    }

    private ChannelHandler quicStreamHandler() {

        return new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                ByteBuf in = (ByteBuf) msg;

                // System.out.println(in.toString(CharsetUtil.UTF_8));
                if (in.readableBytes() < 4)
                    return;

                int channelId = in.readInt();
                if (channelId < 0 || channelId >= channels.length) {
                    // bad stream, likely a bad connection
                    ctx.close();
                    return;
                }

                ctx.channel().pipeline().remove(this);
                channels[channelId].connectInputStream(ctx.channel());
            }

            @Override
            public boolean isSharable() {
                return true;
            }
        };
    }

    @Override
    public QuicChannel getConnection() {
        return connection;
    }

    @Override
    public QChannelImpl getChannel(Channels channelId) {
        return channels[channelId.ordinal()];
    }

    @Override
    public void setChannelPriority(Channels channelId, Priority priority) {
        getChannel(channelId).setChannelPriority(priority.ordinal());
    }

    @Override
    public Priority getChannelPriority(Channels channelId) {
        return priorityEnum.getEnumConstants()[getChannel(channelId).getChannelPriority()];
    }


    @Override
    public ChannelFuture close() {
        return connection.close();
    }

    @Override
    public ChannelFuture close(long applicationProtocolErrorCode, String errorReason) {
        return connection.close(true, (int) applicationProtocolErrorCode, Unpooled.wrappedBuffer(errorReason.getBytes()));
    }


}
