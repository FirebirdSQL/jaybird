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
import javax.resource.ResourceException;
import java.sql.*;
import junit.framework.*;
import org.firebirdsql.logging.Logger;

/**
 * Test suite for the FBWrappingDataSource  class implementation.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
 */

public class TestFBWrappingDataSource extends BaseFBTest {

    private java.sql.Connection connection;
    private FBWrappingDataSource ds;

    public TestFBWrappingDataSource(String testName) {
        super(testName);
    }


    public void testConnect() throws Exception {
        log.info("Testing FBWrapping DataSource on db: " + DB_DATASOURCE_URL);

        ds = new FBWrappingDataSource();
        ds.setDatabaseName(DB_DATASOURCE_URL);
        connection = ds.getConnection(DB_USER, DB_PASSWORD);
        assertTrue("Connection is null", connection != null);
        ds.setUser(DB_USER);
        ds.setPassword(DB_PASSWORD);
        connection = ds.getConnection();
        assertTrue("Connection is null", connection != null);
    }

    public void testOneConnectionWithPooling() throws Exception {
        log.info("Testing FBWrapping DataSource Pooling on db: " + DB_DATASOURCE_URL);

        ds = new FBWrappingDataSource();
        ds.setDatabaseName(DB_DATASOURCE_URL);
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
        log.info("Testing FBWrapping DataSource Pooling on db: " + DB_DATASOURCE_URL);

        ds = new FBWrappingDataSource();
        ds.setDatabaseName(DB_DATASOURCE_URL);
        ds.setMinSize(3);
        ds.setMaxSize(5);
        ds.setBlockingTimeout(1000);
        ds.setIdleTimeout(20000);
        ds.setPooling(true);
        connection = ds.getConnection(DB_USER, DB_PASSWORD);
        assertTrue("Connection is null", connection != null);
        Thread.sleep(2000);
        int ccount = ds.getConnectionCount();
        assertTrue("Wrong number of connections!" + ccount, ccount == ds.getMinSize());
        connection.close();
        ArrayList cs = new ArrayList();
        for (int i = 0; i < ds.getMaxSize(); i++)
        {
           cs.add(ds.getConnection(DB_USER, DB_PASSWORD));
        } // end of for ()
        try
        {
           ds.getConnection(DB_USER, DB_PASSWORD);
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
        ds.setUser(DB_USER);
        ds.setPassword(DB_PASSWORD);
        connection = ds.getConnection();
        assertTrue("Connection is null", connection != null);
        connection.close();

    }

}

