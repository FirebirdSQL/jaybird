package org.firebirdsql.jdbc;


import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.CallableStatement;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.jdbc.FirebirdStatementInfo;


public class TestStatementInfoFactory extends FBTestBase {

    public static final String CREATE_TABLE =
        "CREATE TABLE TEST (                 " +
        "   A INTEGER NOT NULL PRIMARY KEY, " +
        "   B INTEGER NOT NULL)";

    public static final String DROP_TABLE = "DROP TABLE TEST";

    public static final String CREATE_PROC =
        "CREATE PROCEDURE TESTPROC AS " +
        "BEGIN INSERT INTO TEST VALUES (5, 6); END";

    public static final String DROP_PROC = 
        "DROP PROCEDURE TESTPROC";

    private Connection conn;

    public TestStatementInfoFactory(String name) throws SQLException {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        conn = getConnectionViaDriverManager();
        Statement stmt = conn.createStatement();
        try {
            stmt.execute(DROP_PROC);
        } catch (SQLException e1){}
        try {
            stmt.execute(DROP_TABLE);
        } catch (SQLException e2){}
        try {
            stmt.execute(CREATE_TABLE);
        } catch (SQLException e3){}
        try {
            stmt.execute(CREATE_PROC);
        } catch (SQLException e4){}
        stmt.close();
    }

    public void tearDown() throws Exception {
        Statement stmt = conn.createStatement();
        try {
            stmt.execute(DROP_PROC);
            stmt.execute(DROP_TABLE);
        } catch (SQLException e){}
        stmt.close();
        super.tearDown();
    }

    public void testGetExecutionPlan() throws SQLException {
        AbstractStatement stmt = (AbstractStatement)conn.createStatement();
        stmt.execute("SELECT * FROM TEST WHERE A = 2");
        FirebirdStatementInfo stmtInfo = stmt.getStatementInfo();
        assertTrue("Ensure that a valid execution plan is retrieved",
                stmtInfo.getExecutionPlan().indexOf("TEST") != -1);
        stmt.close();
    }

    public void testNonPreparedStatementBeforeExecution() throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            ((AbstractStatement)stmt).getStatementInfo();
            fail("Statement information cannot be fetched for non-prepared "
                    + "statements that have not been executed");
        } catch (SQLException e){
            // Ignore
        }
        stmt.close();
    }

    public void testGetSelectStmtType() throws SQLException {
        AbstractStatement stmt = 
            (AbstractStatement)conn.prepareStatement("SELECT * FROM TEST");
        assertEquals(
                "TYPE_SELECT should be returned for a SELECT statement",
                FirebirdStatementInfo.TYPE_SELECT,
                stmt.getStatementInfo().getStatementType());
        stmt.close(); 
    }

    public void testGetInsertStmtType() throws SQLException {
        AbstractStatement stmt = 
            (AbstractStatement)conn.prepareStatement(
                "INSERT INTO TEST VALUES (5, 6)");
        assertEquals(
                "TYPE_INSERT should be returned for an INSERT statement",
                FirebirdStatementInfo.TYPE_INSERT,
                stmt.getStatementInfo().getStatementType());
        stmt.close();
    }


    public void testGetExecStmtType() throws SQLException {
        CallableStatement callable = conn.prepareCall("{CALL TESTPROC}");
        callable.execute();
        assertEquals(
                "TYPE_EXEC_PROCEDURE should be returned for CallableStatement",
                FirebirdStatementInfo.TYPE_EXEC_PROCEDURE,
                ((AbstractStatement)callable).getStatementInfo()
                    .getStatementType());
        callable.close();
    }

    public void testGetDeleteStmtType() throws SQLException {
        AbstractStatement delStmt = (AbstractStatement)
            conn.prepareStatement("DELETE FROM TEST");
        assertEquals(
                "TYPE_DELETE should be returned for a DELETE statement",
                FirebirdStatementInfo.TYPE_DELETE,
                delStmt.getStatementInfo().getStatementType());
        delStmt.close();
    }

    public void testGetUpdateStmtType() throws SQLException {
        AbstractStatement updStmt = 
            (AbstractStatement)conn.prepareStatement(
                "UPDATE TEST SET A = 2 WHERE B = ?");
        assertEquals(
                "TYPE_UPDATE should be returned for an UPDATE statement",
                FirebirdStatementInfo.TYPE_UPDATE,
                updStmt.getStatementInfo().getStatementType());
        updStmt.close();
    }
}
