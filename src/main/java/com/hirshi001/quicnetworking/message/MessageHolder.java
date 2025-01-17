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

package com.hirshi001.quicnetworking.message;

import java.util.function.Supplier;

/**
 * Holds information about a registered message type.
 *
 * @param <T> the type of the message
 * @author Hrishikesh Ingle
 */
public class MessageHolder<T extends Message> {

    public MessageHandler<T> handler;
    public Class<T> messageClass;
    public Supplier<T> supplier;

    public MessageHolder(Supplier<T> supplier, MessageHandler<T> handler, Class<T> messageClass) {
        this.supplier = supplier;
        if (handler == null) this.handler = MessageHandler.noHandle();
        else this.handler = handler;
        this.messageClass = messageClass;
    }

    public T getMessage() {
        return supplier.get();
    }


}
