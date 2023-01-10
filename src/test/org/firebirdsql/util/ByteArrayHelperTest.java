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
package org.firebirdsql.util;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link ByteArrayHelper}
 *
 * @author Mark Rotteveel
 */
class ByteArrayHelperTest {

    @Test
    void toHexStringEmptyArray() {
        assertEquals("", ByteArrayHelper.toHexString(new byte[0]));
    }

    @Test
    void toHexString_0x00() {
        assertEquals("00", ByteArrayHelper.toHexString(new byte[] { 0x00 }));
    }

    @Test
    void toHexString_0x01ff83a3() {
        assertEquals("01FF83A3", ByteArrayHelper.toHexString(new byte[] { 0x01, (byte) 0xff, (byte) 0x83, (byte) 0xa3 }));
    }

    @Test
    void toHexString_allValues() {
        byte[] content = new byte[256];
        for (int idx = 0; idx < 256; idx++) {
            content[idx] = (byte) idx;
        }

        String result = ByteArrayHelper.toHexString(content);

        BigInteger fromContent = new BigInteger(content);
        BigInteger fromResult = new BigInteger(result, 16);

        assertEquals(fromContent, fromResult);
    }

    @Test
    void toHexString_null_throwsNullPointerException() {
        assertThatNullPointerException()
                .isThrownBy(() -> ByteArrayHelper.toHexString(null));
    }

    @Test
    void fromHexString_0x01ff83a3() {
        assertArrayEquals(new byte[] { 0x01, (byte) 0xff, (byte) 0x83, (byte) 0xa3 },
                ByteArrayHelper.fromHexString("01FF83A3"), "01FF83A3");
    }

    @Test
    void fromHexString_allValues() {
        byte[] content = new byte[256];
        for (int idx = 0; idx < 256; idx++) {
            content[idx] = (byte) idx;
        }

        String asHexString = ByteArrayHelper.toHexString(content);

        assertArrayEquals(content, ByteArrayHelper.fromHexString(asHexString));
    }

    @Test
    void fromBase64String_properlyPadded() {
        final String base64 = "ZWFzdXJlLg==";
        final byte[] expectedValue = "easure.".getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals(expectedValue, ByteArrayHelper.fromBase64String(base64), "base64 decoding");
    }

    /**
     * We expect unpadded base64 (which should have been padded) to work
     */
    @Test
    void fromBase64String_notCorrectlyPadded() {
        final String base64 = "ZWFzdXJlLg";
        final byte[] expectedValue = "easure.".getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals(expectedValue, ByteArrayHelper.fromBase64String(base64), "base64 decoding");
    }

    @Test
    void fromBase64urlString_properlyPadded() {
        final String base64 = "PDw_Pz8-Pg==";
        final byte[] expectedValue = "<<???>>".getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals(expectedValue, ByteArrayHelper.fromBase64urlString(base64), "base64url decoding");
    }

    /**
     * We expect unpadded base64url (which should have been padded) to work
     */
    @Test
    void fromBase64urlString_notCorrectlyPadded() {
        final String base64 = "PDw_Pz8-Pg";
        final byte[] expectedValue = "<<???>>".getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals(expectedValue, ByteArrayHelper.fromBase64urlString(base64), "base64url decoding");
    }

    @Test
    void indexOf_emptyArray() {
        assertEquals(-1, ByteArrayHelper.indexOf(new byte[0], (byte) 1));
    }

    @Test
    void indexOf_notInArray() {
        assertEquals(-1, ByteArrayHelper.indexOf(new byte[] { 2, 3, 4, 5, 6 }, (byte) 1));
    }

    @Test
    void indexOf_inArray() {
        assertEquals(1, ByteArrayHelper.indexOf(new byte[] { 2, 1, 3, 4, 1 }, (byte) 1));
    }

}
