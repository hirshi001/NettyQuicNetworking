package com.hirshi001.examples.meagerexamples;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;
import com.hirshi001.tests.TestUtils;

import java.net.InetSocketAddress;

public final class ClientTest {

    private ClientTest() {
    }

    public static void main(String[] args) throws Exception {


        BlockingPollableConnectionHandler<ServerTest.Channels, ServerTest.Priority> connectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<ServerTest.Channels, ServerTest.Priority> connectionFactory = TestUtils.newClient(ServerTest.Channels.class, ServerTest.Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), connectionHandler);

        Connection<ServerTest.Channels, ServerTest.Priority> newConnection = connectionHandler.pollNewConnection();


        // Reliable receive example
        QChannel textChannel = newConnection.getChannel(ServerTest.Channels.Text);
        Thread.sleep(500);
        DefaultPromise<ByteBuf> receiveFuture = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        textChannel.setChannelHandler(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ByteBuf byteBuf = (ByteBuf) msg;

                if (!receiveFuture.isSuccess())
                    receiveFuture.setSuccess(byteBuf);

                System.out.println("Received message:" + byteBuf.toString(CharsetUtil.US_ASCII));
            }
        });

        receiveFuture.sync();

        // Unreliable receive example (will  be received since localhost)
        QChannel voiceChannel = newConnection.getChannel(ServerTest.Channels.Voice);

        DefaultPromise<ByteBuf> receiveFuture2 = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        voiceChannel.setChannelHandler(new ChannelInboundHandlerAdapter() {
            @Override
            public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                System.out.println("Handler added");
                super.handlerAdded(ctx);
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ByteBuf byteBuf = (ByteBuf) msg;
                receiveFuture2.setSuccess(byteBuf);
            }
        });
        System.out.println("Waiting for message");
        receiveFuture2.sync();
        System.err.println("Received message:" + receiveFuture2.get().toString(CharsetUtil.US_ASCII));

        newConnection.close();
    }
}
