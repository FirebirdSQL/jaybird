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
 * Revision 1.10  2002/06/11 13:35:12  brodsom
 * bug (log!=null) not tested in PreparedStatement and testFBUnmanagedConnection, restore previous loggerFactory
 *
 * Revision 1.9  2002/03/19 17:47:13  d_jencks
 * combined code to use new FBFields only, fixed a little commit bug
 *
 * Revision 1.8  2002/02/02 18:58:24  d_jencks
 * converted to log4j logging and cleaned up some test problems.  If you do not wish to use log4j, you may leave out the log4j-core.jar and get no logging
 *
 * Revision 1.7  2002/01/07 06:59:54  d_jencks
 * Revised FBManager to create dialect 3 databases, and the tests to use a newly created database. Simplified and unified test constants. Test targets are now all-tests for all tests and one-test for one test: specify the test as -Dtest=Gds one-test for the TestGds.class test.  Made a few other small changes to improve error messages
 *
 * Revision 1.6  2002/01/06 23:37:58  d_jencks
 * added a connection test to datasource test, cleaned up constants a bit.
 *
 * Revision 1.5  2001/11/25 23:15:55  d_jencks
 * Implemented autocommit that does not interfere with connections managed by XAResource or LocalTransaction.  Made Driver reuse ManagedConnectionFactory for same database url.
 *
 * Revision 1.4  2001/08/28 17:13:23  d_jencks
 * Improved formatting slightly, removed dos cr's
 *
 * Revision 1.3  2001/07/18 20:07:31  d_jencks
 * Added better GDSExceptions, new NativeSQL, and CallableStatement test from Roman Rokytskyy
 *
 * Revision 1.2  2001/07/15 21:17:36  d_jencks
 * Updated to use assertTrue rather than assert, for junit 3.7
 *
 * Revision 1.1  2001/07/13 18:16:15  d_jencks
 * Implementation of jdbc 1.0 Driver contributed by Roman Rokytskyy
 *

 * Revision 1.1  2001/07/09 09:09:51  rrokytskyy

 * Initial revision

 *

 */



package org.firebirdsql.jdbc;

import junit.framework.*;
import java.sql.*;
import org.firebirdsql.logging.Logger;


/**
 * Test suite for the FBUnmanagedConnection implementation.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
 */

public class TestFBUnmanagedConnection extends BaseFBTest {
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
       super.setUp();
        Class.forName(FBDriver.class.getName());
        connection =
            java.sql.DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);
    }
    protected void tearDown() throws Exception {
        try 
        {
            if (!connection.getAutoCommit()) 
            {
                connection.commit();
            } // end of if ()
        } catch (Exception e) 
        {
            //these messages are too annoying.
            //if (log!=null) log.debug("Possible problem committing before close of connection-- possibly not a problem", e);
        } // end of try-catch

        connection.close();
        super.tearDown();
    }

    public void testCommit() throws Exception {
        try{
            connection.setAutoCommit(false);
            Statement statement = connection.createStatement();
            try {
               statement.execute(DROP_TEST_TABLE);
           }
           catch (Exception e) {}
           statement.executeUpdate(CREATE_TEST_TABLE);
            connection.commit();
            statement.executeUpdate(INSERT_TEST_TABLE);
            connection.commit();
            ResultSet rs = null;
            try {
                rs = statement.executeQuery(SELECT_TEST_TABLE);
                assertTrue("ResultSet is empty", rs.next());
                int value = rs.getInt(1);
                assertTrue("Commit failed: expecting value=1, received value=" + value, value == 1);
            } finally {
                if (rs != null) rs.close();
            }
            statement.executeUpdate(DROP_TEST_TABLE);
            connection.commit();
        } catch(Exception ex) {
            if (log!=null) log.warn("failing testCommit",  ex);
            assertTrue(ex.getMessage(), false);
        }
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
        java.util.Map typeMap = connection.getTypeMap();
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
