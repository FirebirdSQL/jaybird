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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.gds.ISCConstants;

/**
 * Tests for retrieval of auto generated keys through {@link java.sql.PreparedStatement}
 * implementation {@link FBPreparedStatement} created through {@link FBConnection}
 * <p>
 * This is an integration test which uses an actual database.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBPreparedStatementGeneratedKeys extends FBTestGeneratedKeysBase {
    
    private static final String TEXT_VALUE = "Some text to insert";
    private static final String TEST_INSERT_QUERY = "INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES (?)";

    public TestFBPreparedStatementGeneratedKeys(String name) {
        super(name);
    }
    
    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with value {@link Statement#NO_GENERATED_KEYS}.
     * <p>
     * Expected: INSERT statement type and empty generatedKeys resultset.
     * </p>
     * 
     * @throws Exception
     */
    public void testPrepare_INSERT_noGeneratedKeys() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);
            assertEquals(FirebirdPreparedStatement.TYPE_INSERT, ((FirebirdPreparedStatement)stmt).getStatementType());
            
            stmt.setString(1, TEXT_VALUE);
            assertFalse("Expected statement not to produce a resultset", stmt.execute());
            
            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);
            
            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset without columns", 0, metaData.getColumnCount());
            
            assertFalse("Expected no rows in resultset", rs.next());
            
            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }
    
    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with {@link Statement#RETURN_GENERATED_KEYS}.
     * <p>
     * Expected: EXEC_PRODUCERE statement type, all columns of table returned, single row resultset
     * </p>
     */
    public void testPrepare_INSERT_returnGeneratedKeys() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement)stmt).getStatementType());
            
            stmt.setString(1, TEXT_VALUE);
            assertTrue("Expected statement to produce a resultset", stmt.execute());
            
            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);
            
            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 2 columns", 2, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
            assertEquals("Unexpected second column", "TEXT", metaData.getColumnName(2));
            
            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertEquals(TEXT_VALUE, rs.getString(2));
            assertFalse("Expected no second row", rs.next());
            
            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }
    
    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT which already has a RETURNING clause.
     * <p>
     * Expected: EXEC_PRODUCERE statement type, all columns of table returned, single row resultset
     * </p>
     */
    public void testPrepare_INSERT_returnGeneratedKeys_withReturning() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY + " RETURNING ID", Statement.RETURN_GENERATED_KEYS);
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement)stmt).getStatementType());
            
            stmt.setString(1, TEXT_VALUE);
            assertTrue("Expected statement to produce a resultset", stmt.execute());
            
            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);
            
            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 1 column", 1, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
            
            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertFalse("Expected no second row", rs.next());
            
            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }
    
    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT for a non existent table.
     * <p>
     * Expected: SQLException Table unknown
     * </p>
     */
    public void testPrepare_INSERT_returnGeneratedKeys_nonExistentTable() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            con.prepareStatement("INSERT INTO TABLE_NON_EXISTENT(TEXT) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            fail("Expected SQLException for INSERT with non existent table");
        } catch (SQLException ex) {
            assertEquals("42000", ex.getSQLState());
            assertEquals(ISCConstants.isc_dsql_error, ex.getErrorCode());
            assertTrue("Unexpected exception message\n" + ex.getMessage(), ex.getMessage().contains("Table unknown\nTABLE_NON_EXISTENT"));
        } finally {
            closeQuietly(con);
        }
    }
    
    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int[])} with a single column index.
     * <p>
     * Expected: EXEC_PRODUCERE statement type, single row resultset with only the specified column.
     * </p>
     * 
     * @throws Exception
     */
    public void testPrepare_INSERT_columnIndexes() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, new int[] {1});
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement)stmt).getStatementType());
            
            stmt.setString(1, TEXT_VALUE);
            assertTrue("Expected statement to produce a resultset", stmt.execute());
            
            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);
            
            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 1 column", 1, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
            
            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertFalse("Expected no second row", rs.next());
            
            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }
    
    // Other combination for execute(String, int[]) already covered in TestGeneratedKeysQuery
    
    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, String[])} with a single column name.
     * <p>
     * Expected: single row resultset with only the specified column.
     * </p>
     * 
     * @throws Exception
     */
    public void testPrepare_INSERT_columnNames() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, new String[] {"ID"});
            assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement)stmt).getStatementType());
            
            stmt.setString(1, TEXT_VALUE);
            assertTrue("Expected statement to produce a resultset", stmt.execute());
            
            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);
            
            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 1 column", 1, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
            
            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertFalse("Expected no second row", rs.next());
            
            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }
    
    /**
     * Test for {@link FBStatement#execute(String, String[])} with an array of columns containing a non-existent column name.
     * <p>
     * Expected: SQLException for Column unknown.
     * </p>
     * 
     * @throws Exception
     */
    public void testPrepare_INSERT_columnNames_nonExistentColumn() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            con.prepareStatement(TEST_INSERT_QUERY, new String[] {"ID", "NON_EXISTENT"});
            fail("Expected an SQLException for specifying a non-existent column");
        } catch (SQLException ex) {
            assertEquals("42000", ex.getSQLState());
            assertEquals(ISCConstants.isc_dsql_error, ex.getErrorCode());
            assertTrue("Unexpected exception message\n" + ex.getMessage(), ex.getMessage().contains("Column unknown\nNON_EXISTENT"));
        } finally {
            closeQuietly(con);
        }
    }
    
    // TODO In the current implementation executeUpdate uses almost identical logic as execute, decide to test separately or not

}
