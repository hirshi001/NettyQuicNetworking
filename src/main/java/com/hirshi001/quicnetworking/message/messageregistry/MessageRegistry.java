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

package com.hirshi001.quicnetworking.message.messageregistry;

import com.hirshi001.quicnetworking.message.Message;
import com.hirshi001.quicnetworking.message.MessageHandler;
import com.hirshi001.quicnetworking.message.MessageHolder;

import java.util.function.Supplier;

/**
 * A registry that holds message types used in a networking system.
 * Using the register"Type"Packet methods will register messages of that Type to a default negative
 * id.
 *
 * @author Hrishikesh Ingle
 */
@SuppressWarnings("unused")
public interface MessageRegistry {


    /**
     * Registers a message with the given id.
     *
     * @param supplier     the supplier to create the message
     * @param messageClass the class of the message
     * @param id           the id to register the message with
     * @param <T>          the type of the message
     * @return this for chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    default <T extends Message> MessageRegistry register(Supplier<T> supplier, Class<T> messageClass, int id) {
        return register(new MessageHolder<>(supplier, null, messageClass), id);
    }

    /**
     * Registers a message with the given id.
     *
     * @param supplier     the supplier to create the message
     * @param handler      the handler to handle the message, null for no handler
     * @param messageClass the class of the message
     * @param id           the id to register the message with
     * @param <T>          the type of the message
     * @return this for chaining
     */
    @SuppressWarnings("UnusedReturnValue")
    default <T extends Message> MessageRegistry register(Supplier<T> supplier, MessageHandler<T> handler, Class<T> messageClass, int id) {
        return register(new MessageHolder<>(supplier, handler, messageClass), id);
    }

    /**
     * Sets the number of bytes to use for the size of the message. Can be 0, 1, 2, or 4.
     * @param bytesForSize the number of bytes to use for the size of the message.
     */
    void setBytesForSize(int bytesForSize);

    /**
     * Gets the number of bytes to use for the size of the message.
     * Gaurenteed to be 0, 1, 2, or 4.
     * @return the number of bytes to use for the size of the message.
     */
    int getBytesForSize();


    /**
     * Registers a message with the given id.
     *
     * @param holder the message holder to register
     * @param id     the id to register the message with
     * @return this
     */
    MessageRegistry register(MessageHolder<?> holder, int id);

    /**
     * Gets the message holder for the given message.
     *
     * @param message the message to get the message holder for
     * @param <T>     the type of the message
     * @return the message holder for the given message
     */
    @SuppressWarnings("unchecked")
    default <T extends Message> MessageHolder<T> getMessageHolder(T message) {
        return (MessageHolder<T>) getMessageHolder(message.getClass());
    }

    /**
     * Gets the message holder for the given class.
     *
     * @param clazz the class to get the message holder for
     * @param <T>   the type of the message
     * @return the message holder for the given class
     */
    @SuppressWarnings("unchecked")
    default <T extends Message> MessageHolder<T> getMessageHolder(Class<T> clazz) {
        return (MessageHolder<T>) getMessageHolder(getId(clazz));
    }

    /**
     * Gets the message holder for the given id.
     *
     * @param id the id to get the message holder for
     * @return the message holder for the given id
     */
    MessageHolder<?> getMessageHolder(int id);

    /**
     * Gets the id for the given message holder.
     *
     * @param holder the message holder to get the id for
     * @return the id for the given message holder
     */
    int getId(MessageHolder<?> holder);

    /**
     * Gets the id for the given message class.
     *
     * @param clazz the message class to get the id for
     * @return the id for the given message class
     */
    int getId(Class<? extends Message> clazz);


}
