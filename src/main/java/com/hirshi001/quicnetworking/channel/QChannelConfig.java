package com.hirshi001.quicnetworking.channel;

import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.RecvByteBufAllocator;

public class QChannelConfig extends DefaultChannelConfig {
    public QChannelConfig(Channel channel) {
        super(channel);
    }

    protected QChannelConfig(Channel channel, RecvByteBufAllocator allocator) {
        super(channel, allocator);
    }
}
