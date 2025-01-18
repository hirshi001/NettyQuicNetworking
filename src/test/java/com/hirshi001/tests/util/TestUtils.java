package com.hirshi001.tests.util;

import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.ConnectionHandler;
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



    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> NetworkEnvironment<Channels, Priority> newServer(Class<Channels> channelsClass, Class<Priority> priorityClass, SocketAddress address, ConnectionHandler<Channels, Priority> connectionHandler) throws InterruptedException, ExecutionException, CertificateException {
        SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
        QuicSslContext context = QuicSslContextBuilder.forServer(
                        selfSignedCertificate.privateKey(), null, selfSignedCertificate.certificate())
                .applicationProtocols("test")
                .build();


        NioEventLoopGroup group = new NioEventLoopGroup();

        ConnectionFactory<Channels, Priority> connectionFactory = new ConnectionFactory<>(connectionHandler, group, channelsClass, priorityClass);

        ChannelHandler codec = new QuicServerCodecBuilder()
                // Configure some limits for the maximal number of streams (and the data) that we want to handle.
                .initialMaxData(10000000)
                // bidirectional streams
                .initialMaxStreamDataBidirectionalLocal(1000000)
                .initialMaxStreamDataBidirectionalRemote(1000000)
                .initialMaxStreamsBidirectional(100)
                // unidirectional streams
                .initialMaxStreamsUnidirectional(100)
                .initialMaxStreamDataUnidirectional(1000000)
                .datagram(1024 * 16, 1024 * 16)

                // Setup a token handler. In a production system you would want to implement and provide your
                // custom one.
                .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
                .sslContext(context)
                // ChannelHandler that is added into QuicChannel pipeline.
                .handler(connectionFactory.handler())
                .streamHandler(connectionFactory.streamHandler())
                .build();

        Bootstrap bs = new Bootstrap();
        Channel channel = bs.group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec)
                .bind(address).sync().channel();


        return new NetworkEnvironment<>(group, channel, connectionFactory);
    }


    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> NetworkEnvironment<Channels, Priority> newClient(Class<Channels> channelsClass, Class<Priority> priorityClass, SocketAddress remoteAddress, ConnectionHandler<Channels, Priority> connectionHandler) throws InterruptedException, ExecutionException {
        QuicSslContext context = QuicSslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocols("test")
                .build();
        NioEventLoopGroup group = new NioEventLoopGroup(1);

        ChannelHandler codec = new QuicClientCodecBuilder()
                .sslContext(context)
                // Configure some limits for the maximal number of streams (and the data) that we want to handle.
                .initialMaxData(10000000)
                // bidirectional streams
                .initialMaxStreamDataBidirectionalLocal(1000000)
                .initialMaxStreamDataBidirectionalRemote(1000000)
                .initialMaxStreamsBidirectional(100)
                // unidirectional streams
                .initialMaxStreamsUnidirectional(100)
                .initialMaxStreamDataUnidirectional(1000000)

                .datagram(1024 * 16, 1024 * 16)
                .build();

        Bootstrap bs = new Bootstrap();
        Channel channel = bs.group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec)
                .bind(0).sync().channel();

        ConnectionFactory<Channels, Priority> connectionFactory = new ConnectionFactory<>(connectionHandler, group, channelsClass, priorityClass);

        QuicChannel quicChannel = QuicChannel.newBootstrap(channel)
                .handler(connectionFactory.handler())
                .streamHandler(connectionFactory.streamHandler())
                .remoteAddress(remoteAddress)
                .connect()
                .get();


        return new NetworkEnvironment<>(group, channel, connectionFactory);

    }

}
