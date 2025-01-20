package com.hirshi001.quicnetworking.helper;

import io.netty.channel.EventLoopGroup;

public class ConnectionConfig {


    private long initialMaxData = 10000000;
    private long initialMaxStreamDataUnidirectional = 10000000;
    private int datagramReceive = 2048;
    private int datagramSend = 2048;

    private EventLoopGroup eventLoopGroup;

    public ConnectionConfig() {

    }

    public long getInitialMaxData() {
        return initialMaxData;
    }

    public ConnectionConfig setInitialMaxData(long initialMaxData) {
        this.initialMaxData = initialMaxData;
        return this;
    }

    public long getInitialMaxStreamDataUnidirectional() {
        return initialMaxStreamDataUnidirectional;
    }

    public ConnectionConfig setInitialMaxStreamDataUnidirectional(long initialMaxStreamDataUnidirectional) {
        this.initialMaxStreamDataUnidirectional = initialMaxStreamDataUnidirectional;
        return this;
    }

    public int getDatagramReceive() {
        return datagramReceive;
    }

    public ConnectionConfig setDatagramReceive(int datagramReceive) {
        this.datagramReceive = datagramReceive;
        return this;
    }

    public int getDatagramSend() {
        return datagramSend;
    }

    public ConnectionConfig setDatagramSend(int datagramSend) {
        this.datagramSend = datagramSend;
        return this;
    }

    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    public ConnectionConfig setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }
}
