package com.hirshi001.quicnetworking.helper;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.quic.QuicSslContext;
import io.netty.handler.codec.quic.QuicTokenHandler;

public class ServerConfig extends ConnectionConfig{


    private QuicTokenHandler tokenHandler;
    private QuicSslContext sslContext;


    public ServerConfig() {

    }

    public QuicTokenHandler getTokenHandler() {
        return tokenHandler;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ServerConfig setTokenHandler(QuicTokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
        return this;
    }

    public QuicSslContext getSslContext() {
        return sslContext;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ServerConfig setSslContext(QuicSslContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

}
