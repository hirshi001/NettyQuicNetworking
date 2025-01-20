package com.hirshi001.tests.messagecodectests;

import com.hirshi001.quicnetworking.message.channelhandlers.MessageCodec;
import com.hirshi001.quicnetworking.message.defaultmessages.arraymessages.IntegerArrayMessage;
import com.hirshi001.quicnetworking.message.defaultmessages.primitivemessages.IntegerMessage;
import com.hirshi001.quicnetworking.message.defaultmessages.primitivemessages.StringMessage;
import com.hirshi001.quicnetworking.message.messageregistry.DefaultMessageRegistry;
import com.hirshi001.quicnetworking.message.messageregistry.MessageRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodecTest {

    @Test
    public void testStringMessage() throws Exception {
        MessageRegistry registry = new DefaultMessageRegistry();

        registry.register(StringMessage::new, StringMessage.class, 0);

        MessageCodec codec = new MessageCodec(registry);

        String string = "Hello World!";
        StringMessage message = new StringMessage(string);

        ByteBuf buf = Unpooled.buffer();
        codec.encode(null, message, buf);

        List<Object> out = new ArrayList<>();
        codec.decode(null, buf, out);

        StringMessage decoded = (StringMessage) out.get(0);
        assertEquals(string, decoded.value);
    }
    @Test
    public void testMultipleMessage0ByteSize() throws Exception {
        testMultipleMessages(0);
    }

    @Test
    public void testMultipleMessage1ByteSize() throws Exception {
        testMultipleMessages(1);
    }

    @Test
    public void testMultipleMessage2ByteSize() throws Exception {
        testMultipleMessages(2);
    }

    @Test
    public void testMultipleMessage4ByteSize() throws Exception {
        testMultipleMessages(4);
    }

    @Test
    public void invalidByteSize() {
        MessageRegistry registry = new DefaultMessageRegistry();
        assertThrows(IllegalArgumentException.class, () -> registry.setBytesForSize(3));
        assertThrows(IllegalArgumentException.class, () -> registry.setBytesForSize(-1));
        assertThrows(IllegalArgumentException.class, () -> registry.setBytesForSize(5));
    }

    private void testMultipleMessages(int bytesForSize) throws Exception {

        MessageRegistry registry = new DefaultMessageRegistry();
        MessageCodec codec = new MessageCodec(registry);
        registry.setBytesForSize(bytesForSize);

        // Register messages
        registry.register(StringMessage::new, StringMessage.class, 0);
        registry.register(IntegerArrayMessage::new, IntegerArrayMessage.class, 1);
        registry.register(IntegerMessage::new, IntegerMessage.class, 2);


        // Prepare messages
        String string = "Hello World!";
        StringMessage stringMessage = new StringMessage(string);

        int[] array = new int[]{1, 2, 3, 4, 5};
        IntegerArrayMessage arrayMessage = new IntegerArrayMessage(array);

        int integer = 123;
        IntegerMessage integerMessage = new IntegerMessage(integer);

        // Encode messages
        ByteBuf buf = Unpooled.buffer();
        codec.encode(null, stringMessage, buf);
        codec.encode(null, arrayMessage, buf);
        codec.encode(null, integerMessage, buf);

        // Decode messages
        List<Object> out = new ArrayList<>();
        codec.decode(null, buf, out);

        assertInstanceOf(StringMessage.class, out.get(0));
        assertInstanceOf(IntegerArrayMessage.class, out.get(1));
        assertInstanceOf(IntegerMessage.class, out.get(2));

        StringMessage decodedString = (StringMessage) out.get(0);
        IntegerArrayMessage decodedArray = (IntegerArrayMessage) out.get(1);
        IntegerMessage decodedInteger = (IntegerMessage) out.get(2);

        // Check if messages are correct
        assertEquals(string, decodedString.value);
        assertArrayEquals(array, decodedArray.array);
        assertEquals(integer, decodedInteger.value);

        buf.release();
    }

}
