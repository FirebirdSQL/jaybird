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
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test numeric conversion in big endian
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class BigEndianDatatypeCoderTest {

    private final BigEndianDatatypeCoder datatypeCoder =
            new BigEndianDatatypeCoder(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    @Test
    void encodeShort() {
        short testValue = 0b0110_1011_1010_1001;
        byte[] result = datatypeCoder.encodeShort(testValue);

        assertArrayEquals(new byte[] { 0b0110_1011, (byte) 0b1010_1001 }, result);
    }

    @Test
    void decodeShort() {
        byte[] testValue = { 0b0110_1001, 0b0011_1100 };
        short result = datatypeCoder.decodeShort(testValue);

        assertEquals(0b0110_1001_0011_1100, result);
    }

    @Test
    void decodeShort_null() {
        assertEquals(0, datatypeCoder.decodeShort(null));
    }

    @Test
    void decodeInt_null() {
        assertEquals(0, datatypeCoder.decodeInt(null));
    }

    @Test
    void testDecodeLong_null() {
        assertEquals(0L, datatypeCoder.decodeLong(null));
    }

    @Test
    void decodeDecimal64() {
        final Decimal64 decimal64 = Decimal64.valueOf("1.234567890123456E123");
        final byte[] bytes = decimal64.toBytes();

        assertEquals(decimal64, datatypeCoder.decodeDecimal64(bytes));
    }

    @Test
    void decodeDecimal64_null() {
        assertNull(datatypeCoder.decodeDecimal64(null));
    }

    @Test
    void encodeDecimal64() {
        final Decimal64 decimal64 = Decimal64.valueOf("1.234567890123456E123");
        final byte[] bytes = decimal64.toBytes();

        assertArrayEquals(bytes, datatypeCoder.encodeDecimal64(decimal64));
    }

    @Test
    void encodeDecimal64_null() {
        assertNull(datatypeCoder.encodeDecimal64(null));
    }

    @Test
    void decodeDecimal128() {
        final Decimal128 decimal128 = Decimal128.valueOf("1.234567890123456789012345678901234E1234");
        final byte[] bytes = decimal128.toBytes();

        assertEquals(decimal128, datatypeCoder.decodeDecimal128(bytes));
    }

    @Test
    void decodeDecimal128_null() {
        assertNull(datatypeCoder.decodeDecimal128(null));
    }

    @Test
    void encodeDecimal128() {
        final Decimal128 decimal128 = Decimal128.valueOf("1.234567890123456789012345678901234E1234");
        final byte[] bytes = decimal128.toBytes();

        assertArrayEquals(bytes, datatypeCoder.encodeDecimal128(decimal128));
    }

    @Test
    void encodeDecimal128_null() {
        assertNull(datatypeCoder.encodeDecimal128(null));
    }
    
}
