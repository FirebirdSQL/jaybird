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

import org.firebirdsql.logging.Logger;

/**
 * Test suite for the FBDriver class implementation.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
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
        log.info(DB_DRIVER_URL);
        connection = driver.connect(DB_DRIVER_URL, DB_INFO);
        assertTrue("Connection is null", connection != null);
    }

    public void testJdbcCompliant() {
        // current driver is not JDBC compliant.
        assertTrue(!driver.jdbcCompliant());
    }
}

