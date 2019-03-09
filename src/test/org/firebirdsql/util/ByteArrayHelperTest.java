/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ByteArrayHelper}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class ByteArrayHelperTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void toHexStringEmptyArray() {
        assertEquals("", ByteArrayHelper.toHexString(new byte[0]));
    }

    @Test
    public void toHexString_0x00() {
        assertEquals("00", ByteArrayHelper.toHexString(new byte[] { 0x00 }));
    }

    @Test
    public void toHexString_0x01ff83a3() {
        assertEquals("01FF83A3", ByteArrayHelper.toHexString(new byte[] { 0x01, (byte) 0xff, (byte) 0x83, (byte) 0xa3 }));
    }

    @Test
    public void toHexString_allValues() {
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
    public void toHexString_null_throwsNullPointerException() {
        expectedException.expect(NullPointerException.class);

        ByteArrayHelper.toHexString(null);
    }

    @Test
    public void fromHexString_0x01ff83a3() {
        assertArrayEquals("01FF83A3", new byte[] { 0x01, (byte) 0xff, (byte) 0x83, (byte) 0xa3 },
                ByteArrayHelper.fromHexString("01FF83A3"));
    }

    @Test
    public void fromHexString_allValues() {
        byte[] content = new byte[256];
        for (int idx = 0; idx < 256; idx++) {
            content[idx] = (byte) idx;
        }

        String asHexString = ByteArrayHelper.toHexString(content);

        assertArrayEquals(content, ByteArrayHelper.fromHexString(asHexString));
    }

    @Test
    public void fromBase64String_properlyPadded() {
        final String base64 = "ZWFzdXJlLg==";
        final byte[] expectedValue = "easure.".getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals("base64 decoding", expectedValue, ByteArrayHelper.fromBase64String(base64));
    }

    /**
     * We expect unpadded base64 (which should have been padded) to work
     */
    @Test
    public void fromBase64String_notCorrectlyPadded() {
        final String base64 = "ZWFzdXJlLg";
        final byte[] expectedValue = "easure.".getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals("base64 decoding", expectedValue, ByteArrayHelper.fromBase64String(base64));
    }

}
