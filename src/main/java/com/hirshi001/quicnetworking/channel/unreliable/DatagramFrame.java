package com.hirshi001.quicnetworking.channel.unreliable;

import io.netty.buffer.ByteBuf;

public record DatagramFrame(ByteBuf data, int id) {

    public DatagramFrame {
        if (data == null) {
            throw new NullPointerException("data");
        }
    }

}
