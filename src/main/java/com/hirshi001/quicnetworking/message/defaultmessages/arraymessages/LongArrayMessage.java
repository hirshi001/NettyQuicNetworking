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
 * A message that contains an array of longs.
 *
 * @author Hrishikesh Ingle
 */
@SuppressWarnings("unused")
public class LongArrayMessage extends Message {

    public long[] array;

    /**
     * Creates a new LongArrayMessage without instantiating the array.
     */
    public LongArrayMessage() {
        super();
    }

    /**
     * Creates a new LongArrayMessage with a reference to the array argument.
     *
     * @param array the array to reference
     */
    public LongArrayMessage(long[] array) {
        super();
        this.array = array;
    }


    @Override
    public void readBytes(ByteBuf buf) {
        super.readBytes(buf);
        array = new long[ByteBufferUtil.readVarInt(buf)];
        for (int i = 0; i < array.length; i++) {
            array[i] = buf.readLong();
        }
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        super.writeBytes(buf);
        ByteBufferUtil.writeVarInt(buf, array.length);
        for (long l : array) {
            buf.writeLong(l);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof LongArrayMessage message)) return false;
        return Arrays.equals(array, message.array);
    }

    @Override
    public String toString() {
        return ArrayUtil.toString(this, array);
    }

}
