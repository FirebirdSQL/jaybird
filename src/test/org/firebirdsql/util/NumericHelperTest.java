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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link NumericHelper}
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class NumericHelperTest {

    @Test
    void testToUnsignedLong() {
        assertEquals(Integer.MAX_VALUE, NumericHelper.toUnsignedLong(Integer.MAX_VALUE));
        assertEquals(0xffffffffL, NumericHelper.toUnsignedLong(-1));
        assertEquals(0x80000000L, NumericHelper.toUnsignedLong(Integer.MIN_VALUE));
        assertEquals(0x80000001L, NumericHelper.toUnsignedLong(Integer.MIN_VALUE + 1));
    }

    @Test
    void testFitsUnsigned32BitInteger() {
        assertTrue(NumericHelper.fitsUnsigned32BitInteger(0));
        assertTrue(NumericHelper.fitsUnsigned32BitInteger(0xffffffffL)); // equivalent of int -1 'unsigned'
        assertTrue(NumericHelper.fitsUnsigned32BitInteger(0x80000000L)); // equivalent of Integer.MIN_VALUE 'unsigned'
        assertTrue(NumericHelper.fitsUnsigned32BitInteger(0x80000001L));
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(-1));
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(0x100000000L)); // 0xffffffffL + 1
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(Integer.MIN_VALUE - 1L));
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(Long.MAX_VALUE));
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(Long.MIN_VALUE));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            firstValue, secondValue, expectedResult
            0,          0,           0
            0,          2,           2
            1,          2,           1
            1,          0,           1
            2,          1,           2
            -1,         -2,          -1
            -2,         -1,          -2
            """)
    void firstNonZero_2arg(int firstValue, int secondValue, int expectedResult) {
        assertEquals(expectedResult, NumericHelper.firstNonZero(firstValue, secondValue));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            firstValue, secondValue, thirdValue, expectedResult
            0,          0,           0,          0
            0,          0,           3,          3
            0,          2,           3,          2
            1,          2,           3,          1
            1,          0,           3,          1
            1,          2,           0,          1
            3,          2,           1,          3
            -3,         -2,          -1,         -3
            -1,         -2,          -3,         -1
            """)
    void firstNonZero_3arg(int firstValue, int secondValue, int thirdValue, int expectedResult) {
        assertEquals(expectedResult, NumericHelper.firstNonZero(firstValue, secondValue, thirdValue));
    }

}