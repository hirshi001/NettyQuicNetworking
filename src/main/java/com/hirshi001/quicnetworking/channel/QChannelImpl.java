package com.hirshi001.quicnetworking.channel;

import com.hirshi001.quicnetworking.connection.Connection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.PromiseCombiner;

import java.net.SocketAddress;

public class QChannelImpl extends AbstractChannel implements QChannel {

    private enum ChannelState {
        OPEN,
        ACTIVE,
        CLOSED
    }


    private final Connection<?, ?> connection;
    private final Enum<?> channelId;
    private final StreamGenerator streamGenerator;
    private final PriorityUpdater priorityUpdater;
    private int priority; // TODO: Learn default priority value

    private volatile io.netty.channel.Channel inputStream, outputStream; // For reliable unidirectional streams
    private volatile ChannelHandler reliableInputStreamHandler;

    private volatile Reliability outputReliability;

    private final Object lock = new Object();

    InputState inputState = InputState.IDLE;
    OutputState outputState = OutputState.IDLE;
    ChannelState state = ChannelState.OPEN;

    private final ChannelMetadata metadata;
    private final QChannelConfig config;


    public QChannelImpl(Connection<?, ?> connection, Enum<?> channelId, StreamGenerator streamGenerator, PriorityUpdater priorityUpdater) {
        super(connection.getConnection());
        this.connection = connection;
        this.channelId = channelId;
        this.streamGenerator = streamGenerator;
        this.priorityUpdater = priorityUpdater;
        this.metadata = new ChannelMetadata(true, parent().metadata().defaultMaxMessagesPerRead());
        this.config = new QChannelConfig(this);
        outputReliability = null;

    }

    private int channelId() {
        return channelId.ordinal();
    }

    @Override
    public OutputState getOutputState() {
        return outputState;
    }

    @Override
    public InputState getInputState() {
        return inputState;
    }

    @Override
    public Future<QChannelImpl> openOutputStream(Reliability reliability) {
        if(reliability == null)
            throw new IllegalArgumentException("Reliability cannot be null");
        if(outputState != OutputState.IDLE)
            throw new IllegalStateException("Output stream already open");

        EventLoop eventLoop = eventLoop();
        Promise<QChannelImpl> promise = eventLoop.newPromise();
        eventLoop.execute(() -> {
            if (outputState != OutputState.IDLE) {
                promise.setFailure(new IllegalStateException("Output stream is not idle. State: " + outputState));
                return;
            }
            outputState = OutputState.OPENING;
            switch (reliability) {
                case UNRELIABLE -> openUnreliableOutputStream(promise);
                case RELIABLE -> openReliableOutputStream(promise);
            }
        });
        return promise;
    }

    // must be in event loop
    private void openUnreliableOutputStream(Promise<QChannelImpl> promise) {
        assert eventLoop().inEventLoop();
        try {
            if (!transitionOutputToActive()) {
                promise.setFailure(new IllegalStateException("Cannot set output state to ACTIVE. State: " + outputState));
                return;
            }
            outputReliability = Reliability.UNRELIABLE;
            promise.setSuccess(this);
        } catch (Throwable e) {
            outputState = OutputState.FAILED;
            promise.setFailure(e);
        }
    }

    // must be in event loop
    private void openReliableOutputStream(Promise<QChannelImpl> promise) {
        assert eventLoop().inEventLoop();
        try {
            reliableInputStreamHandler = new ReliableInputStreamHandler(this);
            Future<? extends Channel> future = streamGenerator.generateUnidirectionalStream(reliableInputStreamHandler);

            future.addListener(stream -> {
                if (!stream.isSuccess()) {
                    promise.setFailure(stream.cause());
                    return;
                }
                this.outputStream = (Channel) stream.getNow();
                if (!transitionOutputToActive()) {
                    promise.setFailure(new IllegalStateException("Cannot set output state to ACTIVE. State: " + outputState));
                    return;
                }
                outputReliability = Reliability.RELIABLE;
                promise.setSuccess(QChannelImpl.this);
            });
        } catch (Throwable e) {
            outputState = OutputState.FAILED;
            promise.setFailure(e);
        }
    }

    private ChannelHandler newReliableOutputStreamHandler() {
        return new WriteOnceOnActiveHandler(channelId());
    }

