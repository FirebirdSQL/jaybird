/*
 * $Id$
 *
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
package org.firebirdsql.gds.ng;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link org.firebirdsql.gds.ng.SqlCountHolder}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestSqlCountHolder {

    @SuppressWarnings("deprecation")
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testLongUpdateCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals("Unexpected long update count", 7, counts.getLongUpdateCount());
    }

    @Test
    public void testLongUpdateCount_LongMax() {
        final SqlCountHolder counts = new SqlCountHolder(Long.MAX_VALUE, 8, 9, 10);

        assertEquals(Long.MAX_VALUE, counts.getLongUpdateCount());
    }

    @Test
    public void testIntegerUpdateCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals("Unexpected integer update count", 7, counts.getIntegerUpdateCount());
    }

    @Test
    public void testIntegerUpdateCount_IntegerMax() {
        final SqlCountHolder counts = new SqlCountHolder(Integer.MAX_VALUE, 8, 9, 10);

        assertEquals("Unexpected integer update count", Integer.MAX_VALUE, counts.getIntegerUpdateCount());
    }

    @Test
    public void testIntegerUpdateCount_IntegerMaxPlusOne_returnsZero() {
        final SqlCountHolder counts = new SqlCountHolder(Integer.MAX_VALUE + 1L, 8, 9, 10);

        assertEquals("Integer update count for value larger than Integer.MAX_VALUE should be 0",
                0, counts.getIntegerUpdateCount());
    }

    @Test
    public void testLongDeleteCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals("Unexpected long delete count", 8, counts.getLongDeleteCount());
    }

    @Test
    public void testLongDeleteCount_LongMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, Long.MAX_VALUE, 9, 10);

        assertEquals("Unexpected long delete count", Long.MAX_VALUE, counts.getLongDeleteCount());
    }

    @Test
    public void testIntegerDeleteCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals("Unexpected integer delete count", 8, counts.getIntegerDeleteCount());
    }

    @Test
    public void testIntegerDeleteCount_IntegerMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, Integer.MAX_VALUE, 9, 10);

        assertEquals("Unexpected integer delete count", Integer.MAX_VALUE, counts.getIntegerDeleteCount());
    }

    @Test
    public void testIntegerDeleteCount_IntegerMaxPlusOne_returnsZero() {
        final SqlCountHolder counts = new SqlCountHolder(7, Integer.MAX_VALUE + 1L, 9, 10);

        assertEquals("Integer delete count for value larger than Integer.MAX_VALUE should be 0",
                0, counts.getIntegerDeleteCount());
    }

    @Test
    public void testLongInsertCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals("Unexpected long insert count", 9, counts.getLongInsertCount());
    }

    @Test
    public void testLongInsertCount_LongMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, Long.MAX_VALUE, 10);

        assertEquals("Unexpected long insert count", Long.MAX_VALUE, counts.getLongInsertCount());
    }

    @Test
    public void testIntegerInsertCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals("Unexpected integer insert count", 9, counts.getIntegerInsertCount());
    }

    @Test
    public void testIntegerInsertCount_IntegerMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, Integer.MAX_VALUE, 10);

        assertEquals("Unexpected integer insert count", Integer.MAX_VALUE, counts.getIntegerInsertCount());
    }

    @Test
    public void testIntegerInsertCount_IntegerMaxPlusOne_returnsZero() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, Integer.MAX_VALUE + 1L, 10);

        assertEquals("Integer insert count for value larger than Integer.MAX_VALUE should be 0",
                0, counts.getIntegerInsertCount());
    }

    //

    @Test
    public void testLongSelectCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals("Unexpected long select count", 10, counts.getLongSelectCount());
    }

    @Test
    public void testLongSelectCount_LongMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, Long.MAX_VALUE);

        assertEquals("Unexpected long select count", Long.MAX_VALUE, counts.getLongSelectCount());
    }

    @Test
    public void testIntegerSelectCount_basic() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, 10);

        assertEquals("Unexpected integer select count", 10, counts.getIntegerSelectCount());
    }

    @Test
    public void testIntegerSelectCount_IntegerMax() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, Integer.MAX_VALUE);

        assertEquals("Unexpected integer select count", Integer.MAX_VALUE, counts.getIntegerSelectCount());
    }

    @Test
    public void testIntegerSelectCount_IntegerMaxPlusOne_returnsZero() {
        final SqlCountHolder counts = new SqlCountHolder(7, 8, 9, Integer.MAX_VALUE + 1L);

        assertEquals("Integer select count for value larger than Integer.MAX_VALUE should be 0",
                0, counts.getIntegerSelectCount());
    }
}
