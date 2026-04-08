package com.hirshi001.tests.connectiontests;

import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.quicnetworking.helper.QuicNetworkingEnvironment;
import com.hirshi001.tests.channeltests.SingleClientSingleChannelUnreliableTests;
import com.hirshi001.tests.util.QuicNetworkingEnvironmentGroup;
import com.hirshi001.tests.util.TestUtils;
import io.netty.util.NetUtil;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"rawtypes"})
public class ConnectionTests {

    enum Priority {

    }

    enum Channels {
    }


    @Test
    public void singleClientConnectionTest() throws Exception {
        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();

        try (var _ = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);
             var _ = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);
        ) {
            Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            assertNotNull(serverConnection);

            Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            assertNotNull(clientConnection);

            serverConnection.close().sync();
            clientConnection.close().sync();
        }
    }

    @Test
    public void twoClientConnectionTest() throws Exception {

        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler1 = new BlockingPollableConnectionHandler<>();
        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler2 = new BlockingPollableConnectionHandler<>();

        try (var _ = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);
             var _ = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler1);
             var _ = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler2);
        ) {

            Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            assertNotNull(serverConnection);

            Connection<Channels, Priority> clientConnection1 = clientConnectionHandler1.pollNewConnection(100, TimeUnit.MILLISECONDS);
            assertNotNull(clientConnection1);
            Connection<Channels, Priority> clientConnection2 = clientConnectionHandler2.pollNewConnection(100, TimeUnit.MILLISECONDS);
            assertNotNull(clientConnection2);

            serverConnection.close().sync();
            clientConnection1.close().sync();
            clientConnection2.close().sync();
        }
    }


    @Test
    @SuppressWarnings("unchecked")
    public void manyClientConnectionTest() throws Exception {

        int numClients = 100;

        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();

        var clientConnectionHandlers = new ArrayList<BlockingPollableConnectionHandler<Channels, Priority>>();
        var clientNetworkEnvironments = new ArrayList<QuicNetworkingEnvironment<Channels, Priority>>();
        for (int i = 0; i < numClients; i++) {
            clientConnectionHandlers.add(new BlockingPollableConnectionHandler<>());
            clientNetworkEnvironments.add(TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandlers.get(i)));
        }

        try (var _ = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);
             var _ = new QuicNetworkingEnvironmentGroup(clientNetworkEnvironments)) {


            Connection[] server = new Connection[numClients];
            Connection[] clients = new Connection[numClients];

            for (int index = 0; index < numClients; index++) {

                Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
                Connection<Channels, Priority> clientConnection = clientConnectionHandlers.get(index).pollNewConnection(100, TimeUnit.MILLISECONDS);

                assertNotNull(serverConnection);
                assertNotNull(clientConnection);

                server[index] = serverConnection;
                clients[index] = clientConnection;
            }

            var futures = Stream.concat(Arrays.stream(server), Arrays.stream(clients))
                    .map(Connection::close)
                    .toList();

            for (var future : futures)
                future.sync();

        }
    }
}
