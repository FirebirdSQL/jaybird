// SPDX-FileCopyrightText: Copyright 2016-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.util;

import org.junit.jupiter.api.Test;

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
        assertEquals(0xffff_ffffL, NumericHelper.toUnsignedLong(-1));
        assertEquals(0x8000_0000L, NumericHelper.toUnsignedLong(Integer.MIN_VALUE));
        assertEquals(0x8000_0001L, NumericHelper.toUnsignedLong(Integer.MIN_VALUE + 1));
    }

    @Test
    void testFitsUnsigned32BitInteger() {
        assertTrue(NumericHelper.fitsUnsigned32BitInteger(0));
        assertTrue(NumericHelper.fitsUnsigned32BitInteger(0xffff_ffffL)); // equivalent of int -1 'unsigned'
        assertTrue(NumericHelper.fitsUnsigned32BitInteger(0x8000_0000L)); // equivalent of Integer.MIN_VALUE 'unsigned'
        assertTrue(NumericHelper.fitsUnsigned32BitInteger(0x8000_0001L));
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(-1));
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(0x1_0000_0000L)); // 0xffffffffL + 1
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(Integer.MIN_VALUE - 1L));
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(Long.MAX_VALUE));
        assertFalse(NumericHelper.fitsUnsigned32BitInteger(Long.MIN_VALUE));
    }

}