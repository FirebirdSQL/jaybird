/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.
 */

/*
 * CVS modification log:
 * $Log$
 * Revision 1.1  2001/07/09 09:09:51  rrokytskyy
 * Initial revision
 *
 */

package org.firebirdsql.jdbc;

import junit.framework.*;
import java.sql.*;

/**
 * Test suite for the FBUnmanagedConnection implementation.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
 */
public class TestFBUnmanagedConnection extends TestCase {
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

    private java.sql.Connection connection;

    public TestFBUnmanagedConnection(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestFBUnmanagedConnection.class);
    }

    protected void setUp() throws Exception {
        Class.forName(FBDriver.class.getName());
        connection =
            java.sql.DriverManager.getConnection(TestConst.DB_URL, TestConst.DB_INFO);
    }
    protected void tearDown() throws Exception {
        connection.close();
    }

    public void testCommit() throws Exception {
        try{
            Statement statement = connection.createStatement();
            statement.executeUpdate(CREATE_TEST_TABLE);
            connection.commit();
            statement.executeUpdate(INSERT_TEST_TABLE);
            connection.commit();
            ResultSet rs = null;
            try {
                rs = statement.executeQuery(SELECT_TEST_TABLE);
                assert("ResultSet is empty", rs.next());
                int value = rs.getInt(1);
                assert("Commit failed: expecting value=1, received value=" + value, value == 1);
            } finally {
                if (rs != null) rs.close();
            }
            statement.executeUpdate(DROP_TEST_TABLE);
            connection.commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            assert(ex.getMessage(), false);
        }
    }

    public void testCreateStatement() throws Exception {
        Statement statement = connection.createStatement();
        assert("Statement is null", statement != null);
    }

    public void testGetAutoCommit() throws Exception {
        connection.setAutoCommit(true);
        assert("AutoCommit is false", connection.getAutoCommit());
        connection.setAutoCommit(false);
        assert("AutoCommit is false", !connection.getAutoCommit());
    }

    public void testGetMetaData() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        assert("Metadata is null", metaData != null);
    }

    public void testGetTypeMap() throws Exception {
        java.util.Map typeMap = connection.getTypeMap();
        assert("TypeMap is null", typeMap != null);
    }

    public void testNativeSQL() throws Exception {
        String nativeSQL = connection.nativeSQL("SELECT * FROM RDB$DATABASE");
        assert("NativeSQL is null", nativeSQL != null);
    }

    public void testSetAutoCommit() throws Exception {
        testGetAutoCommit();
    }
}