    private boolean transitionOutputToActive() {
        assert eventLoop().inEventLoop();

        if (outputState != OutputState.OPENING) {
            outputState = OutputState.FAILED;
            return false;
        }

        tryTransitionToActive();

        outputState = OutputState.ACTIVE;
        ChannelPipeline pipeline = pipeline();
        pipeline.fireUserEventTriggered(QChannelEvent.OUTPUT_ACTIVE);

        return true;
    }

    private boolean transitionOutputToClosed() {
        assert eventLoop().inEventLoop();
        if (outputState == OutputState.CLOSED) {
            return false;
        }

        tryTransitionToClosed();

        outputState = OutputState.CLOSED;
        ChannelPipeline pipeline = pipeline();
        pipeline.fireUserEventTriggered(QChannelEvent.OUTPUT_SHUTDOWN);
        return true;
    }

    private boolean transitionInputToActive() {
        assert eventLoop().inEventLoop();
        if (inputState != InputState.IDLE) {
            return false;
        }

        tryTransitionToActive();

        inputState = InputState.ACTIVE;
        ChannelPipeline pipeline = pipeline();
        pipeline.fireUserEventTriggered(QChannelEvent.INPUT_ACTIVE);
        return true;
    }

    private boolean transitionInputToClosed() {
        assert eventLoop().inEventLoop();
        if (inputState == InputState.CLOSED) {
            return false;
        }

        tryTransitionToClosed();

        inputState = InputState.CLOSED;

        if (inputStream != null) {
            inputStream.close();
            inputState = null;
        }

        ChannelPipeline pipeline = pipeline();
        pipeline.fireUserEventTriggered(QChannelEvent.INPUT_SHUTDOWN);

        return true;
    }


    private void tryTransitionToActive() {
        assert eventLoop().inEventLoop();
        if (state == ChannelState.OPEN) {
            state = ChannelState.ACTIVE;
            pipeline().fireChannelActive();
        }
    }

    private void tryTransitionToClosed() {
        assert eventLoop().inEventLoop();
        if (state == ChannelState.ACTIVE || state == ChannelState.OPEN) {
            state = ChannelState.CLOSED;
            pipeline().fireChannelInactive();
        }
    }


    @Override
    protected AbstractUnsafe newUnsafe() {
        return new QChannelUnsafe();
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return parent().eventLoop() == loop;
    }

