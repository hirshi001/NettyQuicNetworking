package com.hirshi001.quicnetworking.message.channelhandlers;

import com.hirshi001.quicnetworking.message.Message;
import com.hirshi001.quicnetworking.message.MessageHolder;
import com.hirshi001.quicnetworking.message.messageregistry.MessageRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {

    private final MessageRegistry messageRegistry;

    public MessageCodec(MessageRegistry messageRegistry) {
        this.messageRegistry = messageRegistry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {

        while (in.readableBytes() > 0) {
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

            int bytesForSize = messageRegistry.getBytesForSize();
            if (bytesForSize > 0) {
                if (in.readableBytes() < bytesForSize) {
                    in.resetReaderIndex();
                    return;
                }

                int size = switch (bytesForSize) {
                    case 1 -> in.readUnsignedByte();
                    case 2 -> in.readUnsignedShort();
                    case 4 -> in.readInt();
                    default -> throw new IllegalStateException("Invalid bytes for size: " + bytesForSize);
                };

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

    public void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        int msgId = messageRegistry.getId(msg.getClass());

        out.writeInt(msgId);


        int bytesForSize = messageRegistry.getBytesForSize();
        if(bytesForSize > 0) {
            int startIndex = out.writerIndex();

            out.ensureWritable(bytesForSize);
            out.writerIndex(startIndex + bytesForSize);

            msg.writeBytes(out);

            // write the size of the message
            int size = out.writerIndex() - startIndex - bytesForSize;


            out.markWriterIndex();
            out.writerIndex(startIndex);
            switch(bytesForSize) {
                case 1:
                    out.writeByte(size);
                    break;
                case 2:
                    out.writeShort(size);
                    break;
                case 4:
                    out.writeInt(size);
                    break;
                default:
                    // should never happen
                    throw new IllegalStateException("Invalid bytes for size: " + bytesForSize);
            }
            out.resetWriterIndex();
        }else {
            msg.writeBytes(out);
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        encode(ctx, msg, buf);
        out.add(buf);
    }
}
