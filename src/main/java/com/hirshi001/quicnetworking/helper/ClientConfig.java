package com.hirshi001.quicnetworking.helper;

import io.netty.incubator.codec.quic.QuicSslContext;

public class ClientConfig extends ConnectionConfig{


    private QuicSslContext sslContext;

    public ClientConfig() {

    }


    public QuicSslContext getSslContext() {
        return sslContext;
    }

    public ClientConfig setSslContext(QuicSslContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }


}
