package com.hirshi001.quicnetworking.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ByteBufferUtil {


    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public ByteBufferUtil() {
    }

    public static void writeStringToBuf(Charset charset, String msg, ByteBuf buf) {
        byte[] bytes = msg.getBytes(charset);
        int size = bytes.length;
        buf.ensureWritable(size + computeVarIntSize(size));
        writeVarInt(buf, size);
        buf.writeBytes(bytes);

    }

    public static void writeStringToBuf(String msg, ByteBuf buf) {
        writeStringToBuf(DEFAULT_CHARSET, msg, buf);
    }

    public static String readStringFromBuf(Charset charset, ByteBuf buf) {
        int size = readVarInt(buf);
        byte[] bytes = new byte[size];
        buf.readBytes(bytes);
        return new String(bytes, charset);
    }

    public static String readStringFromBuf(ByteBuf buf) {
        return readStringFromBuf(DEFAULT_CHARSET, buf);
    }

    public static void writeVarInt(ByteBuf out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            } else {
                out.writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public static int computeVarIntSize(final int value) {
        if ((value & (0xffffffff << 7)) == 0) {
            return 1;
        }
        if ((value & (0xffffffff << 14)) == 0) {
            return 2;
        }
        if ((value & (0xffffffff << 21)) == 0) {
            return 3;
        }
        if ((value & (0xffffffff << 28)) == 0) {
            return 4;
        }
        return 5;
    }

    public static int readVarInt(ByteBuf buffer) {

        if (!buffer.isReadable()) {
            return 0;
        }
        buffer.markReaderIndex();
        byte tmp = buffer.readByte();
        if (tmp >= 0) {
            return tmp;
        } else {
            int result = tmp & 127;
            if (!buffer.isReadable()) {
                buffer.resetReaderIndex();
                return 0;
            }
            if ((tmp = buffer.readByte()) >= 0) {
                result |= tmp << 7;
            } else {
                result |= (tmp & 127) << 7;
                if (!buffer.isReadable()) {
                    buffer.resetReaderIndex();
                    return 0;
                }
                if ((tmp = buffer.readByte()) >= 0) {
                    result |= tmp << 14;
                } else {
                    result |= (tmp & 127) << 14;
                    if (!buffer.isReadable()) {
                        buffer.resetReaderIndex();
                        return 0;
                    }
                    if ((tmp = buffer.readByte()) >= 0) {
                        result |= tmp << 21;
                    } else {
                        result |= (tmp & 127) << 21;
                        if (!buffer.isReadable()) {
                            buffer.resetReaderIndex();
                            return 0;
                        }
                        result |= (tmp = buffer.readByte()) << 28;
                        if (tmp < 0) {
                            throw new CorruptedFrameException("malformed varint.");
                        }
                    }
                }
            }
            return result;
        }
    }

}