    @Override
    protected SocketAddress localAddress0() {
        return parent().localAddress();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return parent().remoteAddress();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doDisconnect() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doClose() throws Exception {
        if (state == ChannelState.CLOSED)
            return;
        state = ChannelState.CLOSED;

        ChannelPipeline pipeline = pipeline();
        // CLose output
        pipeline.fireUserEventTriggered(QChannelEvent.OUTPUT_SHUTDOWN);
        outputState = OutputState.CLOSED;


        transitionOutputToClosed();
        transitionInputToClosed();

    }

    @Override
    protected void doBeginRead() throws Exception {
        // Check if the input stream is active, and whether it is datagram or otherwise
        if (inputState != InputState.ACTIVE)
            return;
        if (inputStream == null)
            return;
        inputStream.read();
    }


    public void connectInputStream(io.netty.channel.Channel inputStream) {
        assert eventLoop().inEventLoop();
        if (this.inputStream != null)
            throw new IllegalStateException("Input stream already connected");

        if (!transitionInputToActive())
            throw new IllegalStateException("Cannot set input state to ACTIVE. State: " + inputState);
        inputStream.pipeline().addLast(newReliableInputStreamHandler());
        this.inputStream = inputStream;
    }


    private ChannelHandler newReliableInputStreamHandler() {
        return new ReliableInputStreamHandler(this);
    }

    public void acceptDatagram(ByteBuf frame) {
        if (inputState != InputState.ACTIVE)
            transitionInputToActive();
        if (inputStream != null)
            throw new IllegalStateException("A reliable input stream is already connected");

        pipeline().fireChannelRead(frame);
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        switch (outputReliability) {
            case UNRELIABLE -> writeUnreliable(in);
            case RELIABLE -> writeReliable(in);
        }
    }

    private void writeUnreliable(ChannelOutboundBuffer in) {
        // Prepend the message with this channel id
        for (; ; ) {
            ByteBuf message = (ByteBuf) in.current();
            if (message == null)
                break;
            CompositeByteBuf buffer = alloc().compositeBuffer(2);
            buffer.addComponents(
                    true,
                    alloc().buffer(4).writeInt(channelId()),
                    message.retain()
            );
            in.remove();
            connection.getConnection().write(buffer);
        }
    }

    private void writeReliable(ChannelOutboundBuffer in) {
        for (; ; ) {
            ByteBuf message = (ByteBuf) in.current();
            if (message == null)
                break;
            in.remove();
            outputStream.write(message);
        }
        outputStream.flush();
    }

    public void fireInputStreamInactive() {
        assert eventLoop().inEventLoop();
        transitionInputToClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return inputState == InputState.CLOSED;
    }

    @Override
    public ChannelFuture shutdownInput() {
        ChannelPromise future = new DefaultChannelPromise(this, eventLoop());
        return shutdownInput(future);
    }

    @Override
    public ChannelFuture shutdownInput(ChannelPromise promise) {
        if (eventLoop().inEventLoop())
            shutdownInput0(promise);
        else
            eventLoop().execute(() -> shutdownInput0(promise));
        return promise;
    }

    private void shutdownInput0(ChannelPromise promise) {
        if (inputState == InputState.CLOSED)
            return;

        if (inputStream != null) {

            if (reliableInputStreamHandler != null) {
                try {
                    inputStream.pipeline().remove(reliableInputStreamHandler);
                } catch (Throwable _) {
                }
            }

            inputState = InputState.CLOSED;
            inputStream.close().addListener(_ -> {
                promise.setSuccess();
                transitionInputToClosed();
            });
            inputStream = null;
            reliableInputStreamHandler = null;
        } else {
            inputState = InputState.CLOSED;
            promise.setSuccess();
            transitionInputToClosed();
        }
    }

    @Override
    public boolean isOutputShutdown() {
        return outputState == OutputState.CLOSED;
    }

    @Override
    public ChannelFuture shutdownOutput() {
        ChannelPromise future = new DefaultChannelPromise(this, eventLoop());
        return shutdownOutput(future);
    }

    @Override
    public ChannelFuture shutdownOutput(ChannelPromise promise) {
        if (eventLoop().inEventLoop())
            shutdownOutput0(promise);
        else
            eventLoop().execute(() -> shutdownOutput0(promise));
        return promise;
    }

    private void shutdownOutput0(ChannelPromise promise) {
        if (outputState == OutputState.CLOSED)
            return;
        outputState = OutputState.CLOSED;
        promise.setSuccess();
        tryTransitionToClosed();
    }

    @Override
    public boolean isShutdown() {
        return state == ChannelState.CLOSED;
    }

    @Override
    public ChannelFuture shutdown() {
        ChannelPromise promise = new DefaultChannelPromise(this, eventLoop());
        return shutdown(promise);
    }

    @Override
    public ChannelFuture shutdown(ChannelPromise promise) {
        PromiseCombiner promiseCombiner = new PromiseCombiner(eventLoop());
        promiseCombiner.add(shutdownInput());
        promiseCombiner.add(shutdownOutput());
        promiseCombiner.finish(promise);
        return promise;
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return state != ChannelState.CLOSED;
    }

    @Override
    public boolean isActive() {
        return state == ChannelState.ACTIVE;
    }

    @Override
    public ChannelMetadata metadata() {
        return metadata;
    }


    class QChannelUnsafe extends AbstractUnsafe {

        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            throw new UnsupportedOperationException();
        }
    }


    public void setChannelPriority(int priority) {
        this.priority = priority;
        if (outputStream != null)
            priorityUpdater.updatePriority(priority, outputStream);
    }

    public int getChannelPriority() {
        return priority;
    }

}

