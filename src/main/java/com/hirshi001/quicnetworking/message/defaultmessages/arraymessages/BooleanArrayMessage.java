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
import com.hirshi001.quicnetworking.util.BooleanCompression;
import com.hirshi001.quicnetworking.util.ByteBufferUtil;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

/**
 * A message that contains an array of booleans.
 *
 * @author Hrishikesh Ingle
 */
@SuppressWarnings("unused")
public class BooleanArrayMessage extends Message {

    public boolean[] array;

    /**
     * Creates a new BooleanArrayMessage without instantiating the array.
     */
    public BooleanArrayMessage() {
        super();
    }

    /**
     * Creates a new BooleanArrayMessage with a reference to the array argument.
     *
     * @param array the array to reference
     */
    public BooleanArrayMessage(boolean[] array) {
        super();
        this.array = array;
    }


    @Override
    public void writeBytes(ByteBuf out) {
        super.writeBytes(out);
        byte[] compression = BooleanCompression.compressBooleanArray(array);
        ByteBufferUtil.writeVarInt(out, array.length);
        out.writeBytes(compression);
    }

    @Override
    public void readBytes(ByteBuf in) {
        super.readBytes(in);
        int length = ByteBufferUtil.readVarInt(in);
        if (length == 0) {
            array = new boolean[0];
            return;
        }
        byte[] compression = new byte[(length - 1) / 8 + 1];
        in.readBytes(compression);
        array = BooleanCompression.decompressBooleans(compression, length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof BooleanArrayMessage message)) return false;
        return Arrays.equals(array, message.array);
    }

    @Override
    public String toString() {
        return ArrayUtil.toString(this, array);
    }
}
