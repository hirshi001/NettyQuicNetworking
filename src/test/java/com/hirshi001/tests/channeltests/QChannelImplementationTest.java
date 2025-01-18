package com.hirshi001.tests.channeltests;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.tests.util.NetworkEnvironment;
import com.hirshi001.tests.util.TestUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class QChannelImplementationTest {


    enum Priority {
    }

    enum Channels {
        C1
    }


    @Test
    public void QChannelImplTest() throws ExecutionException, InterruptedException, CertificateException {
        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        NetworkEnvironment<Channels, Priority> serverNetworkEnvironment = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);

        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();
        NetworkEnvironment<Channels, Priority> clientNetworkEnvironment = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);

        Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);

        Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);

        QChannel clientC1 = clientConnection.getChannel(Channels.C1);
        QChannel serverC1 = serverConnection.getChannel(Channels.C1);


        // Asserts regarding openOutputStream
        assertThrows(IllegalArgumentException.class, () -> clientC1.openOutputStream(null));
        assertThrows(IllegalArgumentException.class, () -> serverC1.openOutputStream(null));

        AtomicReference<Future<? extends Channel>> channelFuture = new AtomicReference<>();

        assertDoesNotThrow(() -> channelFuture.set(clientC1.openOutputStream(QChannel.Reliability.RELIABLE)));
        assertTrue(channelFuture.get().sync().isSuccess());

        assertDoesNotThrow(() -> channelFuture.set(serverC1.openOutputStream(QChannel.Reliability.UNRELIABLE)));
        assertTrue(channelFuture.get().sync().isSuccess());


        assertThrows(IllegalStateException.class, () -> clientC1.openOutputStream(QChannel.Reliability.RELIABLE));
        assertThrows(IllegalStateException.class, () -> serverC1.openOutputStream(QChannel.Reliability.UNRELIABLE));

        // Asserts regarding setChannelHandler
        assertThrows(IllegalArgumentException.class, () -> clientC1.setChannelHandler(null));
        assertDoesNotThrow(() -> clientC1.setChannelHandler(new ChannelInboundHandlerAdapter() {
        }));
        assertThrows(IllegalStateException.class, () -> clientC1.setChannelHandler(new ChannelInboundHandlerAdapter() {
        }));


        clientNetworkEnvironment.close();
        serverNetworkEnvironment.close();
    }

}
