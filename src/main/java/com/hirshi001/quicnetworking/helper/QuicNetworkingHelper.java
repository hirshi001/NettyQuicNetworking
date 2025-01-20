package com.hirshi001.quicnetworking.helper;

import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.ConnectionHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicClientCodecBuilder;
import io.netty.incubator.codec.quic.QuicServerCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;

public class QuicNetworkingHelper {

    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> QuicNetworkingEnvironment<Channels, Priority> createServer(ServerConfig serverConfig, SocketAddress address, ConnectionHandler<Channels, Priority> connectionHandler, Class<Channels> channelsClass, Class<Priority> priorityClass) throws Exception {

        EventLoopGroup group = serverConfig.getEventLoopGroup();
        if(group == null){
            group = new NioEventLoopGroup();
        }

        ConnectionFactory<Channels, Priority> connectionFactory = new ConnectionFactory<>(connectionHandler, group, channelsClass, priorityClass);

        ChannelHandler codec = new QuicServerCodecBuilder()
                // Configure some limits for the maximal number of streams (and the data) that we want to handle.
                .initialMaxData(serverConfig.getInitialMaxData())
                // unidirectional streams
                .initialMaxStreamsUnidirectional(channelsClass.getEnumConstants().length)
                .initialMaxStreamDataUnidirectional(serverConfig.getInitialMaxStreamDataUnidirectional())
                .datagram(serverConfig.getDatagramReceive(), serverConfig.getDatagramSend())

                // Setup a token handler. In a production system you would want to implement and provide your
                // custom one.
                .tokenHandler(serverConfig.getTokenHandler())
                .sslContext(serverConfig.getSslContext())

                // ChannelHandler that is added into QuicChannel pipeline.
                .handler(connectionFactory.handler())
                .streamHandler(connectionFactory.streamHandler())
                .build();

        Bootstrap bs = new Bootstrap();
        Channel channel = bs.group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec)
                .bind(address).sync().channel();

        return new QuicNetworkingEnvironment<>(group, channel, connectionHandler, channelsClass, priorityClass);
    }

    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> QuicNetworkingEnvironment<Channels, Priority> createServer(QuicServerCodecBuilder codecBuilder, @Nullable EventLoopGroup group, SocketAddress address, ConnectionHandler<Channels, Priority> connectionHandler, Class<Channels> channelsClass, Class<Priority> priorityClass) throws Exception {

        if(group == null){
            group = new NioEventLoopGroup();
        }

        ConnectionFactory<Channels, Priority> connectionFactory = new ConnectionFactory<>(connectionHandler, group, channelsClass, priorityClass);

        ChannelHandler codec =
                codecBuilder
                .handler(connectionFactory.handler())
                .streamHandler(connectionFactory.streamHandler())
                .build();

        Bootstrap bs = new Bootstrap();
        Channel channel = bs.group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec)
                .bind(address).sync().channel();

        return new QuicNetworkingEnvironment<>(group, channel, connectionHandler, channelsClass, priorityClass);
    }

    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> QuicNetworkingEnvironment<Channels, Priority> createServer(QuicServerCodecBuilder codecBuilder, SocketAddress address, ConnectionHandler<Channels, Priority> connectionHandler, Class<Channels> channelsClass, Class<Priority> priorityClass) throws Exception {
        return createServer(codecBuilder, null, address, connectionHandler, channelsClass, priorityClass);
    }

    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> QuicNetworkingEnvironment<Channels, Priority> createClient(ClientConfig clientConfig, SocketAddress remoteAddress, ConnectionHandler<Channels, Priority> connectionHandler, Class<Channels> channelsClass, Class<Priority> priorityClass) throws Exception {

        EventLoopGroup group = clientConfig.getEventLoopGroup();
        if(group == null){
            group = new NioEventLoopGroup();
        }

        ChannelHandler codec = new QuicClientCodecBuilder()
                .sslContext(clientConfig.getSslContext())
                // Configure some limits for the maximal number of streams (and the data) that we want to handle.
                .initialMaxData(clientConfig.getInitialMaxData())
                // unidirectional streams
                .initialMaxStreamsUnidirectional(channelsClass.getEnumConstants().length)
                .initialMaxStreamDataUnidirectional(clientConfig.getInitialMaxStreamDataUnidirectional())

                .datagram(clientConfig.getDatagramReceive(), clientConfig.getDatagramSend())
                .build();

        Bootstrap bs = new Bootstrap();
        Channel channel = bs.group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec)
                .connect(remoteAddress).sync().channel();

        ConnectionFactory<Channels, Priority> connectionFactory = new ConnectionFactory<>(connectionHandler, group, channelsClass, priorityClass);

        QuicChannel quicChannel = QuicChannel.newBootstrap(channel)
                .handler(connectionFactory.handler())
                .streamHandler(connectionFactory.streamHandler())
                .remoteAddress(remoteAddress)
                .connect()
                .get();


        return new QuicNetworkingEnvironment<>(group, channel, connectionHandler, channelsClass, priorityClass);
    }

    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> QuicNetworkingEnvironment<Channels, Priority> createClient(QuicClientCodecBuilder codecBuilder, @Nullable EventLoopGroup group, SocketAddress remoteAddress, ConnectionHandler<Channels, Priority> connectionHandler, Class<Channels> channelsClass, Class<Priority> priorityClass) throws Exception {
        return createClient(codecBuilder.build(), group, remoteAddress, connectionHandler, channelsClass, priorityClass);
    }

    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> QuicNetworkingEnvironment<Channels, Priority> createClient(ChannelHandler codec, @Nullable EventLoopGroup group, SocketAddress remoteAddress, ConnectionHandler<Channels, Priority> connectionHandler, Class<Channels> channelsClass, Class<Priority> priorityClass) throws Exception {

        if (group == null) {
            group = new NioEventLoopGroup();
        }

        Bootstrap bs = new Bootstrap();
        Channel channel = bs.group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec)
                .connect(remoteAddress).sync().channel();

        ConnectionFactory<Channels, Priority> connectionFactory = new ConnectionFactory<>(connectionHandler, group, channelsClass, priorityClass);

        QuicChannel quicChannel = QuicChannel.newBootstrap(channel)
                .handler(connectionFactory.handler())
                .streamHandler(connectionFactory.streamHandler())
                .remoteAddress(remoteAddress)
                .connect()
                .get();

        return new QuicNetworkingEnvironment<>(group, channel, connectionHandler, channelsClass, priorityClass);
    }




}
