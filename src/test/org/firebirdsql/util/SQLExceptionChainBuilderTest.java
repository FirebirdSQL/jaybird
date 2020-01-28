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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link SQLExceptionChainBuilder}.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class SQLExceptionChainBuilderTest {

    /**
     * Test for empty SQLExceptionChainBuilder.
     */
    @Test
    public void testBuilder_EmptyOnConstruct_NoAppend() {
        SQLExceptionChainBuilder<SQLException> builder = new SQLExceptionChainBuilder<>();
        
        assertFalse("Empty SQLExceptionChainBuilder should have no Exception", builder.hasException());
        assertNull("Empty SQLExceptionChainBuilder should have null Exception", builder.getException());
    }
    
    /**
     * Test for SQLExceptionChainBuilder constructed with a root SQLException and no further appends.
     */
    @Test
    public void testBuilder_RootOnConstruct_NoAppend() {
        SQLException root = new SQLException();
        
        SQLExceptionChainBuilder<SQLException> builder = new SQLExceptionChainBuilder<>(root);
        
        assertTrue("SQLExceptionChainBuilder has a exception", builder.hasException());
        assertSame("Expected root exception to be identical to returned exception", root, builder.getException());
    }
    
    /**
     * Test for SQLExceptionChainBuilder constructed empty with one append
     */
    @Test
    public void testBuilder_EmptyOnConstruct_OneAppend() {
        SQLException root = new SQLException();
        SQLExceptionChainBuilder<SQLException> builder = new SQLExceptionChainBuilder<>();

        builder.append(root);
        
        assertTrue("SQLExceptionChainBuilder has a exception", builder.hasException());
        assertSame("Expected root exception to be identical to returned exception", root, builder.getException());
    }
    
    /**
     * Test for SQLExceptionChainBuilder constructed with a root SQLException and multiple appends.
     */
    @Test
    public void testBuilder_RootOnConstruct_MultipleAppends() {
        SQLException root = new SQLException();
        List<SQLException> additionalExceptions = new ArrayList<>();
        for (int count = 1; count <= 3; count++) {
            additionalExceptions.add(new SQLException(Integer.toString(count)));
        }
        
        SQLExceptionChainBuilder<SQLException> builder = new SQLExceptionChainBuilder<>(root);
        for (SQLException ex : additionalExceptions) {
            builder.append(ex);
        }
        
        assertTrue("SQLExceptionChainBuilder has a exception", builder.hasException());
        SQLException resultException = builder.getException();
        assertSame("Expected root exception to be identical to returned exception", root, resultException);
        checkExceptionChain(resultException, additionalExceptions);
    }
    
    /**
     * Test for SQLExceptionChainBuilder constructed with an empty root and multiple appends.
     */
    @Test
    public void testBuilder_EmptyOnConstruct_MultipleAppends() {
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
        
        assertTrue("SQLExceptionChainBuilder has a exception", builder.hasException());
        SQLException resultException = builder.getException();
        assertSame("Expected root exception to be identical to returned exception", root, resultException);
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
                assertSame("Unexpected Exception for count " + count, expectedExceptions.get(count - 1), nextException);
            } else {
                // Break to avoid circular reference chains
                break;
            }
        }
        assertEquals("Unexpected number of exceptions returned", expectedExceptions.size(), count);
    }
}
