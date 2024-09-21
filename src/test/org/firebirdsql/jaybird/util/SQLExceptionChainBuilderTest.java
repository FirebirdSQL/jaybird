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
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SQLExceptionChainBuilder}.
 * 
 * @author Mark Rotteveel
 */
class SQLExceptionChainBuilderTest {

    /**
     * Test for empty SQLExceptionChainBuilder.
     */
    @Test
    void testBuilder_NoAppend() {
        var builder = new SQLExceptionChainBuilder();
        
        assertFalse(builder.hasException(), "Empty SQLExceptionChainBuilder should have no Exception");
        assertNull(builder.getException(), "Empty SQLExceptionChainBuilder should have null Exception");
        assertThat(builder.optException(), is(emptyOptional()));
        assertDoesNotThrow(builder::throwIfPresent);
    }
    
    /**
     * Test for SQLExceptionChainBuilder with one append
     */
    @Test
    void testBuilder_OneAppend() {
        var root = new SQLException();
        var builder = new SQLExceptionChainBuilder();

        builder.append(root);
        
        assertTrue(builder.hasException(), "SQLExceptionChainBuilder should have an exception");
        assertSame(root, builder.getException(), "Expected root exception to be identical to returned exception");
        assertThat(builder.optException(), is(optionalWithValue(root)));
        checkExceptionChain(root, List.of());
        SQLException e = assertThrows(SQLException.class, builder::throwIfPresent);
        assertSame(root, e, "expected root");
    }
    
    /**
     * Test for SQLExceptionChainBuilder with multiple appends.
     */
    @Test
    void testBuilder_MultipleAppends() {
        var root = new SQLException();
        var additionalExceptions = new ArrayList<SQLException>();
        for (int count = 1; count <= 3; count++) {
            additionalExceptions.add(new SQLException(Integer.toString(count)));
        }
        
        var builder = new SQLExceptionChainBuilder();

        builder.append(root);
        for (SQLException ex : additionalExceptions) {
            builder.append(ex);
        }
        
        assertTrue(builder.hasException(), "SQLExceptionChainBuilder should have an exception");
        SQLException resultException = builder.getException();
        assertSame(root, resultException, "Expected root exception to be identical to returned exception");
        checkExceptionChain(resultException, additionalExceptions);
        SQLException e = assertThrows(SQLException.class, builder::throwIfPresent);
        assertSame(root, e, "expected root");
    }

    /**
     * Test for SQLExceptionChainBuilder with one append and one addFirst
     */
    @Test
    void testBuilder_OneAppendOneAddFirst() {
        var initialRoot = new SQLException("initial root");
        var finalRoot = new SQLException("final root");
        var builder = new SQLExceptionChainBuilder();

        builder.append(initialRoot).addFirst(finalRoot);

        assertTrue(builder.hasException(), "SQLExceptionChainBuilder should have an exception");
        SQLException resultException = builder.getException();
        assertSame(finalRoot, resultException, "Expected final root exception to be the returned exception");
        checkExceptionChain(resultException, List.of(initialRoot));
        SQLException e = assertThrows(SQLException.class, builder::throwIfPresent);
        assertSame(finalRoot, e, "expected finalRoot");
    }

    /**
     * Checks the exception chain returned from getNextException (root itself is not checked, other than it's not null).
     * 
     * @param root The root SQLException
     * @param expectedExceptions SQLExceptions expected in the chain (excluding the root)
     */
    private static void checkExceptionChain(@Nullable SQLException root, List<SQLException> expectedExceptions) {
        assertNotNull(root, "root");
        int count = 0;
        SQLException nextException = root;
        while((nextException = nextException.getNextException()) != null) {
            count++;
            if (count <= expectedExceptions.size()) {
                assertSame(expectedExceptions.get(count - 1), nextException, "Unexpected exception for count " + count);
            } else {
                // Break to avoid circular reference chains
                break;
            }
        }
        assertEquals(expectedExceptions.size(), count, "Unexpected number of exceptions returned");
    }
}
