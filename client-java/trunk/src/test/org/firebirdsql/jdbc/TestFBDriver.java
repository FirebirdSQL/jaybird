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



/*

 * CVS modification log:

 * $Log$
 * Revision 1.3  2002/10/18 14:22:26  rrokytskyy
 * fixed warnings handling
 *
 * Revision 1.2  2002/08/29 13:41:16  d_jencks
 * Changed to lgpl only license.  Moved driver to subdirectory to make build system more consistent.
 *
 * Revision 1.1  2002/08/14 13:22:46  d_jencks
 * Moved tests to separate directory. Removed need for jmx classes, and removed jmxri.jar
 *
 * Revision 1.7  2002/06/06 11:24:07  brodsom
 * Performance patch. Log if log4j is in the classpath, don't log if the enviroment variable FBLog4j is false.
 *
 * Revision 1.6  2002/02/02 18:58:24  d_jencks
 * converted to log4j logging and cleaned up some test problems.  If you do not wish to use log4j, you may leave out the log4j-core.jar and get no logging
 *
 * Revision 1.5  2002/01/07 06:59:54  d_jencks
 * Revised FBManager to create dialect 3 databases, and the tests to use a newly created database. Simplified and unified test constants. Test targets are now all-tests for all tests and one-test for one test: specify the test as -Dtest=Gds one-test for the TestGds.class test.  Made a few other small changes to improve error messages
 *
 * Revision 1.4  2002/01/06 23:37:58  d_jencks
 * added a connection test to datasource test, cleaned up constants a bit.
 *
 * Revision 1.3  2001/08/28 17:13:23  d_jencks
 * Improved formatting slightly, removed dos cr's
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
import java.util.*;

import org.firebirdsql.logging.Logger;

/**
 * Test suite for the FBDriver class implementation.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBDriver extends BaseFBTest {

    private java.sql.Connection connection;
    private java.sql.Driver driver;

    public TestFBDriver(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestFBDriver.class);
    }



    protected void setUp() throws Exception {
       super.setUp();
        Class.forName(org.firebirdsql.jdbc.FBDriver.class.getName());
        driver = java.sql.DriverManager.getDriver(DB_DRIVER_URL);
    }



    protected void tearDown() throws Exception
    {
       super.tearDown();
    }



    public void testAcceptsURL() throws Exception {
        assertTrue(driver.acceptsURL(DB_DRIVER_URL));
    }

    public void testConnect() throws Exception {
        if (log != null) log.info(DB_DRIVER_URL);
        connection = driver.connect(DB_DRIVER_URL, DB_INFO);
        assertTrue("Connection is null", connection != null);
    }

    public void testJdbcCompliant() {
        // current driver is not JDBC compliant.
        assertTrue(!driver.jdbcCompliant());
    }
    
    /**
     * This method tests if driver correctly handles warnings returned from
     * database. We use SQL dialect mismatch between client and server to 
     * make server return us a warning.
     */
    public void testWarnings() throws Exception {
        Properties info = (Properties)DB_INFO.clone();
        info.setProperty("set_db_sql_dialect", "1");
        
        // open connection and convert DB to SQL dialect 1
        Connection dialect1Connection = 
            DriverManager.getConnection(DB_DRIVER_URL, info);
            
        Statement stmt = dialect1Connection.createStatement();
        
        // execute select statement, driver will pass SQL dialect 3 
        // for this statement and database server will return a warning
        stmt.executeQuery("SELECT 1 as col1 FROM rdb$database");
        
        stmt.close();
        
        SQLWarning warning = dialect1Connection.getWarnings();
        
        warning.printStackTrace();
        
        assertTrue("Connection should have at least one warning.", 
            warning != null);
            
        dialect1Connection.clearWarnings();
        
        assertTrue("After clearing no warnings should be present.",
            dialect1Connection.getWarnings() == null);
            
        dialect1Connection.close();
        
        info.setProperty("set_db_sql_dialect", "3");
        
        Connection dialect3Connection = 
            DriverManager.getConnection(DB_DRIVER_URL, info);
            
        dialect3Connection.close();
    }
    
    /**
     * This test checks if transaction is rolled back when connection is closed, 
     * but still has an active transaction associated with it.
     * 
     * @throws Exception if something went wrong.
     */
    public void testClose() throws Exception {
        connection = driver.connect(DB_DRIVER_URL, DB_INFO);
        
        Statement stmt = connection.createStatement();
        
        stmt.executeUpdate("CREATE TABLE test(id INTEGER, test_value INTEGER)");
        stmt.executeUpdate("INSERT INTO test VALUES (1, 1)");
        
        connection.setAutoCommit(false);
        
        stmt.executeUpdate("UPDATE test SET test_value = 2 WHERE id = 1");
        
        stmt.close();
        
        connection.close();
        
        connection = driver.connect(DB_DRIVER_URL, DB_INFO);
        
        stmt = connection.createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT test_value FROM test WHERE id = 1");
        
        assertTrue("Should have at least one row", rs.next());
        assertTrue("Value should be 1.", rs.getInt(1) == 1);
        assertTrue("Should have only one row.", !rs.next());
        
        rs.close();
        stmt.executeUpdate("DROP TABLE test");
        stmt.close();
        connection.close();
    }
}

