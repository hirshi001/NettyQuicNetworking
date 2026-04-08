package com.hirshi001.quicnetworking.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.util.concurrent.Future;

public interface StreamGenerator {


    Future<? extends Channel> generateUnidirectionalStream(ChannelHandler handler);

}
