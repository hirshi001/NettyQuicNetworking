package com.hirshi001.quicnetworking.helper;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.incubator.codec.quic.QuicSslContext;
import io.netty.incubator.codec.quic.QuicTokenHandler;

public class ServerConfig extends ConnectionConfig{


    private QuicTokenHandler tokenHandler;
    private QuicSslContext sslContext;


    public ServerConfig() {

    }

    public QuicTokenHandler getTokenHandler() {
        return tokenHandler;
    }

    public ServerConfig setTokenHandler(QuicTokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
        return this;
    }

    public QuicSslContext getSslContext() {
        return sslContext;
    }

    public ServerConfig setSslContext(QuicSslContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

}
