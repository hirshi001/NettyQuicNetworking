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

import io.netty.channel.ChannelHandlerContext;

/**
 * Represents a handler for a message.
 *
 * @param <T> the type of the message to handle
 * @author Hrishikesh Ingle
 */
@FunctionalInterface
public interface MessageHandler<T extends Message> {

    MessageHandler<?> NO_HANDLE = (MessageHandler<Message>) (context, msg) -> {
    };

    @SuppressWarnings("unchecked")
    static <A extends Message> MessageHandler<A> noHandle() {
        return (MessageHandler<A>) NO_HANDLE;
    }

    void handle(ChannelHandlerContext context, T message);

}
