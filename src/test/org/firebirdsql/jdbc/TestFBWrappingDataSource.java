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
 * Revision 1.6  2003/03/09 18:59:59  rrokytskyy
 * fixed test case failure
 *
 * Revision 1.5  2003/03/09 17:34:36  rrokytskyy
 * removed some deprecations
 *
 * Revision 1.4  2002/11/21 20:33:54  brodsom
 * 1.- Make stmt_handle vars private.
 * 2.- Increase sleep time in TestPoolingConnectionManager and FBWrappingDataSource
 *
 * Revision 1.3  2002/09/28 19:21:37  d_jencks
 * Fixed physical connection leak, made  datasource and ManagedConnectionFactory serializable, and made BlobBufferLength and Integer attribute to be spec-compliant.
 *
 * Revision 1.2  2002/08/29 13:41:16  d_jencks
 * Changed to lgpl only license.  Moved driver to subdirectory to make build system more consistent.
 *
 * Revision 1.1  2002/08/14 13:22:46  d_jencks
 * Moved tests to separate directory. Removed need for jmx classes, and removed jmxri.jar
 *
 * Revision 1.10  2002/06/06 11:24:07  brodsom
 * Performance patch. Log if log4j is in the classpath, don't log if the enviroment variable FBLog4j is false.
 *
 * Revision 1.9  2002/05/09 12:18:29  rrokytskyy
 * fixed couple of issues with correct specification implementation
 * reported by Blas Rodriguez Somoza and Jan Aleman
 *
 * Revision 1.8  2002/03/22 01:43:36  d_jencks
 * Fixed internal association of isc_db_handle to FBManagedConnection to respect the user/password (and FBConnectionRequestInfo) of the FBManagedConnection.  Doesnt break nany tests, but I have no specific test case for the new functionality
 *
 * Revision 1.7  2002/03/21 18:12:40  d_jencks
 * Changed to get a db connection when a ManagedConnection is created.  Note that this may or may not be the db connection used when you start a transaction and do some work.
 *
 * Revision 1.6  2002/02/03 02:45:39  d_jencks
 * Fixed the rest of the bugs! The testsuite now all passes
 *
 * Revision 1.5  2002/02/02 18:58:24  d_jencks
 * converted to log4j logging and cleaned up some test problems.  If you do not wish to use log4j, you may leave out the log4j-core.jar and get no logging
 *
 * Revision 1.4  2002/01/07 06:59:54  d_jencks
 * Revised FBManager to create dialect 3 databases, and the tests to use a newly created database. Simplified and unified test constants. Test targets are now all-tests for all tests and one-test for one test: specify the test as -Dtest=Gds one-test for the TestGds.class test.  Made a few other small changes to improve error messages
 *
 * Revision 1.3  2002/01/06 23:37:58  d_jencks
 * added a connection test to datasource test, cleaned up constants a bit.
 *
 * Revision 1.2  2002/01/05 05:34:06  d_jencks
 * added Doug Lea's concurrent.jar, classes from Concurrent Programming in java, 2nd edition
 *
 * Revision 1.1  2001/11/26 01:04:01  d_jencks
 * Added a datasource DBWrappingDataSource that can be used in standalone applications: allows you to set databaseName, user, and password.  Uses the StandAloneConnectionManager, so there is no pooling at the moment.
 *

 * Initial revision

 *

 */



package org.firebirdsql.jdbc;

import java.util.ArrayList;
import java.util.Iterator;
import java.sql.*;

/**
 * Describe class <code>TestFBWrappingDataSource</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBWrappingDataSource extends BaseFBTest {

    private java.sql.Connection connection;
    private FBWrappingDataSource ds;

    public TestFBWrappingDataSource(String testName) {
        super(testName);
    }


    public void testConnect() throws Exception {
        if (log != null) log.info("Testing FBWrapping DataSource on db: " + DB_DATASOURCE_URL);

        ds = new FBWrappingDataSource();
        ds.setDatabase(DB_DATASOURCE_URL);
        ds.setUserName(DB_USER);
        ds.setPassword(DB_PASSWORD);
        connection = ds.getConnection();
        assertTrue("Connection is null", connection != null);
        connection = ds.getConnection(DB_USER, DB_PASSWORD);
        assertTrue("Connection is null", connection != null);
    }

    public void testOneConnectionWithPooling() throws Exception {
        if (log != null) log.info("Testing FBWrapping DataSource Pooling on db: " + DB_DATASOURCE_URL);

        ds = new FBWrappingDataSource();
        ds.setDatabase(DB_DATASOURCE_URL);
        ds.setMinSize(3);
        ds.setMaxSize(5);
        ds.setBlockingTimeout(100);
        ds.setIdleTimeout(1000);
        ds.setPooling(true);
        connection = ds.getConnection(DB_USER, DB_PASSWORD);
        //connection.setAutoCommit(false);
        assertTrue("Connection is null", connection != null);
        Statement s = connection.createStatement();
        Exception ex = null;
        try {
           s.execute("CREATE TABLE T1 ( C1 SMALLINT, C2 SMALLINT)");
            //s.close();
            ResultSet rs = s.executeQuery("select * from T1");
            rs.close();
        }
        catch (Exception e) {
            ex = e;
        }
        //connection.commit();


        s.execute("DROP TABLE T1");
        s.close();
        //connection.commit();
        connection.close();
        if (ex != null) {
            throw ex;
        }

    }


   public void testPooling() throws Exception {
        if (log != null) log.info("Testing FBWrapping DataSource Pooling on db: " + DB_DATASOURCE_URL);

        ds = new FBWrappingDataSource();
        ds.setDatabase(DB_DATASOURCE_URL);
        ds.setMinSize(3);
        ds.setMaxSize(5);
        ds.setBlockingTimeout(1000);
        ds.setIdleTimeout(20000);
        ds.setPooling(true);
        ds.setUserName(DB_USER);
        ds.setPassword(DB_PASSWORD);
        connection = ds.getConnection();//DB_USER, DB_PASSWORD);
        assertTrue("Connection is null", connection != null);
        Thread.sleep(3000);
        int ccount = ds.getConnectionCount(); // should be 2, 3 total, but one is working
        assertTrue("Wrong number of connections! " + ccount + ", expected " + (ds.getMinSize() - 1), ccount == (ds.getMinSize() - 1));
        connection.close();
        ArrayList cs = new ArrayList();
        for (int i = 0; i < ds.getMaxSize(); i++)
        {
            cs.add(ds.getConnection());//DB_USER, DB_PASSWORD));
        } // end of for ()
        try
        {
            ds.getConnection();//DB_USER, DB_PASSWORD);
           fail("got a connection more than maxsize!");
        }
        catch (SQLException re)
        {
           //got a blocking timeout, good
        } // end of try-catch
        for (Iterator i = cs.iterator(); i.hasNext(); )
        {
           ((Connection)i.next()).close();
        } // end of for ()
        //This will be from same pool due to internal construction of FBDataSource.
        connection = ds.getConnection(DB_USER, DB_PASSWORD);
        assertTrue("Connection is null", connection != null);
        connection.close();

    }

}

