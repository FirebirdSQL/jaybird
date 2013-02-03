/*
 * Firebird Open Source J2ee connector - jdbc driver
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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.firebirdsql.common.FBTestBase;

/**
 * Describe class <code>TestFBUnmanagedConnection</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBUnmanagedConnection extends FBTestBase {
    public static final String CREATE_TEST_TABLE =
        "CREATE TABLE connection_test (" +
        "  test_int INTEGER" +
        ");";

    public static final String DROP_TEST_TABLE =
        "DROP TABLE connection_test;";

    public static final String UPDATE_TEST_TABLE =
        "UPDATE connection_test " +
        "SET" +
        "  test_int = 2" +
        ";";

    public static final String INSERT_TEST_TABLE =
        "INSERT INTO connection_test(test_int) " +
        "VALUES(1);";

    public static final String SELECT_TEST_TABLE =
        "SELECT test_int FROM connection_test;";

    public static final String DELETE_TEST_TABLE =
        "DELETE FROM connection_test;";

    private Connection connection;

    public TestFBUnmanagedConnection(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestFBUnmanagedConnection.class);
    }

    protected void setUp() throws Exception {
       super.setUp();
        connection = getConnectionViaDriverManager();
    }

    protected void tearDown() throws Exception {
        closeQuietly(connection);
        super.tearDown();
    }

    public void testCommit() throws Exception {
        connection.setAutoCommit(false);
        executeDropTable(connection, DROP_TEST_TABLE);
        connection.commit();
        executeCreateTable(connection, CREATE_TEST_TABLE);
        connection.commit();
        Statement statement = connection.createStatement();
        try {
            statement.executeUpdate(INSERT_TEST_TABLE);
            connection.commit();
            ResultSet rs = null;
            try {
                rs = statement.executeQuery(SELECT_TEST_TABLE);
                assertTrue("ResultSet is empty", rs.next());
                int value = rs.getInt(1);
                assertEquals("Commit failed", 1, value);
            } finally {
            	closeQuietly(rs);
            }
        } finally {
            closeQuietly(statement);
        }
        executeDropTable(connection, DROP_TEST_TABLE);
        connection.commit();
    }

    public void testCreateStatement() throws Exception {
        Statement statement = connection.createStatement();
        assertTrue("Statement is null", statement != null);
    }

    public void testGetAutoCommit() throws Exception {
        connection.setAutoCommit(true);
        assertTrue("AutoCommit is false", connection.getAutoCommit());
        connection.setAutoCommit(false);
        assertTrue("AutoCommit is true", !connection.getAutoCommit());
    }

    public void testGetMetaData() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        assertTrue("Metadata is null", metaData != null);
    }

    public void testGetTypeMap() throws Exception {
        Map typeMap = connection.getTypeMap();
        assertTrue("TypeMap is null", typeMap != null);
    }

    public void testNativeSQL() throws Exception {
        String nativeSQL = connection.nativeSQL("SELECT * FROM RDB$DATABASE");
        assertTrue("NativeSQL is null", nativeSQL != null);
    }

    /**
     * Describe <code>testCommitsWithNoWork</code> method here.
     * Make sure commit can be called repeatedly with no work done.
     * @exception Exception if an error occurs
     */
    public void testCommitsWithNoWork() throws Exception
    {
        connection.setAutoCommit(false);
        connection.commit();
        connection.commit();
        connection.commit();
    }

}
