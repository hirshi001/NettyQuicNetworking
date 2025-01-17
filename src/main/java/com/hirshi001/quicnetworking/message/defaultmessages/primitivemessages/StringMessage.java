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
import com.hirshi001.quicnetworking.util.ByteBufferUtil;
import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * A message that contains a string.
 *
 * @author Hrishikesh Ingle
 */
public class StringMessage extends Message {

    public String value;

    /**
     * Creates a new StringMessage with the value set to null.
     */
    public StringMessage() {

    }

    /**
     * Creates a new StringMessage with the value set to the given value.
     *
     * @param value
     */
    public StringMessage(String value) {
        this.value = value;
    }

    @Override
    public void writeBytes(ByteBuf out) {
        super.writeBytes(out);
        ByteBufferUtil.writeStringToBuf(value, out);
    }

    @Override
    public void readBytes(ByteBuf in) {
        super.readBytes(in);
        value = ByteBufferUtil.readStringFromBuf(in);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof StringMessage)) return false;
        StringMessage message = (StringMessage) obj;
        return Objects.equals(message.value, value);
    }

    @Override
    public String toString() {
        return PrimitiveUtil.toString(this, value);
    }
}
