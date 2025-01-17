package com.hirshi001.tests.channeltests;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.tests.TestUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SingleClientSingleChannelUnreliableTests {

    enum Priority {
    }

    enum Channels {
        C1
    }

    @Test
    public void unreliableClientReceiveChannelHandlerSetBeforeFirst() throws ExecutionException, InterruptedException, CertificateException {
        final String message = "Hello World from Server";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());


        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<Channels, Priority> serverConnectionFactory = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);

        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<Channels, Priority> clientConnectionFactory = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);

        Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);

        Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        QChannel clientC1 = clientConnection.getChannel(Channels.C1);
        Promise<ByteBuf> receivedBuffer = clientConnection.getConnection().eventLoop().newPromise();
        clientC1.setChannelHandler(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (!receivedBuffer.isDone())
                    receivedBuffer.setSuccess((ByteBuf) msg);
            }
        });

        QChannel serverC1 = serverConnection.getChannel(Channels.C1);
        serverC1.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();
        serverC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

        assertTrue(receivedBuffer.await(100, TimeUnit.MILLISECONDS), "Message not received in time");
        ByteBuf received = receivedBuffer.get();

        assertEquals(message, received.toString(Charset.defaultCharset()), "Received message does not match sent message");


        assertTrue(serverConnection.close().await(100, TimeUnit.MILLISECONDS));
        assertTrue(clientConnection.close().await(100, TimeUnit.MILLISECONDS));
    }


    @Test
    public void unreliableClientReceiveChannelHandlerSetAfterFirst() throws ExecutionException, InterruptedException, CertificateException {
        final String message = "Hello World from Server";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());


        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<Channels, Priority> serverConnectionFactory = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);

        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<Channels, Priority> clientConnectionFactory = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);

        Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        QChannel serverC1 = serverConnection.getChannel(Channels.C1);
        serverC1.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();
        serverC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

        Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        QChannel clientC1 = clientConnection.getChannel(Channels.C1);
        Promise<ByteBuf> receivedBuffer = clientConnection.getConnection().eventLoop().newPromise();
        clientC1.setChannelHandler(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (!receivedBuffer.isDone())
                    receivedBuffer.setSuccess((ByteBuf) msg);
            }
        });


        // now try to send another message after channel handler already set
        final String message2 = "Hello World 2! from Server";
        final byte[] messageBytes2 = message2.getBytes(Charset.defaultCharset());

        serverC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes2)).sync();

        assertTrue(receivedBuffer.await(100, TimeUnit.MILLISECONDS), "Message not received in time");
        ByteBuf received2 = receivedBuffer.get();

        assertEquals(message2, received2.toString(Charset.defaultCharset()), "Second received message does not match sent message");


        assertTrue(serverConnection.close().await(100, TimeUnit.MILLISECONDS));
        assertTrue(clientConnection.close().await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void unreliableServerReceiveChannelHandlerSetBeforeFirst() throws ExecutionException, InterruptedException, CertificateException {
        final String message = "Hello World from Client";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());

        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<Channels, Priority> serverConnectionFactory = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);


        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<Channels, Priority> clientConnectionFactory = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);

        Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        QChannel serverC1 = serverConnection.getChannel(Channels.C1);

        Promise<ByteBuf> receivedBuffer = serverConnection.getConnection().eventLoop().newPromise();
        serverC1.setChannelHandler(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (!receivedBuffer.isDone())
                    receivedBuffer.setSuccess((ByteBuf) msg);
            }
        });

        Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        QChannel clientC1 = clientConnection.getChannel(Channels.C1);
        clientC1.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();
        clientC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

        assertTrue(receivedBuffer.await(100, TimeUnit.MILLISECONDS), "Message not received in time");
        ByteBuf received = receivedBuffer.get();

        assertEquals(message, received.toString(Charset.defaultCharset()), "Received message does not match sent message");


        assertTrue(serverConnection.close().await(100, TimeUnit.MILLISECONDS));
        assertTrue(clientConnection.close().await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void unreliableServerReceiveChannelHandlerSetAfterFirst() throws ExecutionException, InterruptedException, CertificateException {
        final String message = "Hello World from Client";
        final byte[] messageBytes = message.getBytes(Charset.defaultCharset());

        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<Channels, Priority> serverConnectionFactory = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);


        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<Channels, Priority> clientConnectionFactory = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);

        Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        QChannel serverC1 = serverConnection.getChannel(Channels.C1);

        Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        QChannel clientC1 = clientConnection.getChannel(Channels.C1);
        clientC1.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();
        clientC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes)).sync();

        Promise<ByteBuf> receivedBuffer = serverConnection.getConnection().eventLoop().newPromise();
        serverC1.setChannelHandler(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (!receivedBuffer.isDone())
                    receivedBuffer.setSuccess((ByteBuf) msg);
            }
        });


        // now try to send another message
        final String message2 = "Hello World 2! from Client";
        final byte[] messageBytes2 = message2.getBytes(Charset.defaultCharset());

        clientC1.writeAndFlush(Unpooled.copiedBuffer(messageBytes2)).sync();

        assertTrue(receivedBuffer.await(100, TimeUnit.MILLISECONDS), "Message not received in time");
        ByteBuf received2 = receivedBuffer.get();

        assertEquals(message2, received2.toString(Charset.defaultCharset()), "Second received message does not match sent message");


        assertTrue(serverConnection.close().await(100, TimeUnit.MILLISECONDS));
        assertTrue(clientConnection.close().await(100, TimeUnit.MILLISECONDS));
    }
}
