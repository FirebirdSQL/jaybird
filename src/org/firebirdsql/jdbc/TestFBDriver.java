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

/**
 * Test suite for the FBDriver class implementation.
 *
 * @author Roman Rokytskyy (rrokytskyy@yahoo.co.uk)
 */

public class TestFBDriver extends TestCase {

    private java.sql.Connection connection;
    private java.sql.Driver driver;

    public TestFBDriver(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestFBDriver.class);
    }



    protected void setUp() throws Exception {
        Class.forName(org.firebirdsql.jdbc.FBDriver.class.getName());
        driver = java.sql.DriverManager.getDriver(TestConst.DB_URL);
    }



    protected void tearDown() {
    }



    public void testAcceptsURL() throws Exception {
        assertTrue(driver.acceptsURL(TestConst.DB_URL));
    }

    public void testConnect() throws Exception {
        System.out.println(TestConst.DB_URL);
        connection = driver.connect(TestConst.DB_URL, TestConst.DB_INFO);
        assertTrue("Connection is null", connection != null);
    }

    public void testJdbcCompliant() {
        // current driver is not JDBC compliant.
        assertTrue(!driver.jdbcCompliant());
    }
}

