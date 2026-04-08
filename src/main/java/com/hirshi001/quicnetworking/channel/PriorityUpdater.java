package com.hirshi001.quicnetworking.channel;

import io.netty.channel.Channel;

public interface PriorityUpdater {

    void updatePriority(int priority, Channel channel);

}
