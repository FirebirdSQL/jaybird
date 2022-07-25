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
package org.firebirdsql.gds.ng;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link org.firebirdsql.gds.ng.SqlCountHolder}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class SqlCountHolderTest {

    @Test
    void testLongUpdateCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals(7, counts.getLongUpdateCount(), "Unexpected long update count");
    }

    @Test
    void testLongUpdateCount_LongMax() {
        final SqlCountHolder counts = new SqlCountHolder(Long.MAX_VALUE, 8, 9, 10);

        assertEquals(Long.MAX_VALUE, counts.getLongUpdateCount());
    }

    @Test
    void testIntegerUpdateCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals(7, counts.getIntegerUpdateCount(), "Unexpected integer update count");
    }

    @Test
    void testIntegerUpdateCount_IntegerMax() {
        final SqlCountHolder counts = new SqlCountHolder(Integer.MAX_VALUE, 8, 9, 10);

        assertEquals(Integer.MAX_VALUE, counts.getIntegerUpdateCount(), "Unexpected integer update count");
    }

    @Test
    void testIntegerUpdateCount_IntegerMaxPlusOne_returnsZero() {
        final SqlCountHolder counts = new SqlCountHolder(Integer.MAX_VALUE + 1L, 8, 9, 10);

        assertEquals(0, counts.getIntegerUpdateCount(),
                "Integer update count for value larger than Integer.MAX_VALUE should be 0");
    }

    @Test
    void testLongDeleteCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals(8, counts.getLongDeleteCount(), "Unexpected long delete count");
    }

    @Test
    void testLongDeleteCount_LongMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, Long.MAX_VALUE, 9, 10);

        assertEquals(Long.MAX_VALUE, counts.getLongDeleteCount(), "Unexpected long delete count");
    }

    @Test
    void testIntegerDeleteCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals(8, counts.getIntegerDeleteCount(), "Unexpected integer delete count");
    }

    @Test
    void testIntegerDeleteCount_IntegerMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, Integer.MAX_VALUE, 9, 10);

        assertEquals(Integer.MAX_VALUE, counts.getIntegerDeleteCount(), "Unexpected integer delete count");
    }

    @Test
    void testIntegerDeleteCount_IntegerMaxPlusOne_returnsZero() {
        final SqlCountHolder counts = new SqlCountHolder(7, Integer.MAX_VALUE + 1L, 9, 10);

        assertEquals(0, counts.getIntegerDeleteCount(),
                "Integer delete count for value larger than Integer.MAX_VALUE should be 0");
    }

    @Test
    void testLongInsertCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals(9, counts.getLongInsertCount(), "Unexpected long insert count");
    }

    @Test
    void testLongInsertCount_LongMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, Long.MAX_VALUE, 10);

        assertEquals(Long.MAX_VALUE, counts.getLongInsertCount(), "Unexpected long insert count");
    }

    @Test
    void testIntegerInsertCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals(9, counts.getIntegerInsertCount(), "Unexpected integer insert count");
    }

    @Test
    void testIntegerInsertCount_IntegerMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, Integer.MAX_VALUE, 10);

        assertEquals(Integer.MAX_VALUE, counts.getIntegerInsertCount(), "Unexpected integer insert count");
    }

    @Test
    void testIntegerInsertCount_IntegerMaxPlusOne_returnsZero() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, Integer.MAX_VALUE + 1L, 10);

        assertEquals(0, counts.getIntegerInsertCount(),
                "Integer insert count for value larger than Integer.MAX_VALUE should be 0");
    }

    @Test
    void testLongSelectCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals(10, counts.getLongSelectCount(), "Unexpected long select count");
    }

    @Test
    void testLongSelectCount_LongMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE, counts.getLongSelectCount(), "Unexpected long select count");
    }

    @Test
    void testIntegerSelectCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals(10, counts.getIntegerSelectCount(), "Unexpected integer select count");
    }

    @Test
    void testIntegerSelectCount_IntegerMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, counts.getIntegerSelectCount(), "Unexpected integer select count");
    }

    @Test
    void testIntegerSelectCount_IntegerMaxPlusOne_returnsZero() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, Integer.MAX_VALUE + 1L);

        assertEquals(0, counts.getIntegerSelectCount(),
                "Integer select count for value larger than Integer.MAX_VALUE should be 0");
    }
}
