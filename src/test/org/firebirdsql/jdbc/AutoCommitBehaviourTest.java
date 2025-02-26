// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertResultSetClosed;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertResultSetOpen;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for behaviour surrounding autocommit.
 * <p>
 * Tests in this class are specifically concerned with interactions between statements and result sets during
 * autocommit. A lot of the autocommit behaviour is tested elsewhere.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 2.2
 */
class AutoCommitBehaviourTest {

    private static final String CREATE_ID_TABLE = "CREATE TABLE ID_TABLE (ID INTEGER PRIMARY KEY)";
    private static final String INSERT_ID_TABLE = "INSERT INTO ID_TABLE (ID) VALUES (?)";
    private static final String SELECT_ALL_ID_TABLE = "SELECT ID FROM ID_TABLE";
    private static final int MAX_ID = 50;

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_ID_TABLE);

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        connection.setAutoCommit(false);
        try (Statement stmt = connection.createStatement();
             PreparedStatement pstmt = connection.prepareStatement(INSERT_ID_TABLE)) {
            stmt.execute("delete from ID_TABLE");
            for (int idValue = 1; idValue <= MAX_ID; idValue++) {
                pstmt.setInt(1, idValue);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    /**
     * Executing another statement in autocommit should close any previously opened result set if it isn't holdable.
     */
    @ParameterizedTest
    @ValueSource(ints = { ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_FORWARD_ONLY })
    void testDifferentStatementExecution_ClosesResultSet(int resultSetType) throws Exception {
        assumeThat("Test requires ResultSet.CLOSE_CURSORS_AT_COMMIT", connection.getHoldability(),
                is(ResultSet.CLOSE_CURSORS_AT_COMMIT));

        var stmt1 = connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
        assertEquals(resultSetType, stmt1.getResultSetType(), "stmt1.getResultSetType");
        var stmt2 = connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
        assertEquals(resultSetType, stmt2.getResultSetType(), "stmt2.getResultSetType");

        var rs1 = stmt1.executeQuery(SELECT_ALL_ID_TABLE);
        assertEquals(resultSetType, rs1.getType(), "rs1.getType");
        assertNextRow(rs1);
        assertResultSetOpen(rs1, "Expected rs1 open");

        var rs2 = stmt2.executeQuery(SELECT_ALL_ID_TABLE);
        assertEquals(resultSetType, rs2.getType(), "rs2.getType");

        assertResultSetClosed(rs1, "Expected rs1 closed by execution of stmt2");

        var exception = assertThrows(SQLException.class, rs1::next, "Expected exception on rs1.next()");
        assertThat(exception, message(startsWith("The result set is closed")));

        for (int count = 1; count <= MAX_ID; count++) {
            assertNextRow(rs2, "Expected true for rs2.next() nr " + count);
        }
    }

    /**
     * Executing another statement in autocommit and the result set is holdable should keep it open.
     */
    @ParameterizedTest
    @ValueSource(ints = { ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.TYPE_FORWARD_ONLY })
    void testHoldableDifferentStatementExecution_ResultSetRemainsOpen(int resultSetType) throws Exception {
        connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        var stmt1 = connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
        assertEquals(resultSetType, stmt1.getResultSetType(), "stmt1.getResultSetType");
        var stmt2 = connection.createStatement(resultSetType, ResultSet.CONCUR_READ_ONLY);
        assertEquals(resultSetType, stmt2.getResultSetType(), "stmt2.getResultSetType");

        var rs1 = stmt1.executeQuery(SELECT_ALL_ID_TABLE);
        assertEquals(resultSetType, rs1.getType(), "rs1.getType");
        assertNextRow(rs1);
        assertResultSetOpen(rs1, "Expected rs1 open");

        var rs2 = stmt2.executeQuery(SELECT_ALL_ID_TABLE);
        assertEquals(resultSetType, rs2.getType(), "rs2.getType");

        assertResultSetOpen(rs1, "Expected rs1 open");

        for (int count = 2; count <= MAX_ID; count++) {
            assertNextRow(rs1, "Expected true for rs1.next() nr " + count);
        }

        for (int count = 1; count <= MAX_ID; count++) {
            assertNextRow(rs2, "Expected true for rs2.next() nr " + count);
        }
    }
}
