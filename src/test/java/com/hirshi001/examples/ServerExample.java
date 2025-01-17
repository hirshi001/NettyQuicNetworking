package com.hirshi001.examples;

import static com.hirshi001.examples.Shared.*;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.quicnetworking.util.ByteBufferUtil;
import com.hirshi001.tests.TestUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ServerExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException, CertificateException {

        System.out.println("Server Starting");

        BlockingPollableConnectionHandler<Channels, Priority> connectionHandler = new BlockingPollableConnectionHandler<>();
        ConnectionFactory<Channels, Priority> connectionFactory = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress( 9999), connectionHandler);


        TextChannelHandler textChannelHandler = new TextChannelHandler();

        while(true) {
            Connection<Channels, Priority> newConnection = connectionHandler.pollNewConnection();
            textChannelHandler.newConnection(newConnection);
        }
    }

    static class TextChannelHandler {

        private final List<Connection<Channels, Priority>> connectionList = new ArrayList<>();

        public TextChannelHandler() {

        }

        public void newConnection(Connection<Channels, Priority> connection) {
            synchronized (connectionList) {
                connectionList.add(connection);
            }

            connection.setChannelPriority(Channels.TextChannel, Priority.LOW);
            QChannel textChannel = connection.getChannel(Channels.TextChannel);
            textChannel.openOutputStream(QChannel.Reliability.RELIABLE);
            textChannel.setChannelHandler(new ChannelInboundHandlerAdapter(){
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if(msg instanceof ByteBuf in) {
                        String name = ByteBufferUtil.readStringFromBuf(in);
                        String message = ByteBufferUtil.readStringFromBuf(in);
                        System.out.println(name + ": " + message);
                        sendToAll(name, message);
                    }
                    else{
                        super.channelRead(ctx, msg);
                    }
                }

                @Override
                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                    super.channelInactive(ctx);
                    synchronized (connectionList) {
                        connectionList.remove(connection);
                    }
                }

                @Override
                public boolean isSharable() {
                    return true;
                }
            });
        }

        private void sendToAll(String name, String message) {
            synchronized (connectionList) {
                for (Connection<Channels, Priority> connection : connectionList) {
                    QChannel textChannel = connection.getChannel(Channels.TextChannel);
                    ByteBuf buffer = Unpooled.directBuffer(256);
                    ByteBufferUtil.writeStringToBuf(name, buffer);
                    ByteBufferUtil.writeStringToBuf(message, buffer);
                    try {
                        textChannel.writeAndFlush(buffer);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
