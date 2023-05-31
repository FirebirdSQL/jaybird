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
package org.firebirdsql.gds;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link VaxEncoding}.
 *
 * @author Mark Rotteveel
 */
class VaxEncodingTest {

    @Test
    void iscVaxInteger_singleByte() {
        byte[] encoding = { 5 };

        assertEquals(5, VaxEncoding.iscVaxInteger(encoding, 0, 1));
    }

    @Test
    void iscVaxInteger_twoBytes() {
        byte[] encoding = { 5, 3 };

        assertEquals(773, VaxEncoding.iscVaxInteger(encoding, 0, 2));
    }

    @Test
    void iscVaxInteger_threeBytes() {
        byte[] encoding = { 5, 3, 4 };

        assertEquals(262917, VaxEncoding.iscVaxInteger(encoding, 0, 3));
    }

    @Test
    void iscVaxInteger_fourBytes() {
        byte[] encoding = { 5, 3, 4, 115 };

        assertEquals(1929642757, VaxEncoding.iscVaxInteger(encoding, 0, 4));
    }

    @Test
    void iscVaxInteger_fiveBytes_returnsZero() {
        byte[] encoding = { 5, 3, 4, 115, 45 };

        assertEquals(0, VaxEncoding.iscVaxInteger(encoding, 0, 5));
    }

    @Test
    void iscVaxInteger_usesOffset() {
        byte[] encoding = { 5, 3, 4, 115, 45 };

        assertEquals(762512387, VaxEncoding.iscVaxInteger(encoding, 1, 4));
    }

    @Test
    void encodeIscVaxInteger_shouldDecode() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(5);
        final int testValue = 789345734;

        VaxEncoding.encodeVaxInteger(bos, testValue);

        byte[] result = bos.toByteArray();
        assertEquals(4, result[0], "length");
        assertEquals(testValue, VaxEncoding.iscVaxInteger(result, 1, 4), "decoded value");
    }

    @Test
    void iscVaxLong_singleByte() {
        byte[] encoding = { 5 };

        assertEquals(5L, VaxEncoding.iscVaxLong(encoding, 0, 1));
    }

    @Test
    void iscVaxLong_twoBytes() {
        byte[] encoding = { 5, 3 };

        assertEquals(773L, VaxEncoding.iscVaxLong(encoding, 0, 2));
    }

    @Test
    void iscVaxLong_threeBytes() {
        byte[] encoding = { 5, 3, 4 };

        assertEquals(262917L, VaxEncoding.iscVaxLong(encoding, 0, 3));
    }

    @Test
    void iscVaxLong_fourBytes() {
        byte[] encoding = { 5, 3, 4, 115 };

        assertEquals(1929642757L, VaxEncoding.iscVaxLong(encoding, 0, 4));
    }

    @Test
    void iscVaxLong_fiveBytes() {
        byte[] encoding = { 5, 3, 4, 115, -128 };

        assertEquals(551685456645L, VaxEncoding.iscVaxLong(encoding, 0, 5));
    }

    @Test
    void iscVaxLong_sixBytes() {
        byte[] encoding = { 5, 3, 4, 115, -128, 23 };

        assertEquals(25840452895493L, VaxEncoding.iscVaxLong(encoding, 0, 6));
    }

    @Test
    void iscVaxLong_sevenBytes() {
        byte[] encoding = { 5, 3, 4, 115, -128, 23, 127 };

        assertEquals(35773162495148805L, VaxEncoding.iscVaxLong(encoding, 0, 7));
    }

    @Test
    void iscVaxLong_eightBytes() {
        byte[] encoding = { 5, 3, 4, 115, -128, 23, 127, 45 };

        assertEquals(3278364894201905925L, VaxEncoding.iscVaxLong(encoding, 0, 8));
    }

    @Test
    void iscVaxLong_nineBytes_returnsZero() {
        byte[] encoding = { 5, 3, 4, 115, -128, 23, 127, 45, 1 };

        assertEquals(0L, VaxEncoding.iscVaxLong(encoding, 0, 9));
    }

    @Test
    void iscVaxLong_usesOffset() {
        byte[] encoding = { 5, 3, 4, 115, -128, 23, 127, 45, 1 };

        assertEquals(84863706905904131L, VaxEncoding.iscVaxLong(encoding, 1, 8));
    }

    @Test
    void encodeIscVaxLong_shouldDecode() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(5);
        final long testValue = 7893457342389489729L;

        VaxEncoding.encodeVaxLong(bos, testValue);

        byte[] result = bos.toByteArray();
        assertEquals(8, result[0], "length");
        assertEquals(testValue, VaxEncoding.iscVaxLong(result, 1, 8), "decoded value");
    }

    @Test
    void iscVaxInteger2() {
        byte[] encoding = { 5, 3 };

        assertEquals(773, VaxEncoding.iscVaxInteger2(encoding, 0));
    }

    @Test
    void iscVaxInteger2_usesOffset() {
        byte[] encoding = { 5, 3, 83 };

        assertEquals(21251, VaxEncoding.iscVaxInteger2(encoding, 1));
    }
}