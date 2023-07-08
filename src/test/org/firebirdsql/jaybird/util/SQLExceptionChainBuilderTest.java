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

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    void testBuilder_EmptyOnConstruct_NoAppend() {
        SQLExceptionChainBuilder<SQLException> builder = new SQLExceptionChainBuilder<>();
        
        assertFalse(builder.hasException(), "Empty SQLExceptionChainBuilder should have no Exception");
        assertNull(builder.getException(), "Empty SQLExceptionChainBuilder should have null Exception");
    }
    
    /**
     * Test for SQLExceptionChainBuilder constructed with a root SQLException and no further appends.
     */
    @Test
    void testBuilder_RootOnConstruct_NoAppend() {
        SQLException root = new SQLException();
        
        SQLExceptionChainBuilder<SQLException> builder = new SQLExceptionChainBuilder<>(root);
        
        assertTrue(builder.hasException(), "SQLExceptionChainBuilder has a exception");
        assertSame(root, builder.getException(), "Expected root exception to be identical to returned exception");
    }
    
    /**
     * Test for SQLExceptionChainBuilder constructed empty with one append
     */
    @Test
    void testBuilder_EmptyOnConstruct_OneAppend() {
        SQLException root = new SQLException();
        SQLExceptionChainBuilder<SQLException> builder = new SQLExceptionChainBuilder<>();

        builder.append(root);
        
        assertTrue(builder.hasException(), "SQLExceptionChainBuilder has a exception");
        assertSame(root, builder.getException(), "Expected root exception to be identical to returned exception");
    }
    
    /**
     * Test for SQLExceptionChainBuilder constructed with a root SQLException and multiple appends.
     */
    @Test
    void testBuilder_RootOnConstruct_MultipleAppends() {
        SQLException root = new SQLException();
        List<SQLException> additionalExceptions = new ArrayList<>();
        for (int count = 1; count <= 3; count++) {
            additionalExceptions.add(new SQLException(Integer.toString(count)));
        }
        
        SQLExceptionChainBuilder<SQLException> builder = new SQLExceptionChainBuilder<>(root);
        for (SQLException ex : additionalExceptions) {
            builder.append(ex);
        }
        
        assertTrue(builder.hasException(), "SQLExceptionChainBuilder has a exception");
        SQLException resultException = builder.getException();
        assertSame(root, resultException, "Expected root exception to be identical to returned exception");
        checkExceptionChain(resultException, additionalExceptions);
    }
    
    /**
     * Test for SQLExceptionChainBuilder constructed with an empty root and multiple appends.
     */
    @Test
    void testBuilder_EmptyOnConstruct_MultipleAppends() {
        SQLException root = new SQLException();
        List<SQLException> additionalExceptions = new ArrayList<>();
        for (int count = 1; count <= 3; count++) {
            additionalExceptions.add(new SQLException(Integer.toString(count)));
        }
        
        SQLExceptionChainBuilder<SQLException> builder = new SQLExceptionChainBuilder<>();
        builder.append(root);
        for (SQLException ex : additionalExceptions) {
            builder.append(ex);
        }
        
        assertTrue(builder.hasException(), "SQLExceptionChainBuilder has a exception");
        SQLException resultException = builder.getException();
        assertSame(root, resultException, "Expected root exception to be identical to returned exception");
        checkExceptionChain(resultException, additionalExceptions);
    }
    
    /**
     * Checks the exception chain returned from getNextException (root itself is not checked).
     * 
     * @param root The root SQLException
     * @param expectedExceptions SQLExceptions expected in the chain (excluding the root)
     */
    private void checkExceptionChain(SQLException root, List<SQLException> expectedExceptions) {
        int count = 0;
        SQLException nextException = root;
        while((nextException = nextException.getNextException()) != null) {
            count++;
            if (count <= expectedExceptions.size()) {
                assertSame(expectedExceptions.get(count - 1), nextException, "Unexpected Exception for count " + count);
            } else {
                // Break to avoid circular reference chains
                break;
            }
        }
        assertEquals(expectedExceptions.size(), count, "Unexpected number of exceptions returned");
    }
}
