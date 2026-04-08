package com.hirshi001.examples.meagerexamples;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.quicnetworking.helper.QuicNetworkingEnvironment;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import com.hirshi001.tests.util.TestUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;


public class ServerTest {


    public enum Priority {
        High,
        Medium,
        Low
    }

    public enum Channels {
        Text,
        Voice,
        Video
    }

    static void main() throws Exception {
        BlockingPollableConnectionHandler<Channels, Priority> connectionHandler = new BlockingPollableConnectionHandler<>();

        try (QuicNetworkingEnvironment<Channels, Priority> networkEnvironment = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), connectionHandler)) {
            runServer(networkEnvironment, connectionHandler);
        }
    }

    @SuppressWarnings("BusyWait")
    static void runServer(QuicNetworkingEnvironment<Channels, Priority> networkEnvironment, BlockingPollableConnectionHandler<Channels, Priority> connectionHandler) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (System.in.available() > 0) {
                String message = scanner.nextLine();
                if (message.equals("exit")) {
                    break;
                }
            }

            System.out.println("Waiting for new connection");

            Connection<Channels, Priority> newConnection = connectionHandler.pollNewConnection();
            if (newConnection == null) {
                continue;
            }

            System.out.println("New connection accepted");
            QChannel textChannel = newConnection.getChannel(Channels.Text);

            System.out.println("Creating Text Output Stream");
            textChannel.openOutputStream(QChannel.Reliability.RELIABLE).sync();

            assert textChannel.getOutputState() == QChannel.OutputState.ACTIVE;
            System.out.println("Writing to Text Output Stream");
            textChannel.writeAndFlush(Unpooled.copiedBuffer("Hello World from text", CharsetUtil.US_ASCII));

            Thread.sleep(1000);

            textChannel.writeAndFlush(Unpooled.copiedBuffer("Hello World 2! from text", CharsetUtil.US_ASCII));

            QChannel voiceChannel = newConnection.getChannel(Channels.Voice);
            System.out.println("Creating Voice Output Stream");
            voiceChannel.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();

            Thread.sleep(2000);
            assert voiceChannel.getOutputState() == QChannel.OutputState.ACTIVE;
            System.out.println("Writing to Voice Output Stream");
            voiceChannel.writeAndFlush(Unpooled.copiedBuffer("Hello World from Voice", CharsetUtil.US_ASCII));
        }
    }
}

