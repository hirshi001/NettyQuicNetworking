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
 * A message that contains a short.
 *
 * @author Hrishikesh Ingle
 */
public class ShortMessage extends Message {

    public short value;

    /**
     * Creates a new ShortMessage with the value set to 0.
     */
    public ShortMessage() {
        super();
    }

    /**
     * Creates a new ShortMessage with the value set to the given value.
     *
     * @param value the value to set the message to.
     */
    public ShortMessage(short value) {
        super();
        this.value = value;
    }

    @Override
    public void writeBytes(ByteBuf out) {
        super.writeBytes(out);
        out.writeShort(value);
    }

    @Override
    public void readBytes(ByteBuf in) {
        super.readBytes(in);
        value = in.readShort();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ShortMessage)) return false;
        ShortMessage message = (ShortMessage) obj;
        return message.value == value;
    }

    @Override
    public String toString() {
        return PrimitiveUtil.toString(this, value);
    }
}
