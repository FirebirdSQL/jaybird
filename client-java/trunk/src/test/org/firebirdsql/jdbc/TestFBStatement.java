package org.firebirdsql.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.common.FBTestBase;

public class TestFBStatement extends FBTestBase {

    private Connection con;

    private static final int DATA_ITEMS = 5;
    public static final String CREATE_TABLE = "CREATE TABLE test ( col1 INTEGER )";
    public static final String DROP_TABLE = "DROP TABLE test";
    public static final String INSERT_DATA = "INSERT INTO test(col1) VALUES(?)";
    public static final String SELECT_DATA = "SELECT col1 FROM test ORDER BY col1";

    public TestFBStatement(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        Class.forName(FBDriver.class.getName());
        con = this.getConnectionViaDriverManager();

        try {
            Statement ddlStmt = con.createStatement();
            try {
                try {
                    ddlStmt.execute(DROP_TABLE);
                } catch (SQLException ex) {
                    // ignore
                }

                ddlStmt.execute(CREATE_TABLE);

            } finally {
                closeQuietly(ddlStmt);
            }

        } finally {
            closeQuietly(con);
        }
        con = this.getConnectionViaDriverManager();
    }

    protected void tearDown() throws Exception {
        Statement stmt = con.createStatement();
        try {

        } finally {
            closeQuietly(stmt);
        }

        closeQuietly(con);

        super.tearDown();
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
        prepareTestData();
        FBStatement stmt = (FBStatement)con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet)stmt.getResultSet();
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
        prepareTestData();
        FBStatement stmt = (FBStatement)con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet)stmt.getResultSet();
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
        prepareTestData();
        FBStatement stmt = (FBStatement)con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            stmt.closeOnCompletion();
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet)stmt.getResultSet();
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
        prepareTestData();
        FBStatement stmt = (FBStatement)con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            stmt.closeOnCompletion();
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet)stmt.getResultSet();
            assertFalse("Resultset should be open", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());
            
            rs.close();

            assertTrue("Resultset should be closed", rs.isClosed());
            assertTrue("Statement should be closed", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }
    
    // TODO Add test for queries not producing a ResultSet (closeOnCompletion should not affect the statement)
    // TODO Add similar tests for PreparedStatement and CallableStatement

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
