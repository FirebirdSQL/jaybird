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
 * Revision 1.1  2001/11/26 01:04:01  d_jencks
 * Added a datasource DBWrappingDataSource that can be used in standalone applications: allows you to set databaseName, user, and password.  Uses the StandAloneConnectionManager, so there is no pooling at the moment.
 *

 * Initial revision

 *

 */



package org.firebirdsql.jdbc;

import java.util.ArrayList;
import javax.resource.ResourceException;
import java.sql.SQLException;
import junit.framework.*;

/**
 * Test suite for the FBWrappingDataSource  class implementation.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
 */

public class TestFBWrappingDataSource extends TestCase {

    private java.sql.Connection connection;
    private FBWrappingDataSource ds;

    public TestFBWrappingDataSource(String testName) {
        super(testName);
    }

    //public static Test suite() {
    //    return new TestSuite(TestFBDriver.class);
    // }







    public void testConnect() throws Exception {
        System.out.println("Testing FBWrapping DataSource on db: " + TestConst.DB_URL);

        ds = new FBWrappingDataSource();
        ds.setDatabaseName(TestConst.DB_URL);
        connection = ds.getConnection(TestConst.DB_USER, TestConst.DB_PASSWORD);
        assertTrue("Connection is null", connection != null);
        ds.setUser(TestConst.DB_USER);
        ds.setPassword(TestConst.DB_PASSWORD);
        connection = ds.getConnection();
        assertTrue("Connection is null", connection != null);
    }

    public void testPooling() throws Exception {
        System.out.println("Testing FBWrapping DataSource Pooling on db: " + TestConst.DB_URL);

        ds = new FBWrappingDataSource();
        ds.setDatabaseName(TestConst.DB_URL);
        ds.setMinSize(3);
        ds.setMaxSize(5);
        ds.setBlockingTimeout(100);
        ds.setIdleTimeout(1000);
        ds.setPooling(true);
        connection = ds.getConnection(TestConst.DB_USER, TestConst.DB_PASSWORD);
        assertTrue("Connection is null", connection != null);
        Thread.sleep(500);
        int ccount = ds.getConnectionCount();
        assertTrue("Wrong number of connections!" + ccount, ccount == ds.getMinSize());
        connection.close();
        ArrayList cs = new ArrayList();
        for (int i = 0; i < ds.getMaxSize(); i++)
        {
           cs.add(ds.getConnection(TestConst.DB_USER, TestConst.DB_PASSWORD));      
        } // end of for ()
        try 
        {
           ds.getConnection(TestConst.DB_USER, TestConst.DB_PASSWORD);              
           fail("got a connection more than maxsize!");
        }
        catch (SQLException re)
        {
           //got a blocking timeout, good    
        } // end of try-catch
        
        ds.setUser(TestConst.DB_USER);
        ds.setPassword(TestConst.DB_PASSWORD);
        connection = ds.getConnection();
        assertTrue("Connection is null", connection != null);
        connection.close();

    }

}

