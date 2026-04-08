package com.hirshi001.tests.channeltests;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.tests.util.TestUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.NetUtil;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SingleClientSingleChannelReliableTests {

    enum Priority {
        High,
        Medium,
        Low
    }


    enum Channels {
        C1,
        C2
    }

    @Test
    public void reliableClientReceive() throws Exception {
        final String message = "Hello World from Server";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());


        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();

        try (var _ = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);
             var _ = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);
        ) {

            Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            assertNotNull(serverConnection);
            QChannel serverC1 = serverConnection.getChannel(Channels.C1);
            serverC1.openOutputStream(QChannel.Reliability.RELIABLE).sync();
            serverC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

            Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            assertNotNull(clientConnection);
            QChannel clientC1 = clientConnection.getChannel(Channels.C1);

            BlockingQueue<ByteBuf> received = new LinkedBlockingQueue<>();
            ByteBuf buf;

            clientC1.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    received.add(((ByteBuf) msg).retain());
                }
            });

            // Message should be received almost immediately
            buf = received.poll(100, TimeUnit.MILLISECONDS);
            assertNotNull(buf, "Message not received in time");
            assertEquals(message, buf.toString(Charset.defaultCharset()), "First received message does not match sent message");


            // now try to send another message
            final String message2 = "Hello World 2! from Server";
            final byte[] messageBytes2 = message2.getBytes(Charset.defaultCharset());

            serverC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes2)).sync();

            buf = received.poll(100, TimeUnit.MILLISECONDS);
            assertNotNull(buf, "Message not received in time");
            assertEquals(message2, buf.toString(Charset.defaultCharset()), "Second received message does not match sent message");

            clientC1.close().sync();
            serverC1.close().sync();
        }
    }

    @Test
    public void reliableServerReceive() throws Exception {
        final String message = "Hello World from Client";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());

        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();

        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();
        try (var _ = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);
             var _ = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);
        ) {
            Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            assertNotNull(serverConnection);
            QChannel serverC1 = serverConnection.getChannel(Channels.C1);

            Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            assertNotNull(clientConnection);
            QChannel clientC1 = clientConnection.getChannel(Channels.C1);
            clientC1.openOutputStream(QChannel.Reliability.RELIABLE).sync();
            clientC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

            BlockingQueue<ByteBuf> received = new LinkedBlockingQueue<>();
            ByteBuf buf;
            serverC1.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    received.add(((ByteBuf) msg).retain());
                }
            });

            // Message should be received almost immediately
            buf = received.poll(100, TimeUnit.MILLISECONDS);
            assertNotNull(buf, "Message not received in time");
            assertEquals(message, buf.toString(Charset.defaultCharset()), "First received message does not match sent message");

            // now try to send another message
            final String message2 = "Hello World 2! from Client";
            final byte[] messageBytes2 = message2.getBytes(Charset.defaultCharset());

            clientC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes2)).sync();

            buf = received.poll(100, TimeUnit.MILLISECONDS);
            assertNotNull(buf, "Message not received in time");
            assertEquals(message2, buf.toString(Charset.defaultCharset()), "Second received message does not match sent message");

            clientC1.close().sync();
            serverC1.close().sync();
        }
    }
}
