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

import junit.framework.*;

/**
 * Describe class <code>TestFBCallableStatement</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBCallableStatement extends BaseFBTest {
    public static final String CREATE_PROCEDURE =
        "CREATE PROCEDURE notfactorial(number INTEGER) RETURNS (result INTEGER) " +
        "AS " +
        "BEGIN " +
        "  result = number;" +
        "END";
        /*"  DECLARE VARIABLE temp INTEGER; " +
        "BEGIN " +
        "  temp = number - 1; " +
        "  IF (NOT temp IS NULL) THEN BEGIN " +
        "    IF (temp > 0) THEN " +
        "      EXECUTE PROCEDURE notfactorial(:temp) RETURNING_VALUES :temp; " +
        "    ELSE " +
        "      temp = 1; " +
        "    result = number * temp; " +
        "  END " +
        "END";*/

    public static final String DROP_PROCEDURE =
        "DROP PROCEDURE notfactorial;";

    public static final String SELECT_PROCEDURE =
        "SELECT * FROM notfactorial(?);";

    public static final String EXECUTE_PROCEDURE =
        "{call notfactorial(?)}";

    private java.sql.Connection connection;

    public TestFBCallableStatement(String testName) {
        super(testName);
    }


    protected void setUp() throws Exception {
       super.setUp();
        Class.forName(FBDriver.class.getName());
        connection =
            java.sql.DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);
        java.sql.Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(DROP_PROCEDURE);
        }
        catch (Exception e) {}

        stmt.executeUpdate(CREATE_PROCEDURE);
        stmt.close();
    }
    protected void tearDown() throws Exception {
        java.sql.Statement stmt = connection.createStatement();
        stmt.executeUpdate(DROP_PROCEDURE);
        stmt.close();
        connection.close();
        super.tearDown();
    }

    public void testRun() throws Exception {
        java.sql.CallableStatement stmt = connection.prepareCall(EXECUTE_PROCEDURE);
        stmt.setInt(1, 5);
        stmt.execute();
        int ans = stmt.getInt(1);
        assertTrue("got wrong answer, expected 5: " + ans, ans == 5);
            /*java.sql.ResultSet rs = stmt.execute();
              boolean hasResult = false;
              while (rs.next()) {
              hasResult = true;
              int result = rs.getInt(1);
              assertTrue("Wrong result: expecting 5, received " + result, result == 5);
              }
              assertTrue("No result were found.", hasResult);
              rs.close();*/
        stmt.close();
    }
    /*
    public void testExecute() {
        assertTrue(false);
    }
    public void testExecuteQuery() {
        assertTrue(false);
    }
    public void testExecuteUpdate() {
        assertTrue(false);
    }
    */

}
