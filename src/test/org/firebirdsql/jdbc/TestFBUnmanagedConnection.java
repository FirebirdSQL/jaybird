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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Describe class <code>TestFBUnmanagedConnection</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBUnmanagedConnection extends FBJUnit4TestBase {

    //@formatter:off
    private static final String CREATE_TEST_TABLE =
        "CREATE TABLE connection_test (" +
        "  test_int INTEGER" +
        ")";

    private static final String INSERT_TEST_TABLE = "INSERT INTO connection_test(test_int) VALUES(1)";

    private static final String SELECT_TEST_TABLE = "SELECT test_int FROM connection_test";
    //@formatter:on

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
    }

    @After
    public void tearDown() throws Exception {
        closeQuietly(connection);
    }

    @Test
    public void testCommit() throws Exception {
        connection.setAutoCommit(false);
        executeCreateTable(connection, CREATE_TEST_TABLE);
        connection.commit();
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(INSERT_TEST_TABLE);
            connection.commit();
            try (ResultSet rs = statement.executeQuery(SELECT_TEST_TABLE)) {
                assertTrue("ResultSet is empty", rs.next());
                int value = rs.getInt(1);
                assertEquals("Commit failed", 1, value);
            }
        }
        connection.commit();
    }

    @Test
    public void testCreateStatement() throws Exception {
        Statement statement = connection.createStatement();
        assertTrue("Statement is null", statement != null);
    }

    @Test
    public void testGetAutoCommit() throws Exception {
        connection.setAutoCommit(true);
        assertTrue("AutoCommit is false", connection.getAutoCommit());
        connection.setAutoCommit(false);
        assertTrue("AutoCommit is true", !connection.getAutoCommit());
    }

    @Test
    public void testGetMetaData() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        assertTrue("Metadata is null", metaData != null);
    }

    @Test
    public void testGetTypeMap() throws Exception {
        Map<String, Class<?>> typeMap = connection.getTypeMap();
        assertTrue("TypeMap is null", typeMap != null);
    }

    @Test
    public void testNativeSQL() throws Exception {
        String nativeSQL = connection.nativeSQL("SELECT * FROM RDB$DATABASE");
        assertTrue("NativeSQL is null", nativeSQL != null);
    }

    /**
     * Describe <code>testCommitsWithNoWork</code> method here.
     * Make sure commit can be called repeatedly with no work done.
     *
     * @throws Exception
     *         if an error occurs
     */
    @Test
    public void testCommitsWithNoWork() throws Exception {
        connection.setAutoCommit(false);
        connection.commit();
        connection.commit();
        connection.commit();
    }

}
