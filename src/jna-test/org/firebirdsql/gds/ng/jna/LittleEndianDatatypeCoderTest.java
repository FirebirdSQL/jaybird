/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test numeric conversion in little endian
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class LittleEndianDatatypeCoderTest {

    private final LittleEndianDatatypeCoder datatypeCoder =
            new LittleEndianDatatypeCoder(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    @Test
    void encodeShort() {
        short testValue = 0b0110_1011_1010_1001;
        byte[] result = datatypeCoder.encodeShort(testValue);

        assertArrayEquals(new byte[] { (byte) 0b1010_1001, 0b0110_1011 }, result);
    }

    @Test
    void decodeShort() {
        byte[] testValue = { 0b0110_1001, 0b0011_1100 };
        short result = datatypeCoder.decodeShort(testValue);

        assertEquals(0b0011_1100_0110_1001, result);
    }

    @Test
    void encodeInt() {
        int testValue = 0b1011_0110_1001_0000_1111_0101_0001_1010;
        byte[] result = datatypeCoder.encodeInt(testValue);

        assertArrayEquals(new byte[] {0b0001_1010, (byte) 0b1111_0101, (byte) 0b1001_0000, (byte) 0b1011_0110 }, result);
    }

    @Test
    void decodeInt() {
        byte[] testValue = {0b0001_1010, (byte) 0b1111_0101, (byte) 0b1001_0000, (byte) 0b1011_0110 };
        int result = datatypeCoder.decodeInt(testValue);

        assertEquals(0b1011_0110_1001_0000_1111_0101_0001_1010, result);
    }

    @Test
    void encodeLong() {
        long testValue = 0b1000_0001_0100_0010_0010_0100_0001_1000_1001_0001_0101_0010_0010_1100_0001_1010L;
        byte[] result = datatypeCoder.encodeLong(testValue);

        assertArrayEquals(new byte[] { 0b0001_1010, 0b0010_1100, 0b0101_0010, (byte) 0b1001_0001,
                0b0001_1000, 0b0010_0100, 0b0100_0010, (byte) 0b1000_0001 }, result);
    }

    @Test
    void decodeLong() {
        byte[] testValue = { 0b0001_1010, 0b0010_1100, 0b0101_0010, (byte) 0b1001_0001,
                0b0001_1000, 0b0010_0100, 0b0100_0010, (byte) 0b1000_0001 };
        long result = datatypeCoder.decodeLong(testValue);

        assertEquals(0b1000_0001_0100_0010_0010_0100_0001_1000_1001_0001_0101_0010_0010_1100_0001_1010L, result);
    }

    @Test
    void decodeDecimal64() {
        final Decimal64 decimal64 = Decimal64.valueOf("1.234567890123456E123");
        final byte[] bytes = reverseByteOrder(decimal64.toBytes());

        assertEquals(decimal64, datatypeCoder.decodeDecimal64(bytes));
    }

    @Test
    void encodeDecimal64() {
        final Decimal64 decimal64 = Decimal64.valueOf("1.234567890123456E123");
        final byte[] bytes = reverseByteOrder(decimal64.toBytes());

        assertArrayEquals(bytes, datatypeCoder.encodeDecimal64(decimal64));
    }

    @Test
    void decodeDecimal128() {
        final Decimal128 decimal128 = Decimal128.valueOf("1.234567890123456789012345678901234E1234");
        final byte[] bytes = reverseByteOrder(decimal128.toBytes());

        assertEquals(decimal128, datatypeCoder.decodeDecimal128(bytes));
    }

    @Test
    void encodeDecimal128() {
        final Decimal128 decimal128 = Decimal128.valueOf("1.234567890123456789012345678901234E1234");
        final byte[] bytes = reverseByteOrder(decimal128.toBytes());

        assertArrayEquals(bytes, datatypeCoder.encodeDecimal128(decimal128));
    }

    private byte[] reverseByteOrder(byte[] array) {
        final byte[] newArray = new byte[array.length];
        final int maxIndex = newArray.length - 1;
        for (int idx = 0; idx < array.length; idx++) {
            newArray[idx] = array[maxIndex - idx];
        }
        return newArray;
    }
}
