package com.hirshi001.tests.connectiontests;

import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.quicnetworking.helper.QuicNetworkingEnvironment;
import com.hirshi001.tests.util.TestUtils;
import io.netty.channel.EventLoopGroup;
import io.netty.util.NetUtil;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
        QuicNetworkingEnvironment<Channels, Priority> serverNetworkEnvironment = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);

        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();
        QuicNetworkingEnvironment<Channels, Priority> clientNetworkEnvironment = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);

        AtomicReference<Connection> server = new AtomicReference<>();
        AtomicReference<Connection> client = new AtomicReference<>();

        assertDoesNotThrow(() -> server.set(serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS)));
        assertDoesNotThrow(() -> client.set(clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS)));

        assertNotNull(server.get());
        assertNotNull(client.get());

        server.get().close().sync();
        client.get().close().sync();

        clientNetworkEnvironment.close().await();
        serverNetworkEnvironment.close().await();

        clientNetworkEnvironment.shutdownGracefully().await();
        serverNetworkEnvironment.shutdownGracefully().await();
    }

    @Test
    public void twoClientConnectionTest() throws Exception {
        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        QuicNetworkingEnvironment<Channels, Priority> serverNetworkEnvironment = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);

        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler1 = new BlockingPollableConnectionHandler<>();
        QuicNetworkingEnvironment<Channels, Priority> clientNetworkEnvironment1 = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler1);

        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler2 = new BlockingPollableConnectionHandler<>();
        QuicNetworkingEnvironment<Channels, Priority> clientNetworkEnvironment2 = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler2);

        AtomicReference<Connection> server1 = new AtomicReference<>();
        AtomicReference<Connection> server2 = new AtomicReference<>();

        AtomicReference<Connection> client1 = new AtomicReference<>();
        AtomicReference<Connection> client2 = new AtomicReference<>();
        assertDoesNotThrow(() -> server1.set(serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS)));
        assertDoesNotThrow(() -> server2.set(serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS)));
        assertDoesNotThrow(() -> client1.set(clientConnectionHandler1.pollNewConnection(100, TimeUnit.MILLISECONDS)));
        assertDoesNotThrow(() -> client2.set(clientConnectionHandler2.pollNewConnection(100, TimeUnit.MILLISECONDS)));

        assertNotNull(server1.get());
        assertNotNull(server2.get());
        assertNotNull(client1.get());
        assertNotNull(client2.get());

        assertNotEquals(server1.get(), server2.get());
        assertNotEquals(client1.get(), client2.get());

        server1.get().close().sync();
        server2.get().close().sync();
        client1.get().close().sync();
        client2.get().close().sync();

        clientNetworkEnvironment1.close().await();
        clientNetworkEnvironment2.close().await();
        serverNetworkEnvironment.close().await();

        clientNetworkEnvironment1.shutdownGracefully().await();
        clientNetworkEnvironment2.shutdownGracefully().await();
        serverNetworkEnvironment.shutdownGracefully().await();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void manyClientConnectionTest() throws Exception {
        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        QuicNetworkingEnvironment<Channels, Priority> serverNetworkEnvironment = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);

        int numClients = 100;
        BlockingPollableConnectionHandler<Channels, Priority>[] clientConnectionHandlers = new BlockingPollableConnectionHandler[numClients];
        QuicNetworkingEnvironment<Channels, Priority>[] clientNetworkEnvironments = new QuicNetworkingEnvironment[numClients];

        for (int i = 0; i < numClients; i++) {
            clientConnectionHandlers[i] = new BlockingPollableConnectionHandler<>();
            clientNetworkEnvironments[i] = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandlers[i]);
        }

        AtomicReference<Connection>[] server = new AtomicReference[numClients];
        AtomicReference<Connection>[] clients = new AtomicReference[numClients];
        for (int i = 0; i < numClients; i++) {
            clients[i] = new AtomicReference<>();
            server[i] = new AtomicReference<>();
        }

        for (int i = 0; i < numClients; i++) {
            final int index = i;
            assertDoesNotThrow(() -> server[index].set(serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS)));
            assertDoesNotThrow(() -> clients[index].set(clientConnectionHandlers[index].pollNewConnection(100, TimeUnit.MILLISECONDS)));
        }

        for (int i = 0; i < numClients; i++) {
            assertNotNull(server[i].get());
            assertNotNull(clients[i].get());
        }

        EventLoopGroup eventLoopGroup = serverNetworkEnvironment.getEventLoopGroup();

        final CountDownLatch closeClientLatch = new CountDownLatch(numClients);
        for (int i = 0; i < numClients; i++) {
            final int index = i;
            eventLoopGroup.execute(() -> {
                assertDoesNotThrow(() -> clientNetworkEnvironments[index].close().await());
                assertDoesNotThrow(() -> clientNetworkEnvironments[index].shutdownGracefully().await());
                closeClientLatch.countDown();
            });
        }

        closeClientLatch.await();
        serverNetworkEnvironment.close().await();
        serverNetworkEnvironment.shutdownGracefully().await();
    }
}
