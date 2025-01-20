package com.hirshi001.examples.meagerexamples;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.ConnectionFactory;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.quicnetworking.helper.QuicNetworkingEnvironment;
import com.hirshi001.tests.util.NetworkEnvironment;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import com.hirshi001.tests.util.TestUtils;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

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

    public static void main(String[] args) throws Exception {
        BlockingPollableConnectionHandler<Channels, Priority> connectionHandler = new BlockingPollableConnectionHandler<>();

        QuicNetworkingEnvironment<Channels, Priority> networkEnvironment = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), connectionHandler);


        Scanner scanner = new Scanner(System.in);
        while (true) {

            if(System.in.available() > 0) {
                String message = scanner.nextLine();
                if(message.equals("exit")) {
                    break;
                }
            }

            System.out.println("Waiting for new connection");
            Connection<Channels, Priority> newConnection = connectionHandler.pollNewConnection();
            if(newConnection == null) {
                continue;
            }
            System.out.println("New connection accepted");
            QChannel textChannel = newConnection.getChannel(Channels.Text);

            System.out.println("Creating Text Output Stream");
            textChannel.openOutputStream(QChannel.Reliability.RELIABLE).sync();

            Channel textOutChannel = textChannel.getOutChannel();
            assert textOutChannel.isActive();
            assert textOutChannel.isOpen();
            System.out.println("Writing to Text Output Stream");
            textOutChannel.writeAndFlush(Unpooled.copiedBuffer("Hello World from text", CharsetUtil.US_ASCII));

            Thread.sleep(1000);

            textOutChannel.writeAndFlush(Unpooled.copiedBuffer("Hello World 2! from text", CharsetUtil.US_ASCII));

            QChannel voiceChannel = newConnection.getChannel(Channels.Voice);
            System.out.println("Creating Voice Output Stream");
            voiceChannel.openOutputStream(QChannel.Reliability.UNRELIABLE).sync();

            Thread.sleep(2000);
            Channel voiceOutChannel = voiceChannel.getOutChannel();
            assert voiceOutChannel.isActive();
            assert voiceOutChannel.isOpen();
            System.out.println("Writing to Voice Output Stream");
            voiceOutChannel.writeAndFlush(Unpooled.copiedBuffer("Hello World from Voice", CharsetUtil.US_ASCII));

        }

        networkEnvironment.close().await();
        networkEnvironment.shutdownGracefully().await();
    }


}