/*

    private static final String CHANNEL_HANDLER = "channelHandler";

    private final Connection<?, ?> connection;
    private final Enum<?> channelId;
    private int priority; // TODO: Learn default priority value

    private final EventLoop eventLoop;

    private volatile io.netty.channel.Channel outChannel, inChannel;

    private volatile ChannelHandler channelHandler;

    private final Object lock = new Object();


    public QChannelImpl(Connection<?, ?> connection, Enum<?> channelId, EventLoopGroup eventLoopGroup) {
        this.connection = connection;
        this.channelId = channelId;
        this.eventLoop = eventLoopGroup.next();
    }


    public Enum<?> getChannelId() {
        return channelId;
    }

    public void connectInputStream(io.netty.channel.Channel inputStream) {
        synchronized (lock) {
            assert this.inChannel == null;
            this.inChannel = inputStream;
            inChannel.pipeline().addLast(channelHandler);
        }
        QuicStreamChannel
    }

    public void acceptDatagram(ByteBuf frame) {
        if (inChannel == null) {
            synchronized (lock) {
                if (inChannel == null) {
                    ChannelHandler[] handlers;
                    if (channelHandler != null)
                        handlers = new ChannelHandler[]{channelHandler};
                    else
                        handlers = new ChannelHandler[0];

                    this.inChannel = new EmbeddedChannel(
                            connection.getConnection(),
                            DefaultChannelId.newInstance(),
                            true,
                            false,
                            handlers
                    );
                }
            }
        }
        ((EmbeddedChannel)inChannel).writeOneInbound(frame);
    }

    public io.netty.channel.Channel getOutChannel() {
        return outChannel;
    }

    public io.netty.channel.Channel getInChannel() {
        return inChannel;
    }


    @Override
    public Promise<QChannel> close() {
        synchronized (lock) {
            if (outChannel == null && inChannel == null) {
                return connection.getConnection().eventLoop().<QChannel>newPromise().setSuccess(this);
            }

            EventLoop eventLoop = connection.getConnection().eventLoop();

            Promise<QChannel> promise = eventLoop.newPromise();

            if (eventLoop.inEventLoop()) {
                close0(eventLoop).addListener(future -> promise.setSuccess(this));
            } else {
                eventLoop.execute(() -> close0(eventLoop).addListener(future -> promise.setSuccess(this)));
            }
            return promise;
        }
    }

    private Promise<Void> close0(EventLoop eventLoop) {
        PromiseCombiner promiseCombiner = new PromiseCombiner(eventLoop);
        Promise<Void> promise = eventLoop.newPromise();
        if (outChannel != null) {
            promiseCombiner.add(outChannel.close());
        }
        if (inChannel != null) {
            promiseCombiner.add(inChannel.close());
        }
        promiseCombiner.finish(promise);
        return promise;
    }


    @Override
    public Future<? extends io.netty.channel.Channel> openOutputStream(QChannelImpl.Reliability reliability) throws IllegalStateException {
        if (outChannel != null) {
            throw new IllegalStateException("Output stream already open");
        }
        if (reliability == null) {
            throw new IllegalArgumentException("Reliability cannot be null");
        }
        if (reliability == Reliability.RELIABLE) {
            return connection.getConnection().createStream(QuicStreamType.UNIDIRECTIONAL, new ChannelInboundHandlerAdapter() {
                @Override
                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                    // write the channel id to the stream

                    ByteBuf buffer = Unpooled.directBuffer(4);
                    buffer.writeInt(channelId.ordinal());
                    ctx.channel().writeAndFlush(buffer);

                    synchronized (lock) {
                        outChannel = ctx.channel();
                        if (channelHandler != null) {
                            outChannel.pipeline().addLast(CHANNEL_HANDLER, channelHandler);
                        }
                    }
                }
            });
        } else {
            synchronized (lock) {

                Promise<UnreliableDatagramChannel> promise = eventLoop.newPromise();
                outChannel = new UnreliableDatagramChannel(connection.getConnection(), channelId.ordinal());
                eventLoop.register(outChannel)
                        .addListener((ChannelFuture f) -> {
                            if (f.isSuccess()) {
                                promise.setSuccess((UnreliableDatagramChannel) outChannel);
                            } else {
                                promise.setFailure(f.cause());
                            }
                        });
                if (channelHandler != null)
                    outChannel.pipeline().addLast(CHANNEL_HANDLER, channelHandler);

                return promise;

            }
        }
    }

    @Override
    public void pipeline().addLast(ChannelHandler channelHandler) {
        synchronized (lock) {
            if (this.channelHandler != null) {
                throw new IllegalStateException("Channel handler already set");
            }
            if (channelHandler == null) {
                throw new IllegalArgumentException("Channel handler cannot be null");
            }

            this.channelHandler = channelHandler;
            if (inChannel != null) {
                inChannel.pipeline().addLast(CHANNEL_HANDLER, channelHandler);
            }
            if (outChannel != null) {
                outChannel.pipeline().addLast(CHANNEL_HANDLER, channelHandler);
            }
        }
    }
 */