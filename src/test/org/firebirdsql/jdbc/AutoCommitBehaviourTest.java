/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for behaviour surrounding autocommit.
 * <p>
 * Tests in this class are specifically concerned with interactions between statements and result sets during
 * autocommit. A lot of the autocommit behaviour is tested elsewhere.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
    @Test
    void testDifferentStatementExecution_ClosesResultSet() throws Exception {
        // Check actual holdability, for example OOConnection forces HOLD_CURSORS_OVER_COMMIT always
        assumeThat("Test requires ResultSet.CLOSE_CURSORS_AT_COMMIT", connection.getHoldability(),
                is(ResultSet.CLOSE_CURSORS_AT_COMMIT));

        Statement stmt1 = connection.createStatement();
        Statement stmt2 = connection.createStatement();

        ResultSet rs1 = stmt1.executeQuery(SELECT_ALL_ID_TABLE);
        assertTrue(rs1.next(), "Expected a row");
        assertFalse(rs1.isClosed(), "Expected rs1 open");

        ResultSet rs2 = stmt2.executeQuery(SELECT_ALL_ID_TABLE);

        assertTrue(rs1.isClosed(), "Expected rs1 closed");

        SQLException exception = assertThrows(SQLException.class, rs1::next, "Expected exception on rs1.next()");
        assertThat(exception, message(equalTo("The result set is closed")));

        for (int count = 1; count <= MAX_ID; count++) {
            assertTrue(rs2.next(), "Expected true for rs2.next() nr " + count);
        }
    }

    /**
     * Executing another statement in autocommit and the result set is holdable should keep it open.
     */
    @Test
    void testHoldableDifferentStatementExecution_ResultSetRemainsOpen() throws Exception {
        connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        Statement stmt1 = connection.createStatement();
        Statement stmt2 = connection.createStatement();

        ResultSet rs1 = stmt1.executeQuery(SELECT_ALL_ID_TABLE);
        assertTrue(rs1.next(), "Expected a row");
        assertFalse(rs1.isClosed(), "Expected rs1 open");

        ResultSet rs2 = stmt2.executeQuery(SELECT_ALL_ID_TABLE);

        assertFalse(rs1.isClosed(), "Expected rs1 open");

        for (int count = 2; count <= MAX_ID; count++) {
            assertTrue(rs1.next(), "Expected true for rs1.next() nr " + count);
        }

        for (int count = 1; count <= MAX_ID; count++) {
            assertTrue(rs2.next(), "Expected true for rs2.next() nr " + count);
        }
    }
}
