package com.hirshi001.quicnetworking.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DuplexChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;


public interface QChannel extends DuplexChannel {

    enum Reliability {
        UNRELIABLE,
        RELIABLE
    }

    enum QChannelEvent {
        INPUT_ACTIVE, // If the other side sends unreliable data, it will be called when the first unreliable packet is received, but before it is is handled
        OUTPUT_ACTIVE,
        INPUT_SHUTDOWN, // Not used for unreliable data
        OUTPUT_SHUTDOWN
    }

    enum OutputState {
        IDLE,
        OPENING,
        ACTIVE,
        CLOSED,
        FAILED
    }

    enum InputState {
        IDLE,
        ACTIVE,
        CLOSED
    }

    Future<? extends io.netty.channel.Channel> openOutputStream(QChannelImpl.Reliability reliability) throws IllegalStateException;

    OutputState getOutputState();

    InputState getInputState();
}
