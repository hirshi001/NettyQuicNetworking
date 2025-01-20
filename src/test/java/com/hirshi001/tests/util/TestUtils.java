package com.hirshi001.tests.util;

import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.ConnectionHandler;
import com.hirshi001.quicnetworking.helper.ClientConfig;
import com.hirshi001.quicnetworking.helper.QuicNetworkingEnvironment;
import com.hirshi001.quicnetworking.helper.QuicNetworkingHelper;
import com.hirshi001.quicnetworking.helper.ServerConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.incubator.codec.quic.*;

import java.net.SocketAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

public class TestUtils {



    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> QuicNetworkingEnvironment<Channels, Priority> newServer(Class<Channels> channelsClass, Class<Priority> priorityClass, SocketAddress address, ConnectionHandler<Channels, Priority> connectionHandler) throws Exception {
        SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
        QuicSslContext context = QuicSslContextBuilder.forServer(
                        selfSignedCertificate.privateKey(), null, selfSignedCertificate.certificate())
                .applicationProtocols("test")
                .build();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setSslContext(context);
        serverConfig.setTokenHandler(InsecureQuicTokenHandler.INSTANCE);
        serverConfig.setEventLoopGroup(new NioEventLoopGroup());

        return QuicNetworkingHelper.createServer(serverConfig, address, connectionHandler, channelsClass, priorityClass);
    }


    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> QuicNetworkingEnvironment<Channels, Priority> newClient(Class<Channels> channelsClass, Class<Priority> priorityClass, SocketAddress remoteAddress, ConnectionHandler<Channels, Priority> connectionHandler) throws Exception {
        QuicSslContext context = QuicSslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocols("test")
                .build();

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setSslContext(context);
        clientConfig.setEventLoopGroup(new NioEventLoopGroup());

        return QuicNetworkingHelper.createClient(clientConfig, remoteAddress, connectionHandler, channelsClass, priorityClass);
    }

}
