/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Original developer David Jencks
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

package org.firebirdsql.jdbc;

import junit.framework.*;

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
