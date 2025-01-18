package com.hirshi001.tests.messagecodectests;

import com.hirshi001.quicnetworking.channel.QChannel;
import com.hirshi001.quicnetworking.connection.Connection;
import com.hirshi001.quicnetworking.connectionfactory.connectionhandler.BlockingPollableConnectionHandler;
import com.hirshi001.quicnetworking.message.channelhandlers.AsyncMessageHandler;
import com.hirshi001.quicnetworking.message.channelhandlers.MessageCodec;
import com.hirshi001.quicnetworking.message.channelhandlers.MessageContext;
import com.hirshi001.quicnetworking.message.channelhandlers.PollableMessageHandler;
import com.hirshi001.quicnetworking.message.defaultmessages.arraymessages.IntegerArrayMessage;
import com.hirshi001.quicnetworking.message.defaultmessages.primitivemessages.StringMessage;
import com.hirshi001.quicnetworking.message.messageregistry.DefaultMessageRegistry;
import com.hirshi001.quicnetworking.message.messageregistry.MessageRegistry;
import com.hirshi001.tests.util.NetworkEnvironment;
import com.hirshi001.tests.util.TestUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.NetUtil;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class CodecAsyncHandlerUsageTest {

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
    public void reliableCodecAsyncHandlerUsageTest() throws ExecutionException, InterruptedException, CertificateException {
        codecAsyncHandlerUsageTest(QChannel.Reliability.RELIABLE);
    }


    @Test
    public void unreliableCodecAsyncHandlerUsageTest() throws ExecutionException, InterruptedException, CertificateException {
        codecAsyncHandlerUsageTest(QChannel.Reliability.UNRELIABLE);
    }


    private void codecAsyncHandlerUsageTest(QChannel.Reliability reliability) throws CertificateException, ExecutionException, InterruptedException {
        final String message = "Hello World from Server";
        final int[] messageArray = {1, 2, 3, 4, 5};

        BlockingPollableConnectionHandler<Channels, Priority> serverConnectionHandler = new BlockingPollableConnectionHandler<>();
        NetworkEnvironment<Channels, Priority> serverNetworkEnvironment = TestUtils.newServer(Channels.class, Priority.class, new InetSocketAddress(9999), serverConnectionHandler);

        BlockingPollableConnectionHandler<Channels, Priority> clientConnectionHandler = new BlockingPollableConnectionHandler<>();
        NetworkEnvironment<Channels, Priority> clientNetworkEnvironment = TestUtils.newClient(Channels.class, Priority.class, new InetSocketAddress(NetUtil.LOCALHOST4, 9999), clientConnectionHandler);

        MessageRegistry serverRegistry = new DefaultMessageRegistry();
        final Promise<StringMessage> serverReceivedStringMessage = serverNetworkEnvironment.eventLoopGroup.next().newPromise();
        final Promise<IntegerArrayMessage> serverReceivedArrayMessage = serverNetworkEnvironment.eventLoopGroup.next().newPromise();
        serverRegistry.register(StringMessage::new, (context, message1) -> {
            serverReceivedStringMessage.setSuccess(message1);
        }, StringMessage.class, 0);
        serverRegistry.register(IntegerArrayMessage::new, (context, message1) -> {
            serverReceivedArrayMessage.setSuccess(message1);
        }, IntegerArrayMessage.class, 1);

        MessageRegistry clientRegistry = new DefaultMessageRegistry();
        final Promise<StringMessage> clientReceivedStringMessage = clientNetworkEnvironment.eventLoopGroup.next().newPromise();
        final Promise<IntegerArrayMessage> clientReceivedArrayMessage = clientNetworkEnvironment.eventLoopGroup.next().newPromise();
        clientRegistry.register(StringMessage::new, (context, message1) -> {
            clientReceivedStringMessage.setSuccess(message1);
        }, StringMessage.class, 0);
        clientRegistry.register(IntegerArrayMessage::new, (context, message1) -> {
            clientReceivedArrayMessage.setSuccess(message1);
        }, IntegerArrayMessage.class, 1);


        Connection<Channels, Priority> serverConnection = serverConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        QChannel serverC1 = serverConnection.getChannel(Channels.C1);
        serverC1.setChannelHandler(new ChannelInboundHandlerAdapter() {
            @Override
            public void handlerAdded(ChannelHandlerContext ctx) {
                ctx.pipeline().addLast(new MessageCodec(serverRegistry), new AsyncMessageHandler(serverRegistry));
            }

            @Override
            public boolean isSharable() {
                return true;
            }
        });

        serverC1.openOutputStream(reliability).sync();

        Connection<Channels, Priority> clientConnection = clientConnectionHandler.pollNewConnection(100, TimeUnit.MILLISECONDS);
        QChannel clientC1 = clientConnection.getChannel(Channels.C1);
        clientC1.setChannelHandler(new ChannelInboundHandlerAdapter() {
            @Override
            public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                ctx.pipeline().addLast(new MessageCodec(clientRegistry), new AsyncMessageHandler(clientRegistry));
            }

            @Override
            public boolean isSharable() {
                return true;
            }
        });
        clientC1.openOutputStream(reliability).sync();


        // handlers set up, now send messages
        serverC1.writeAndFlush(new StringMessage(message)).sync();
        serverC1.writeAndFlush(new IntegerArrayMessage(messageArray)).sync();

        clientC1.writeAndFlush(new StringMessage(message)).sync();
        clientC1.writeAndFlush(new IntegerArrayMessage(messageArray)).sync();

        // Check Server received messages
        assertTrue(serverReceivedStringMessage.await(100, TimeUnit.MILLISECONDS), "Server did not receive string message");
        assertEquals(message, serverReceivedStringMessage.get().value, "Server received string message does not match sent message");

        assertTrue(serverReceivedArrayMessage.await(100, TimeUnit.MILLISECONDS), "Server did not receive array message");
        assertArrayEquals(messageArray, serverReceivedArrayMessage.get().array, "Server received array message does not match sent message");

        // Check Client received messages
        assertTrue(clientReceivedStringMessage.await(100, TimeUnit.MILLISECONDS), "Client did not receive string message");
        assertEquals(message, clientReceivedStringMessage.get().value, "Client received string message does not match sent message");

        assertTrue(clientReceivedArrayMessage.await(100, TimeUnit.MILLISECONDS), "Client did not receive array message");
        assertArrayEquals(messageArray, clientReceivedArrayMessage.get().array, "Client received array message does not match sent message");



        clientNetworkEnvironment.close();
        serverNetworkEnvironment.close();


    }
}
