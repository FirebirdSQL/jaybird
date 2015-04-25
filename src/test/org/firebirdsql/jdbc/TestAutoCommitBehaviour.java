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
public class TestAutoCommitBehaviour extends FBTestBase {

    private static final String CREATE_ID_TABLE = "CREATE TABLE ID_TABLE (ID INTEGER PRIMARY KEY)";
    private static final String INSERT_ID_TABLE = "INSERT INTO ID_TABLE (ID) VALUES (?)";
    private static final String SELECT_ALL_ID_TABLE = "SELECT ID FROM ID_TABLE";
    private static final int MAX_ID = 50;

    public TestAutoCommitBehaviour(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Connection connection = getConnectionViaDriverManager();
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(CREATE_ID_TABLE);
            stmt.close();

            connection.setAutoCommit(false);
            PreparedStatement pstmt = connection.prepareStatement(INSERT_ID_TABLE);
            try {
                for (int idValue = 1; idValue <= MAX_ID; idValue++) {
                    pstmt.setInt(1, idValue);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
            } finally {
                pstmt.close();
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Executing another statement in autocommit should close any previously opened result set if it isn't holdable.
     */
    public void testDifferentStatementExecution_ClosesResultSet() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        try {
            // Check actual holdability, for example OOConnection forces HOLD_CURSORS_OVER_COMMIT always
            assertEquals("Test requires ResultSet.CLOSE_CURSORS_AT_COMMIT", ResultSet.CLOSE_CURSORS_AT_COMMIT,
                    connection.getHoldability());

            Statement stmt1 = connection.createStatement();
            Statement stmt2 = connection.createStatement();

            ResultSet rs1 = stmt1.executeQuery(SELECT_ALL_ID_TABLE);
            assertTrue("Expected a row", rs1.next());
            assertFalse("Expected rs1 open", rs1.isClosed());

            ResultSet rs2 = stmt2.executeQuery(SELECT_ALL_ID_TABLE);

            assertTrue("Expected rs1 closed", rs1.isClosed());

            try {
                rs1.next();
                fail ("Expected exception on rs1.next()");
            } catch (SQLException ex) {
                assertEquals("The result set is closed", ex.getMessage());
            }

            for (int count = 1; count <= MAX_ID; count++) {
                assertTrue("Expected true for rs2.next() nr " + count, rs2.next());
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Executing another statement in autocommit and the result set is holdable should keep it open.
     */
    public void testHoldableDifferentStatementExecution_ResultSetRemainsOpen() throws Exception {
        Connection connection = getConnectionViaDriverManager();

        try {
            connection.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);
            Statement stmt1 = connection.createStatement();
            Statement stmt2 = connection.createStatement();

            ResultSet rs1 = stmt1.executeQuery(SELECT_ALL_ID_TABLE);
            assertTrue("Expected a row", rs1.next());
            assertFalse("Expected rs1 open", rs1.isClosed());

            ResultSet rs2 = stmt2.executeQuery(SELECT_ALL_ID_TABLE);

            assertFalse("Expected rs1 open", rs1.isClosed());

            for (int count = 2; count <= MAX_ID; count++) {
                assertTrue("Expected true for rs1.next() nr " + count, rs1.next());
            }

            for (int count = 1; count <= MAX_ID; count++) {
                assertTrue("Expected true for rs2.next() nr " + count, rs2.next());
            }
        } finally {
            connection.close();
        }
    }
}
