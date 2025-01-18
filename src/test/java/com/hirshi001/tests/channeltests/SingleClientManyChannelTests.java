package com.hirshi001.tests.channeltests;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.tests.util.NetworkEnvironment;
import com.hirshi001.tests.util.TestUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SingleClientManyChannelTests {


    enum Priority {
        High,
        Medium,
        Low
    }


    // make have 100 channels
    enum Channels {
        C1, C2, C3, C4, C5, C6, C7, C8, C9, C10,
        C11, C12, C13, C14, C15, C16, C17, C18, C19, C20,
        C21, C22, C23, C24, C25, C26, C27, C28, C29, C30,
        C31, C32, C33, C34, C35, C36, C37, C38, C39, C40,
        C41, C42, C43, C44, C45, C46, C47, C48, C49, C50,
        C51, C52, C53, C54, C55, C56, C57, C58, C59, C60,
        C61, C62, C63, C64, C65, C66, C67, C68, C69, C70,
        C71, C72, C73, C74, C75, C76, C77, C78, C79, C80,
        C81, C82, C83, C84, C85, C86, C87, C88, C89, C90,
        C91, C92, C93, C94, C95, C96, C97, C98, C99, C100
    }

    @Test
    public void manyChannelsTest() throws ExecutionException, InterruptedException, CertificateException {
        final String message = "Hello World from Server";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());

        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        NetworkEnvironment<Channels, Priority> serverNetworkEnvironment = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);

        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();
        NetworkEnvironment<Channels, Priority> clientNetworkEnvironment = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);

        Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);


        // Open all channels, 0-49 are reliable, 50-99 are unreliable
        AtomicInteger serverReceivedCount = new AtomicInteger(0);
        CountDownLatch serverReceivedLatch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            QChannel serverC = serverConnection.getChannel(Channels.values()[i]);

            final boolean reliable = i < 50;
            serverC.openOutputStream(reliable? QChannel.Reliability.RELIABLE : QChannel.Reliability.UNRELIABLE).sync();

            serverC.setChannelHandler(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    if(!reliable) {
                        ReferenceCountUtil.release(msg);
                    }
                    serverReceivedCount.incrementAndGet();
                    serverReceivedLatch.countDown();
                }

                @Override
                public boolean isSharable() {
                    return true;
                }
            });
        }

        Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        AtomicInteger clientReceivedCount = new AtomicInteger(0);
        CountDownLatch clientReceivedLatch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            QChannel clientC = clientConnection.getChannel(Channels.values()[i]);
            final boolean reliable = i < 50;
            clientC.openOutputStream(reliable? QChannel.Reliability.RELIABLE : QChannel.Reliability.UNRELIABLE).sync();

            clientC.setChannelHandler(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    if(!reliable) {
                        ReferenceCountUtil.release(msg);
                    }
                    clientReceivedCount.incrementAndGet();
                    clientReceivedLatch.countDown();
                }

                @Override
                public boolean isSharable() {
                    return true;
                }
            });
        }

        // Send a message on each channel
        for (int i = 0; i < 100; i++) {
            QChannel serverC = serverConnection.getChannel(Channels.values()[i]);
            serverC.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

            QChannel clientC = clientConnection.getChannel(Channels.values()[i]);
            clientC.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();
        }


        assertTrue(serverReceivedLatch.await(100, TimeUnit.MILLISECONDS), "Server did not receive all messages in time");
        assertTrue(clientReceivedLatch.await(100, TimeUnit.MILLISECONDS), "Client did not receive all messages in time");
        assertEquals(100, serverReceivedCount.get(), "Server did not receive all messages");
        assertEquals(100, clientReceivedCount.get(), "Client did not receive all messages");


        clientNetworkEnvironment.close();
        serverNetworkEnvironment.close();
    }

}
