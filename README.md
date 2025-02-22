# NettyQuicNetworking
A thin wrapper around Netty Quic which makes it easier to use. This library send some metadata at the beginning of each
stream created and datagram sent in order to demultiplex the streams/datagrams into their corresponding Channels.

## Usage
### Shared
First you must define what Channels and Priority levels you wish to support
```java
public class Shared {
    public enum Channels {
        TextChannel, VoiceChannel, GameChannel
    }

    // Always declare higher priority levels first
    public enum Priority {
        Urgent, High, Medium, Low
    }
}
```

### Server
Since this is a light wrapper around Netty Quic, you will directly need to create the Netty Quic Server and Clients, and
add elements from NettyQuicNetworking as handlers.
```java
public static void main(String[] args) {

    // First define the connection handler you wish to use for when new connections are made to your server
    BlockingPollableConnectionHandler<Channels, Priority> connectionHandler = new BlockingPollableConnectionHandler<>();
    
    
    // Then create the server
    SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
    QuicSslContext context = QuicSslContextBuilder.forServer(
                    selfSignedCertificate.privateKey(), null, selfSignedCertificate.certificate())
            .applicationProtocols("example")
            .build();

    ServerConfig serverConfig = new ServerConfig();
    serverConfig.setSslContext(context);
    serverConfig.setTokenHandler(InsecureQuicTokenHandler.INSTANCE);
    serverConfig.setEventLoopGroup(new NioEventLoopGroup());

    QuicNetworkingEnvironment<Channels, Priority> serverNetworkEnvironment = QuicNetworkingHelper.createServer(serverConfig, address, connectionHandler, channelsClass, priorityClass);

    // Now you can use the connection handler to handle new connections:

    while (true) {
        Connection<Channel, Priority> newConnection = connectionHandler.poll();
        // handle the connection
        handleConnection(newConnection);
    }
    
    // Don't forget to close the server when you are done
    serverNetworkEnvironment.close().awaitUninterruptibly();
    serverNetworkEnvironment.shutdownGracefully().awaitUninterruptibly();
}
```

## Client
Very similar to the server, but pay attention to where the handler and streamHandler is added to in this case
```java
public static void main(String[] args) {

    // First define the connection handler you wish to use for when the client connects to the server
    BlockingPollableConnectionHandler<Channels, Priority> connectionHandler = new BlockingPollableConnectionHandler<>();

    // Then create the client
    QuicSslContext context = QuicSslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .applicationProtocols("example")
            .build();

    ClientConfig clientConfig = new ClientConfig();
    clientConfig.setSslContext(context);
    clientConfig.setEventLoopGroup(new NioEventLoopGroup());

    QuicNetworkingEnvironment clientNetworkingEnvironment = QuicNetworkingHelper.createClient(clientConfig, remoteAddress, connectionHandler, channelsClass, priorityClass);

    // Now you can use the connection handler to handle new connections:
    Connection<Channel, Priority> connection = connectionHandler.poll();
    // handle the connection
    handleConnection(connection);
    
    // Don't forget to close the client when you are done
    connection.close().awaitUninterruptibly();
    clientNetworkingEnvironment.close().awaitUninterruptibly();
    clientNetworkingEnvironment.shutdownGracefully().awaitUninterruptibly();

}
```

## What you can do with your new connections
There are different things you can do with your connection. Here are some examples

### Getting channels and using them
```java
public static void handleConnection(Connection<Channel, Priority> connection) {
    // You can get any channel you want
    QC textChannel = connection.getChannel(Channels.TextChannel);

    // You can set the priority of the channel
    connection.setChannelPriority(Channel.TextChannel, Priority.Low);

    // If you want to send data to the channel, you can do so by
    textChannel.openOutputStream(QChannel.Reliability.RELIABLE);
    textChannel.writeAndFlush(Unpooled.copiedBuffer("Hello, World!".getBytes()));

    // You likely want to handle data coming from the other end of the channel
    textChannel.setChannelHandler(new ChannelInboundHandlerAdapter() {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println(buf.toString(StandardCharsets.UTF_8));
        }
        
        // IMPORTANT, the channel handler must be sharable if the channel is both written to and read from
        @Override
        public boolean isSharable() {
            return true;
        }
    });

    // You can close the channel
    textChannel.close();
}

```

### Message Handlers
This library provides a way to automatically register and handle messages (Objects) of different types.
```java
public static void handleConnection(Connection<Channel, Priority> connection) {
    
    // Create the Message Registry
    MessageRegistry messageRegistry = new DefaultMessageRegistry();
    
    // You must create handlers (Functional Interfaces) for each message type you expect to receive
    messageRegistry.register(StringMessage::new, Handlers::handleStringMessage, 0); 
    messageRegistry.register(IntegerArrayMessage::new, Handlers::handleIntegerArrayMessage, 1);
    messageRegistry.register(PlayerMoveMessage::new, 2); // No need to add handler if you are only sending and not receiving

    QC textChannel = connection.getChannel(Channels.GameChannel);
    connection.setChannelPriority(Channel.GameChannel, Priority.High);

    textChannel.openOutputStream(QChannel.Reliability.Unreliable);

    // You likely want to handle data coming from the other end of the channel
    textChannel.setChannelHandler(new ChannelInboundHandlerAdapter() {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // MessageDecoder is required to encode and decode messages
            // In order to handle the messages, you can use either AsyncMessageHandler, PollableMessageHandler, or some other handler of your choice.
            // AsyncMessageHandler is recommended for most cases, as it will automatically call the corresponding message handler for you
            ctx.pipeline().addLast(new MessageCodec(messageRegistry), new AsyncMessageHandler(messageRegistry));
        }

        // IMPORTANT, the channel handler must be sharable if the channel is both written to and read from
        @Override
        public boolean isSharable() {
            return true;
        }
    });

    // You can send a message, as long as the other side has a handler for it
    connection.sendMessage(new StringMessage("Hello, World!"));
    connection.sendMessage(new IntegerArrayMessage(new int[]{1, 2, 3, 4, 5}));
    connection.sendMessage(new PlayerMoveMessage(Player.position.x, Player.position.y));
    
    // ...
    textChannel.close();
}
```



