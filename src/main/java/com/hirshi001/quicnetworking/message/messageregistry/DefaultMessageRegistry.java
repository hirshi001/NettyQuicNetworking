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
import com.hirshi001.quicnetworking.message.MessageHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation of MessageRegistry
 *
 * @author Hrishikesh Ingle
 */
public class DefaultMessageRegistry implements MessageRegistry {

    protected final Map<Class<? extends Message>, Integer> classIdMap;
    protected final Map<Integer, MessageHolder<?>> intToMessageHolderMap;
    protected final Map<MessageHolder<?>, Integer> messageHolderIntMap;


    private boolean sizeCheck = true;

    /**
     * Creates a new DefaultMessageRegistry with the given name.
     */
    public DefaultMessageRegistry() {
        this.classIdMap = new HashMap<>();
        this.intToMessageHolderMap = new HashMap<>();
        this.messageHolderIntMap = new HashMap<>();
    }

    @Override
    public void sizeCheck(boolean check) {
        this.sizeCheck = check;
    }

    @Override
    public boolean sizeCheck() {
        return sizeCheck;
    }

    @Override
    public final MessageRegistry register(MessageHolder<?> messageHolder, int id) {
        if (getMessageHolder(id) != null) {
            messageHolderIntMap.values().remove(id);
            classIdMap.values().remove(id);
            intToMessageHolderMap.remove(id);
        }
        classIdMap.put(messageHolder.messageClass, id);
        intToMessageHolderMap.put(id, messageHolder);
        messageHolderIntMap.put(messageHolder, id);
        return this;
    }

    @Override
    public final MessageHolder<?> getMessageHolder(int id) {
        return intToMessageHolderMap.get(id);
    }

    @Override
    public int getId(MessageHolder<?> holder) {
        return messageHolderIntMap.get(holder);
    }

    @Override
    public int getId(Class<? extends Message> clazz) {
        return classIdMap.get(clazz);
    }

}
