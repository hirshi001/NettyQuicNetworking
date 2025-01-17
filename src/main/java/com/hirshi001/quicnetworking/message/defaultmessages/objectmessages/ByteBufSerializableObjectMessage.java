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

package com.hirshi001.quicnetworking.message.defaultmessages.objectmessages;

import com.hirshi001.quicnetworking.message.ByteBufSerializable;
import com.hirshi001.quicnetworking.message.Message;
import io.netty.buffer.ByteBuf;

/**
 * Any class which overrides {@link ByteBufSerializable} should implement the
 * {@link ByteBufSerializableObjectMessage#supply()} method to create a new object each time it is called (similar to a
 * factory method).
 * <br>
 * For example
 * <pre>
 * {@code
 *  @Override
 *  public MyObject get() {
 *      return new MyObject();
 *  }
 * }
 * </pre>
 * or
 * <pre>
 *  {@code
 *  static Supplier<MyObject> myObjectSupplier = () -> new MyObject(); // or MyObject::new or any other supplier
 *
 *  @Override
 *  public MyObject get() {
 *      return myObjectSupplier.get();
 *  }
 *  }
 *  </pre>
 *
 * @param <T> the type of object to be serialized
 * @author Hrishikesh Ingle
 */
@SuppressWarnings("unused")
public abstract class ByteBufSerializableObjectMessage<T extends ByteBufSerializable> extends Message {

    private T object;

    /**
     * Creates a new ByteBufSerializableObjectMessage with the object set to null.
     */
    public ByteBufSerializableObjectMessage() {
        super();
    }


    /**
     * Creates a new ByteBufSerializableObjectMessage with the object intended to be sent as an
     * argument.
     *
     * @param object the object intended to be sent
     */
    public ByteBufSerializableObjectMessage(T object) {
        super();
        this.object = object;
    }


    @Override
    public final void writeBytes(ByteBuf out) {
        super.writeBytes(out);
        object.writeBytes(out);
    }

    @Override
    public final void readBytes(ByteBuf in) {
        super.readBytes(in);
        object = supply();
        object.readBytes(in);
    }

    public final T getObject() {
        return object;
    }

    /**
     * Creates a new object of type T.
     *
     * @return a new object of type T
     */
    protected abstract T supply();

    /**
     * Sets the object of this message to the argument.
     *
     * @param object the object to set
     */
    public final void set(T object) {
        this.object = object;
    }
}
