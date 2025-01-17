package com.hirshi001.quicnetworking.connection;

import com.hirshi001.quicnetworking.channel.QChannel;
import io.netty.channel.ChannelFuture;
import io.netty.incubator.codec.quic.QuicChannel;

public interface Connection<Channels extends Enum<Channels>, Priority extends Enum<Priority>> {


    QuicChannel getConnection();

    /**
     * Returns the channel associated with the given channel id.
     *
     * @param channelId The channel id
     * @return The channel associated with the given channel id.
     */
    QChannel getChannel(Channels channelId);

    void setChannelPriority(Channels channelId, Priority priority);

    Priority getChannelPriority(Channels channelId);

    ChannelFuture close();

    ChannelFuture close(long applicationProtocolErrorCode, String errorReason);

}
