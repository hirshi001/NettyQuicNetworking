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

/**
 * A utility class for the primitive messages.
 *
 * @author Hrishikesh Ingle
 */
public class PrimitiveUtil {

    /**
     * Returns a string representation of the message.
     *
     * @param object the message
     * @param data   the value of the message
     * @return a string representation of the message
     */
    public static String toString(Object object, Object data) {
        return object.getClass().getName() + "{data=" + data + "}";
    }

}
