/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestBase;

import java.sql.*;

public class TestFBStatement extends FBTestBase {

    private Connection con;

    private static final int DATA_ITEMS = 5;
    private static final String CREATE_TABLE = "CREATE TABLE test ( col1 INTEGER )";
    private static final String INSERT_DATA = "INSERT INTO test(col1) VALUES(?)";
    private static final String SELECT_DATA = "SELECT col1 FROM test ORDER BY col1";

    public TestFBStatement(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        con = this.getConnectionViaDriverManager();

        try {
            executeCreateTable(con, CREATE_TABLE);
            prepareTestData();
        } finally {
            closeQuietly(con);
        }
        con = this.getConnectionViaDriverManager();
    }

    protected void tearDown() throws Exception {
        try {
            closeQuietly(con);
        } finally {
            super.tearDown();
        }
    }

    /**
     * Closing a statement twice should not result in an Exception.
     *
     * @throws SQLException
     */
    public void testDoubleClose() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.close();
        stmt.close();
    }

    /**
     * Test of initial value of isCloseOnCompletion, expected: false.
     * <p>
     * JDBC 4.1 feature.
     * </p>
     *
     * @throws SQLException
     */
    public void testIsCloseOnCompletion_initial() throws SQLException {
        // Cast so it also works under JDBC 3.0 and 4.0
        FBStatement stmt = (FBStatement) con.createStatement();
        assertFalse("Initial value of isCloseOnCompletion expected to be false",
                stmt.isCloseOnCompletion());
    }

    /**
     * Test of value of isCloseOnCompletion after closeOnCompletion call,
     * expected: true.
     * <p>
     * JDBC 4.1 feature.
     * </p>
     *
     * @throws SQLException
     */
    public void testIsCloseOnCompletion_afterCloseOnCompletion() throws SQLException {
        // Cast so it also works under JDBC 3.0 and 4.0
        FBStatement stmt = (FBStatement) con.createStatement();
        stmt.closeOnCompletion();
        assertTrue("Value of isCloseOnCompletion after closeOnCompletion expected to be true",
                stmt.isCloseOnCompletion());
    }

    /**
     * Test of value of isCloseOnCompletion after multiple calls to
     * closeOnCompletion call, expected: true.
     * <p>
     * JDBC 4.1 feature.
     * </p>
     *
     * @throws SQLException
     */
    public void testIsCloseOnCompletion_multipleCloseOnCompletion() throws SQLException {
        // Cast so it also works under JDBC 3.0 and 4.0
        FBStatement stmt = (FBStatement) con.createStatement();
        stmt.closeOnCompletion();
        stmt.closeOnCompletion();
        assertTrue("Value of isCloseOnCompletion after closeOnCompletion expected to be true",
                stmt.isCloseOnCompletion());
    }

    /**
     * Test if an implicit close (by fully reading the resultset) while closeOnCompletion is false, will not close
     * the statement.
     *
     * @throws SQLException
     */
    public void testNoCloseOnCompletion_StatementOpen_afterImplicitResultSetClose() throws SQLException {
        FBStatement stmt = (FBStatement) con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet) stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertFalse("Resultset should be open", rs.isClosed());
                assertFalse("Statement should be open", stmt.isClosed());
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertEquals(DATA_ITEMS, count);
            assertTrue("Resultset should be closed (automatically closed after last result read)", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test if an explicit close (by calling close()) while closeOnCompletion is false, will not close
     * the statement.
     *
     * @throws SQLException
     */
    public void testNoCloseOnCompletion_StatementOpen_afterExplicitResultSetClose() throws SQLException {
        FBStatement stmt = (FBStatement) con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet) stmt.getResultSet();
            assertFalse("Resultset should be open", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());

            rs.close();

            assertTrue("Resultset should be closed", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test if an implicit close (by fully reading the resultset) while closeOnCompletion is true, will close
     * the statement.
     *
     * @throws SQLException
     */
    public void testCloseOnCompletion_StatementClosed_afterImplicitResultSetClose() throws SQLException {
        FBStatement stmt = (FBStatement) con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            stmt.closeOnCompletion();
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet) stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertFalse("Resultset should be open", rs.isClosed());
                assertFalse("Statement should be open", stmt.isClosed());
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertEquals(DATA_ITEMS, count);
            assertTrue("Resultset should be closed (automatically closed after last result read)", rs.isClosed());
            assertTrue("Statement should be closed", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test if an explicit close (by calling close()) while closeOnCompletion is true, will close
     * the statement.
     *
     * @throws SQLException
     */
    public void testCloseOnCompletion_StatementClosed_afterExplicitResultSetClose() throws SQLException {
        FBStatement stmt = (FBStatement) con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            stmt.closeOnCompletion();
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet) stmt.getResultSet();
            assertFalse("Resultset should be open", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());

            rs.close();

            assertTrue("Resultset should be closed", rs.isClosed());
            assertTrue("Statement should be closed", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test if a executing a query which does not produce a resultset (eg an INSERT without generated keys) will not close the
     * statement.
     */
    public void testCloseOnCompletion_StatementOpen_afterNonResultSetQuery() throws SQLException {
        FBStatement stmt = (FBStatement) con.createStatement();
        try {
            stmt.closeOnCompletion();
            stmt.execute("INSERT INTO test(col1) VALUES(" + DATA_ITEMS + ")");

            assertFalse("Statement should be open", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if Firebird 1.5+ custom exception messages work.
     */
    public void testCustomExceptionMessage() throws Exception {
        final DatabaseMetaData metaData = con.getMetaData();
        final int databaseMajorVersion = metaData.getDatabaseMajorVersion();
        final int databaseMinorVersion = metaData.getDatabaseMinorVersion();
        assertTrue("Test only works on Firebird 1.5 or higher",
                databaseMajorVersion == 1 && databaseMinorVersion >= 5 || databaseMajorVersion > 1);

        //@formatter:off
        executeDDL(con, "CREATE EXCEPTION simple_exception 'Standard message'", new int[0]);
        executeDDL(con,
                "CREATE PROCEDURE testexception " +
                "AS " +
                "BEGIN " +
                "  EXCEPTION simple_exception 'Custom message';" +
                "END", new int[0]);
        //@formatter:on

        Statement stmt = con.createStatement();
        try {
            stmt.execute("EXECUTE PROCEDURE testexception");

            fail("Expected an exception");
        } catch (SQLException e) {
            assertTrue("Exception does not contain the custom message", e.getMessage().contains("\nCustom message\n"));
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if Firebird 3 parametrized exceptions are correctly rendered.
     */
    public void testParametrizedExceptions() throws Exception {
        assertTrue("Test only works on Firebird 3 or higher", con.getMetaData().getDatabaseMajorVersion() >= 3);
        executeDDL(con, "CREATE EXCEPTION two_param_exception 'Param 1 ''@1'', Param 2 ''@2'''", new int[0]);

        Statement stmt = con.createStatement();
        try {
            //@formatter:off
            stmt.execute(
                "EXECUTE BLOCK AS " +
                "BEGIN " +
                "  EXCEPTION two_param_exception USING ('value_1', 'value2'); " +
                "END"
            );
            //@formatter:on
            fail("Expected an exception");
        } catch (SQLException e) {
            assertTrue("Parametrized exception does not contain expected message",
                    e.getMessage().contains("\nParam 1 'value_1', Param 2 'value2'\n"));
        } finally {
            stmt.close();
        }
    }

    public void testRetrievingUpdateCountAndResultSet() throws Exception {
        DatabaseMetaData md = con.getMetaData();
        int majorVersion = md.getDatabaseMajorVersion();
        int minorVersion = md.getDatabaseMinorVersion();
        assertTrue("Test only works on Firebird 2.1 or higher",
                majorVersion == 2 && minorVersion >= 1 || majorVersion > 2);

        Statement stmt = con.createStatement();
        try {
            boolean isResultSet = stmt.execute("INSERT INTO test(col1) VALUES(5) RETURNING col1");

            assertTrue("Expected first result to be a result set", isResultSet);
            ResultSet rs = stmt.getResultSet();
            assertNotNull("Result set should not be null", rs);
            assertTrue("Expected a row in the result set", rs.next());
            assertEquals("Unexpected value in result set", 5, rs.getInt(1));
            assertFalse("Expected only one row", rs.next());
            assertEquals("Update count should be -1 before first call to getMoreResults", -1, stmt.getUpdateCount());

            assertFalse("Next result should not be a result set", stmt.getMoreResults());
            assertNull("Expected null result set", stmt.getResultSet());
            assertEquals("Update count should be 1 after first call to getMoreResults", 1, stmt.getUpdateCount());

            assertFalse("Next result should not be a result set", stmt.getMoreResults());
            assertNull("Expected null result set", stmt.getResultSet());
            assertEquals("Update count should be -1 after second call to getMoreResults", -1, stmt.getUpdateCount());
        } finally {
            stmt.close();
        }
    }

    private void prepareTestData() throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(INSERT_DATA);
        try {
            for (int i = 0; i < DATA_ITEMS; i++) {
                pstmt.setInt(1, i);
                pstmt.executeUpdate();
            }
        } finally {
            pstmt.close();
        }
    }
}
