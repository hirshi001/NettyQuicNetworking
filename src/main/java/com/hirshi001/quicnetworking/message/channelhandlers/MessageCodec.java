package com.hirshi001.quicnetworking.message.channelhandlers;

import com.hirshi001.quicnetworking.message.Message;
import com.hirshi001.quicnetworking.message.MessageHolder;
import com.hirshi001.quicnetworking.message.messageregistry.MessageRegistry;
import com.hirshi001.quicnetworking.util.ByteBufferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class MessageCodec extends ByteToMessageCodec<Message> {

    private final MessageRegistry messageRegistry;

    public MessageCodec(MessageRegistry messageRegistry) {
        this.messageRegistry = messageRegistry;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {

        while (true) {
            in.markReaderIndex();
            if (in.readableBytes() < 4) {
                return;
            }
            int msgId = in.readInt();

            MessageHolder<?> holder = messageRegistry.getMessageHolder(msgId);
            if (holder == null) {
                ctx.channel().close();
                throw new IllegalArgumentException("Unknown message id: " + msgId);
            }


            Message msg = holder.getMessage();

            if (messageRegistry.sizeCheck()) {
                int size = ByteBufferUtil.readVarInt(in);
                if(in.readableBytes() < size){
                    in.resetReaderIndex();
                    return;
                }
                ByteBuf data = in.readSlice(size);
                msg.readBytes(data);
            } else {
                try {
                    msg.readBytes(in);
                } catch (Exception e) {
                    in.resetReaderIndex();
                    return;
                }
            }


            out.add(msg);
        }

    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        int msgId = messageRegistry.getId(msg.getClass());

        out.writeInt(msgId);
        if(messageRegistry.sizeCheck()) {
            out.ensureWritable(4);
            int writerIndex = out.writerIndex();
            out.writerIndex(writerIndex + 4);

            msg.writeBytes(out);

            // write the size of the message
            int size = out.writerIndex() - writerIndex - 4;
            out.markWriterIndex();
            out.writerIndex(writerIndex);
            ByteBufferUtil.writeVarInt(out, size);
            out.resetWriterIndex();
        }else {
            msg.writeBytes(out);
        }
    }
}
