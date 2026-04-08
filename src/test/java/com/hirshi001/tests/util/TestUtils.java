package com.hirshi001.tests.util;

import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.ConnectionHandler;
import com.hirshi001.quicnetworking.helper.ClientConfig;
import com.hirshi001.quicnetworking.helper.QuicNetworkingEnvironment;
import com.hirshi001.quicnetworking.helper.QuicNetworkingHelper;
import com.hirshi001.quicnetworking.helper.ServerConfig;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.codec.quic.*;

import java.net.SocketAddress;

public class TestUtils {


    static final int MAX_DATA = 1000000000;


    public static <Channels extends Enum<Channels>, Priority extends Enum<Priority>> QuicNetworkingEnvironment<Channels, Priority> newServer(Class<Channels> channelsClass, Class<Priority> priorityClass, SocketAddress address, ConnectionHandler<Channels, Priority> connectionHandler) throws Exception {
        SelfSignedCertificate cert = new SelfSignedCertificate("localhost");
        QuicSslContext context = QuicSslContextBuilder.forServer(
                        cert.key(), null, cert.cert())
                .applicationProtocols("test")
                .build();

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setSslContext(context);
        serverConfig.setTokenHandler(InsecureQuicTokenHandler.INSTANCE);
        serverConfig.setEventLoopGroup(new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory()));
        serverConfig.setInitialMaxStreamDataUnidirectional(MAX_DATA);
        serverConfig.setInitialMaxData(MAX_DATA);

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
        clientConfig.setEventLoopGroup(new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory()));

        clientConfig.setInitialMaxStreamDataUnidirectional(MAX_DATA);
        clientConfig.setInitialMaxData(MAX_DATA);

        return QuicNetworkingHelper.createClient(clientConfig, remoteAddress, connectionHandler, channelsClass, priorityClass);
    }

}
