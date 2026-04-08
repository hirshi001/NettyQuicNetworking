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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SingleClientSingleChannelUnreliableTests {

    enum Priority {
    }

    enum Channels {
        C1
    }

    @Test
    public void unreliableClientReceiveChannelHandlerSetBeforeFirst() throws Exception {
        final var message = "Hello World from Server";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());

        var serverConnectionHandler = new BlockingPollableConnectionHandler<Channels, Priority>();
        var clientConnectionHandler = new BlockingPollableConnectionHandler<Channels, Priority>();
        try (var _ = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);
             var _ = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler)) {

            Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            QChannel clientC1 = clientConnection.getChannel(Channels.C1);

            var receivedBuffer = new CompletableFuture<ByteBuf>();
            clientC1.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    if (!receivedBuffer.isDone())
                        receivedBuffer.complete(((ByteBuf) msg).retain());
                }
            });

            QChannel serverC1 = serverConnection.getChannel(Channels.C1);
            serverC1.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();
            serverC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

            ByteBuf received = receivedBuffer.get(100, TimeUnit.MILLISECONDS);
            assertEquals(message, received.toString(Charset.defaultCharset()), "Received message does not match sent message");

            received.release();

            clientC1.close().sync();
            serverC1.close().sync();
        }
    }


    @Test
    public void unreliableClientReceiveChannelHandlerSetAfterFirst() throws Exception {
        final String message = "Hello World from Server";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());


        var serverConnectionHandler = new BlockingPollableConnectionHandler<Channels, Priority>();
        var clientConnectionHandler = new BlockingPollableConnectionHandler<Channels, Priority>();
        try (var _ = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);
             var _ = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler)) {


            Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            QChannel serverC1 = serverConnection.getChannel(Channels.C1);
            serverC1.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();
            serverC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

            Thread.sleep(100);

            Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            QChannel clientC1 = clientConnection.getChannel(Channels.C1);

            var receivedBuffer = new CompletableFuture<ByteBuf>();
            clientC1.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    if (!receivedBuffer.isDone())
                        receivedBuffer.complete(((ByteBuf) msg).retain());
                }
            });


            // now try to send another message after channel handler already set
            final String message2 = "Hello World 2! from Server";
            final byte[] messageBytes2 = message2.getBytes(Charset.defaultCharset());

            serverC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes2)).sync();

            ByteBuf received = receivedBuffer.get(100, TimeUnit.MILLISECONDS);

            assertEquals(message2, received.toString(Charset.defaultCharset()), "Second received message does not match sent message");
            received.release();

            clientC1.close().sync();
            serverC1.close().sync();
        }

    }


    @Test
    public void unreliableServerReceiveChannelHandlerSetBeforeFirst() throws Exception {
        final String message = "Hello World from Client";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());

        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();

        try (var _ = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);
             var _ = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler)) {

            Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            QChannel serverC1 = serverConnection.getChannel(Channels.C1);

            var receivedBuffer = new CompletableFuture<ByteBuf>();
            serverC1.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    if (!receivedBuffer.isDone())
                        receivedBuffer.complete(((ByteBuf) msg).retain());
                }
            });

            Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            QChannel clientC1 = clientConnection.getChannel(Channels.C1);
            clientC1.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();
            clientC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

            ByteBuf received = receivedBuffer.get(100, TimeUnit.MILLISECONDS);
            assertEquals(message, received.toString(Charset.defaultCharset()), "Received message does not match sent message");
            received.release();

            clientC1.close().sync();
            serverC1.close().sync();
        }
    }

    @Test
    public void unreliableServerReceiveChannelHandlerSetAfterFirst() throws Exception {
        final String message = "Hello World from Client";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());

        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();

        try (var _ = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);
        var _ = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);) {

            Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            QChannel serverC1 = serverConnection.getChannel(Channels.C1);

            Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
            QChannel clientC1 = clientConnection.getChannel(Channels.C1);
            clientC1.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();
            clientC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

            Thread.sleep(100);

            var receivedBuffer = new CompletableFuture<ByteBuf>();
            serverC1.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                    if (!receivedBuffer.isDone())
                        receivedBuffer.complete(((ByteBuf) msg).retain());
                }
            });


            // now try to send another message
            final String message2 = "Hello World 2! from Client";
            final byte[] messageBytes2 = message2.getBytes(Charset.defaultCharset());

            clientC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes2)).sync();

            ByteBuf received = receivedBuffer.get(100, TimeUnit.MILLISECONDS);

            assertEquals(message2, received.toString(Charset.defaultCharset()), "Second received message does not match sent message");
            received.release();

            clientC1.close().sync();
            serverC1.close().sync();
        }
    }
}
