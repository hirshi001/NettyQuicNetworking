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

package com.hirshi001.quicnetworking.message.defaultmessages.primitivemessages;

import com.hirshi001.quicnetworking.message.Message;
import io.netty.buffer.ByteBuf;

/**
 * A message that contains a long.
 *
 * @author Hrishikesh Ingle
 */
public class LongMessage extends Message {

    long value;

    /**
     * Creates a new LongMessage with the value set to 0.
     */
    public LongMessage() {

    }

    /**
     * Creates a new LongMessage with the value set to the argument.
     *
     * @param value the value to set
     */
    public LongMessage(long value) {

        this.value = value;
    }

    @Override
    public void writeBytes(ByteBuf out) {
        super.writeBytes(out);
        out.writeLong(value);
    }

    @Override
    public void readBytes(ByteBuf in) {
        super.readBytes(in);
        value = in.readLong();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof LongMessage)) return false;
        LongMessage message = (LongMessage) obj;
        return message.value == value;
    }

    @Override
    public String toString() {
        return PrimitiveUtil.toString(this, value);
    }
}
