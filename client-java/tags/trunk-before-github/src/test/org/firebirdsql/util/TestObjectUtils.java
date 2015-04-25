/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Tests for {@link ObjectUtils}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class TestObjectUtils {

    @Test
    public void testEquals_BothNull() {
        assertTrue("Expected ObjectUtils.equals(null, null) to be true", ObjectUtils.equals(null, null));
    }

    @Test
    public void testEquals_FirstNullSecondNonNull() {
        assertFalse("Expected ObjectUtils.equals(null, \"Test\") to be false", ObjectUtils.equals(null, "Test"));
    }

    @Test
    public void testEquals_FirstNonNullSecondNull() {
        assertFalse("Expected ObjectUtils.equals(\"Test\", null) to be false", ObjectUtils.equals("Test", null));
    }

    @Test
    public void testEquals_BothIdentical() {
        String testValue = "Test";
        assertTrue("Expected ObjectUtils.equals(testValue, testValue) to be true", ObjectUtils.equals(testValue, testValue));
    }

    @Test
    public void testEquals_BothEqual() {
        String testValue = "Test";
        assertTrue("Expected ObjectUtils.equals(testValue, new String(testValue)) to be true",
                ObjectUtils.equals(testValue, new String(testValue)));
    }

    @Test
    public void testEquals_DifferentValues() {
        assertFalse("Expected ObjectUtils.equals(\"Test1\", \"Test2\") to be false", ObjectUtils.equals("Test1", "Test2"));
    }

    @Test
    public void testHashCode_nullValue() {
        assertEquals("Unexpected hashCode for ObjectUtils.hashCode(null)", 0, ObjectUtils.hashCode(null));
    }

    @Test
    public void testHashCode_NonNull() {
        Object value = new Object();
        assertEquals("Unexpected hashCode for ObjectUtils.hashCode(value)", value.hashCode(), ObjectUtils.hashCode(value));
    }

    @Test
    public void testHash_noValues() {
        assertEquals("Unexpected hashCode for ObjectUtils.hash()", Arrays.hashCode(new Object[0]), ObjectUtils.hash());
    }

    @SuppressWarnings("NullArgumentToVariableArgMethod")
    @Test
    public void testHash_null() {
        assertEquals("Unexpected hashCode for ObjectUtils.hash(null)", 0, ObjectUtils.hash((Object[]) null));
    }

    @Test
    public void testHash_singleValue() {
        String value = "Test";
        assertEquals("Unexpected hashCode for ObjectUtils.hash(value)", Arrays.hashCode(new Object[] { value }),
                ObjectUtils.hash(value));
    }

    @Test
    public void testHash_multipleValues() {
        String value1 = "Test";
        String value2 = "Test2";
        assertEquals("Unexpected hashCode for ObjectUtils.hash(value1, value2)", Arrays.hashCode(new Object[] { value1, value2 }),
                ObjectUtils.hash(value1, value2));
    }
}
