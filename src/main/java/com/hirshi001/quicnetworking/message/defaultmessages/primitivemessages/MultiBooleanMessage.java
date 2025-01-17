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


import com.hirshi001.quicnetworking.util.BooleanCompression;

/**
 * A class that represents a message that contains multiple booleans.
 * The booleans are stored in a byte.
 *
 * @author Hrishikesh Ingle
 */
public class MultiBooleanMessage extends ByteMessage {

    /**
     * Creates a new MultiBooleanMessage with the value set to 0.
     */
    public MultiBooleanMessage() {
        super();
    }

    /**
     * Creates a new MultiBooleanMessage with the value set to the argument.
     *
     * @param value the value to set
     */
    public MultiBooleanMessage(byte value) {
        super(value);
    }

    public MultiBooleanMessage(boolean... value) {
        this.value = BooleanCompression.compressBooleans(value);
    }

    //Create 8 constructors, one for each bit in the byte.
    public MultiBooleanMessage(boolean bit0) {
        this.value = BooleanCompression.compressBooleans(bit0);
    }

    public MultiBooleanMessage(boolean bit0, boolean bit1) {
        this.value = BooleanCompression.compressBooleans(bit0, bit1);
    }

    public MultiBooleanMessage(boolean bit0, boolean bit1, boolean bit2) {
        this.value = BooleanCompression.compressBooleans(bit0, bit1, bit2);
    }

    public MultiBooleanMessage(boolean bit0, boolean bit1, boolean bit2, boolean bit3) {
        this.value = BooleanCompression.compressBooleans(bit0, bit1, bit2, bit3);
    }

    public MultiBooleanMessage(boolean bit0, boolean bit1, boolean bit2, boolean bit3, boolean bit4) {
        this.value = BooleanCompression.compressBooleans(bit0, bit1, bit2, bit3, bit4);
    }

    public MultiBooleanMessage(boolean bit0, boolean bit1, boolean bit2, boolean bit3, boolean bit4, boolean bit5) {
        this.value = BooleanCompression.compressBooleans(bit0, bit1, bit2, bit3, bit4, bit5);
    }

    public MultiBooleanMessage(boolean bit0, boolean bit1, boolean bit2, boolean bit3, boolean bit4, boolean bit5, boolean bit6) {
        this.value = BooleanCompression.compressBooleans(bit0, bit1, bit2, bit3, bit4, bit5, bit6);
    }

    public MultiBooleanMessage(boolean bit0, boolean bit1, boolean bit2, boolean bit3, boolean bit4, boolean bit5, boolean bit6, boolean bit7) {
        this.value = BooleanCompression.compressBooleans(bit0, bit1, bit2, bit3, bit4, bit5, bit6, bit7);
    }

    //getters
    public boolean getBit0() {
        return BooleanCompression.getBoolean(value, 0);
    }

    //setters
    public void setBit0(boolean bit0) {
        value = BooleanCompression.setBoolean(value, 0, bit0);
    }

    public boolean getBit1() {
        return BooleanCompression.getBoolean(value, 1);
    }

    public void setBit1(boolean bit1) {
        value = BooleanCompression.setBoolean(value, 1, bit1);
    }

    public boolean getBit2() {
        return BooleanCompression.getBoolean(value, 2);
    }

    public void setBit2(boolean bit2) {
        value = BooleanCompression.setBoolean(value, 2, bit2);
    }

    public boolean getBit3() {
        return BooleanCompression.getBoolean(value, 3);
    }

    public void setBit3(boolean bit3) {
        value = BooleanCompression.setBoolean(value, 3, bit3);
    }

    public boolean getBit4() {
        return BooleanCompression.getBoolean(value, 4);
    }

    public void setBit4(boolean bit4) {
        value = BooleanCompression.setBoolean(value, 4, bit4);
    }

    public boolean getBit5() {
        return BooleanCompression.getBoolean(value, 5);
    }

    public void setBit5(boolean bit5) {
        value = BooleanCompression.setBoolean(value, 5, bit5);
    }

    public boolean getBit6() {
        return BooleanCompression.getBoolean(value, 6);
    }

    public void setBit6(boolean bit6) {
        value = BooleanCompression.setBoolean(value, 6, bit6);
    }

    public boolean getBit7() {
        return BooleanCompression.getBoolean(value, 7);
    }

    public void setBit7(boolean bit7) {
        value = BooleanCompression.setBoolean(value, 7, bit7);
    }

    public boolean getNthBit(int n) {
        return BooleanCompression.getBoolean(value, n);
    }

    public void setNthBit(int n, boolean bit) {
        value = BooleanCompression.setBoolean(value, n, bit);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof MultiBooleanMessage)) return false;
        MultiBooleanMessage message = (MultiBooleanMessage) obj;
        return message.value == value;
    }

    @Override
    public String toString() {
        return PrimitiveUtil.toString(this, Integer.toBinaryString(value));
    }
}
