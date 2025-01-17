# NettyQuicNetworking
A thin wrapper around Netty Quic which makes it easier to use. This library send some metadata at the beginning of each
stream created and datagram sent in order to demultiplex the streams/datagrams into their corresponding Channels.

## Usage
### Shared
```java
public class Shared {
    // First define what channels and priority levels you want to use
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
```java
public static void main(String[] args) {

    // First define the connection handler you wish to use for when new connections are made to your server
    BlockingPollableConnectionHandler<Channels, Priority> connectionHandler = new BlockingPollableConnectionHandler<>();
    // Then create the Connection Factory
    ConnectionFactory<Channels, Priority> connectionFactory = new ConnectionFactory<>(connectionHandler, Channels.class, Priority.class);

    // Then create the Netty Quic Server, passing in connectionFactory.handler() and connectionFactory.streamHandler() to the handler and stream handler
    // This is just an quick example, but it is missing some core components, please look at the full example on the netty quic incubator page

    NioEventLoopGroup group = new NioEventLoopGroup();
    ChannelHandler codec = new QuicServerCodecBuilder()
            // Configure some limits for the maximal number of streams (and the data) that we want to handle.
            // ...
            // Make sure to include this if you want to support unreliable channels
            .datagram(2048, 2048)

            // IMPORTANT: Set the handler and stream handler
            .handler(connectionFactory.handler())
            .streamHandler(connectionFactory.streamHandler())
            .build();

    Bootstrap bs = new Bootstrap();
    Channel channel = bs.group(group)
            .channel(NioDatagramChannel.class)
            .handler(codec)
            .bind(new InetSocketAddress(9999/*port number*/)).sync().channel();

    // Finally, set the channel in the connection factory
    connectionFactory.setChannel(channel);

    // Now you can use the connection handler to handle new connections:

    while (true) {
        Connection<Channel, Priority> newConnection = ChannconnectionHandler.poll();
        // Create streams, add handlers, etc... do whatever with your new connections
    }
}
```

## Client

```java
import java.sql.Connection;

public static void main(String[] args) {

    // First define the connection handler you wish to use for when the client connects to the server
    BlockingPollableConnectionHandler<Channels, Priority> connectionHandler = new BlockingPollableConnectionHandler<>();
    // Then create the Connection Factory
    ConnectionFactory<Channels, Priority> connectionFactory = new ConnectionFactory<>(connectionHandler, Channels.class, Priority.class);

    NioEventLoopGroup group = new NioEventLoopGroup(1);
    ChannelHandler codec = new QuicClientCodecBuilder()
            // Configure some limits for the maximal number of streams (and the data) that we want to handle.
            // ...
            // Make sure to include this if you want to support unreliable channels
            .datagram(2048, 2048)
            // Don't set the handler and stream handler here, we will set it later
            .build();

    Bootstrap bs = new Bootstrap();
    Channel channel = bs.group(group)
            .channel(NioDatagramChannel.class)
            .handler(codec)
            .bind(0).sync().channel();

    // Set the channel in the connection factory
    connectionFactory.setChannel(channel);

    QuicChannel quicChannel = QuicChannel.newBootstrap(channel)
            // IMPORTANT: Set the handler and stream handler
            .handler(connectionFactory.handler())
            .streamHandler(connectionFactory.streamHandler())
            .remoteAddress(remoteAddress)
            .connect()
            .get();


    // Now you can use the connection handler to handle new connections:
    Connection<Channel, Priority> connection = connectionHandler.poll();
    
    // Create streams, add handlers, etc... do whatever with your new connections

}
```



