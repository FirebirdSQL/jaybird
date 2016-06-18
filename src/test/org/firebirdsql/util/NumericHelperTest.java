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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link NumericHelper}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class NumericHelperTest {

    @Test
    public void testToUnsignedLong() {
        assertEquals(Integer.MAX_VALUE, NumericHelper.toUnsignedLong(Integer.MAX_VALUE));
        assertEquals(0xffffffffL, NumericHelper.toUnsignedLong(-1));
        assertEquals(0x80000000L, NumericHelper.toUnsignedLong(Integer.MIN_VALUE));
        assertEquals(0x80000001L, NumericHelper.toUnsignedLong(Integer.MIN_VALUE + 1));
    }

    @Test
    public void testFitsUnsigned32BitInteger() {
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
}