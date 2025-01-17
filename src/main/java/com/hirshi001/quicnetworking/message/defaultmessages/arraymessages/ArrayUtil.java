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

import java.lang.reflect.Array;

/**
 * A utility class that contains methods for working with arrays, mainly used for array messages but
 * can be used for other things.
 *
 * @author Hrishikesh Ingle
 */
public class ArrayUtil {

    public static String toString(Object object, Object array) {
        String objectString = object == null ? "null" : object.getClass().getName();
        String arrayString = array == null ? "null" : arrayToString(array);
        return objectString + " : " + arrayString;
    }

    private static String arrayToString(Object array) {
        int size = Array.getLength(array);
        StringBuilder builder = new StringBuilder();
        builder.append(array.getClass().getName()).append("{");

        for (int i = 0; i < size - 1; i++) {
            builder.append(Array.get(array, i));
            builder.append(", ");
        }
        if (size > 0) {
            builder.append(Array.get(array, size - 1));
        }
        builder.append("}");
        return builder.toString();
    }


}
