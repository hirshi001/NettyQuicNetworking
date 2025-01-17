/*
 * Copyright 2023 Hrishikesh Ingle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hirshi001.quicnetworking.message.defaultmessages.arraymessages;


import com.hirshi001.quicnetworking.message.Message;
import com.hirshi001.quicnetworking.util.ByteBufferUtil;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

/**
 * A message that contains an array of shorts.
 *
 * @author Hrishikesh Ingle
 */
@SuppressWarnings("unused")
public class ShortArrayMessage extends Message {

    public short[] array;

    /**
     * Creates a new ShortArrayMessage without instantiating the array.
     */
    public ShortArrayMessage() {
        super();
    }

    /**
     * Creates a new ShortArrayMessage with a reference to the array argument.
     *
     * @param array the array to reference
     */
    public ShortArrayMessage(short[] array) {
        super();
        this.array = array;
    }

    @Override
    public void readBytes(ByteBuf buf) {
        super.readBytes(buf);
        array = new short[ByteBufferUtil.readVarInt(buf)];
        for (int i = 0; i < array.length; i++) {
            array[i] = buf.readShort();
        }
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        super.writeBytes(buf);
        ByteBufferUtil.writeVarInt(buf, array.length);
        for (short s : array) {
            buf.writeShort(s);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof ShortArrayMessage message)) return false;
        return Arrays.equals(array, message.array);
    }

    @Override
    public String toString() {
        return ArrayUtil.toString(this, array);
    }

}
