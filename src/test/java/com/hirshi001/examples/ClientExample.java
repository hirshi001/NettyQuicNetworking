package com.hirshi001.examples;

import static com.hirshi001.examples.Shared.Channels;
import static com.hirshi001.examples.Shared.Priority;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.quicnetworking.util.ByteBufferUtil;
import com.hirshi001.tests.util.NetworkEnvironment;
import com.hirshi001.tests.util.TestUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class ClientExample {

    public static String name;

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("Client Starting");
        System.out.println("What is your name?");
        Scanner scanner = new Scanner(System.in);
        name = scanner.nextLine();


        BlockingPollableConnectionHandler<Channels, Priority> connectionHandler = new BlockingPollableConnectionHandler<>();
        NetworkEnvironment<Channels, Priority> networkEnvironment = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), connectionHandler);

        Connection<Channels, Priority> newConnection = connectionHandler.pollNewConnection();

        Thread textChannelThread = new TextChannelThread(newConnection);
        textChannelThread.start();

        textChannelThread.join();
        System.out.println("Client exiting");

        networkEnvironment.close();
    }

    static class TextChannelThread extends Thread {
        private final Connection<Channels, Priority> connection;

        public TextChannelThread(Connection<Channels, Priority> connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            connection.setChannelPriority(Channels.TextChannel, Priority.LOW);
            QChannel textChannel = connection.getChannel(Channels.TextChannel);

            // set handler
            textChannel.setChannelHandler(new ChannelInboundHandlerAdapter(){
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if(msg instanceof ByteBuf in) {
                        String name = ByteBufferUtil.readStringFromBuf(in);
                        String message = ByteBufferUtil.readStringFromBuf(in);
                        System.out.println(name + ": " + message);
                    }else {
                        super.channelRead(ctx, msg);
                    }
                }

                @Override
                public boolean isSharable() {
                    return true;
                }
            });


            try {
                textChannel.openOutputStream(QChannel.Reliability.RELIABLE).sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Scanner scanner = new Scanner(System.in);


            System.out.println("Type 'exit' to exit");
            while (true) {
                String line = scanner.nextLine();
                if (line.equals("exit")) {
                    connection.close();
                    break;
                }

                ByteBuf buffer = Unpooled.directBuffer(256);
                ByteBufferUtil.writeStringToBuf(name, buffer);
                ByteBufferUtil.writeStringToBuf(line, buffer);
                textChannel.writeAndFlush(buffer);
            }
            System.out.println("Exiting");
        }
    }
}
