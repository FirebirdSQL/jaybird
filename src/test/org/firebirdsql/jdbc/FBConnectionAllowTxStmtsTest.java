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
import org.firebirdsql.common.function.ThrowingBiFunction;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jdbc.FBStatementAllowTxStmtsTest.IsolationLevel;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNoNextRow;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.jdbc.FBStatementAllowTxStmtsTest.assertCurrentTransaction;
import static org.firebirdsql.jdbc.FBStatementAllowTxStmtsTest.getTransactionId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for allowing or disallowing transaction management statements in {@link FBConnection}.
 */
@SuppressWarnings("SqlSourceToSinkFlow")
class FBConnectionAllowTxStmtsTest {

    private static final String RECREATE_TABLE = "recreate table TEST (COL1 integer primary key)";
    private static final String INSERT_COL1_IS_1 = "insert into TEST (COL1) values (1)";
    private static final String SELECT_COL1_IS_1 = "select COL1 from TEST where COL1 = 1";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            RECREATE_TABLE);

    /** Connection shared by tests with {@code allowTxStmts=false}. */
    private static Connection conDisallowTx;
    /** Connection shared by tests with {@code allowTxStmts=true}. */
    private static Connection conAllowTx;

    @BeforeAll
    static void initConnection() throws SQLException {
        conDisallowTx = getConnectionViaDriverManager(PropertyNames.allowTxStmts, "false");
        conAllowTx = getConnectionViaDriverManager(PropertyNames.allowTxStmts, "true");
    }

    @BeforeEach
    void resetConnection() throws SQLException {
        conDisallowTx.setAutoCommit(true);
        conAllowTx.setAutoCommit(true);
    }

    @AfterAll
    static void closeConnection() {
        try {
            closeQuietly(conDisallowTx, conAllowTx);
        } finally {
            conDisallowTx = null;
            conAllowTx = null;
        }
    }

    private static Connection getConnection(boolean allowTxStmts) {
        return allowTxStmts ? conAllowTx : conDisallowTx;
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "{0} - {1} - {2}")
    @MethodSource("transactionManagementAlwaysDisallowPrepareCallCases")
    void prepareCallMethods_alwaysDisallowTransactionManagementStatements(String description, String sql,
            ThrowingBiFunction<Connection, String, CallableStatement> prepare, boolean allowTxStmts) {
        Connection connection = getConnection(allowTxStmts);
        var exception = assertThrows(SQLFeatureNotSupportedException.class,
                () -> prepare.apply(connection, sql));
        assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_prepareCallWithTxStmt));
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("transactionManagementDisallowPrepareStatementCases")
    void prepareStatementMethods_disallowTransactionManagementStatements(String description, String sql,
            ThrowingBiFunction<Connection, String, PreparedStatement> prepare, int expectedErrorCode) {
        Connection connection = getConnection(false);
        var exception = assertThrows(SQLException.class, () -> prepare.apply(connection, sql));
        assertThat(exception, errorCodeEquals(expectedErrorCode));
    }

    // NOTE: The following only tests basic execution through PreparedStatement, further details of
    // the FBTxPreparedStatement class are tested in FBTxPreparedStatementTest

    @ParameterizedTest(name = "{0}")
    @MethodSource("prepareStatementCases")
    void prepareStatementMethods_allowsCommitPrepareAndExecute(String description,
            ThrowingBiFunction<Connection, String, PreparedStatement> prepare) throws Exception {
        Connection connection = getConnection(true);
        try (var stmt = connection.createStatement();
             PreparedStatement commitStmt = prepare.apply(connection, "commit")) {
            stmt.execute(RECREATE_TABLE);
            connection.setAutoCommit(false);
            stmt.execute(INSERT_COL1_IS_1);
            long currentTxId = getTransactionId(stmt);
            assertDoesNotThrow(() -> commitStmt.execute());
            // Expected a new transaction
            assertNotEquals(currentTxId, getTransactionId(stmt));
            try (var rs = stmt.executeQuery(SELECT_COL1_IS_1)) {
                assertNextRow(rs, "Expected data in TEST to have been committed");
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("prepareStatementCases")
    void prepareStatementMethods_allowsRollbackPrepareAndExecute(String description,
            ThrowingBiFunction<Connection, String, PreparedStatement> prepare) throws Exception {
        Connection connection = getConnection(true);
        try (var stmt = connection.createStatement();
             PreparedStatement rollbackStmt = prepare.apply(connection, "rollback")) {
            stmt.execute(RECREATE_TABLE);
            connection.setAutoCommit(false);
            stmt.execute(INSERT_COL1_IS_1);
            long currentTxId = getTransactionId(stmt);
            assertDoesNotThrow(() -> rollbackStmt.execute());
            // Expected a new transaction
            assertNotEquals(currentTxId, getTransactionId(stmt));
            try (var rs = stmt.executeQuery(SELECT_COL1_IS_1)) {
                // should have rolled back data
                assertNoNextRow(rs, "Expected data in TEST to have been rolled back");
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("prepareStatementCases")
    void prepareStatementMethods_allowsSetTransactionPrepareAndExecute(String description,
            ThrowingBiFunction<Connection, String, PreparedStatement> prepare) throws Exception {
        Connection connection = getConnection(true);
        try (var stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            try (PreparedStatement setTxStmt = assertDoesNotThrow(() -> prepare.apply(connection, "set transaction"))) {
                setTxStmt.execute();
            }
            assertCurrentTransaction(stmt, IsolationLevel.SNAPSHOT, -1, false, false, true);
            connection.commit();
            try (PreparedStatement setTxStmt = assertDoesNotThrow(() -> prepare.apply(connection,
                    "set transaction read only read committed wait lock timeout 500 no auto undo"))) {
                setTxStmt.execute();
            }
            assertCurrentTransaction(stmt, IsolationLevel.READ_COMMITTED, 500, true, false, false);
            assertEquals(getTransactionId(stmt), getTransactionId(stmt), "Expected same transaction");
            connection.commit();
            try (PreparedStatement setTxStmt = assertDoesNotThrow(() -> prepare.apply(connection,
                    "set transaction snapshot table stability reserving TEST for shared write"))) {
                setTxStmt.execute();
            }
            assertCurrentTransaction(stmt, IsolationLevel.SNAPSHOT_TABLE_STABILITY, -1, false, false, true);

            if (FirebirdSupportInfo.supportInfoFor(connection).isVersionEqualOrAbove(4)) {
                connection.commit();
                try (PreparedStatement setTxStmt = assertDoesNotThrow(() -> prepare.apply(connection,
                        "set transaction read write read committed no wait auto commit"))) {
                    setTxStmt.execute();
                }
                assertCurrentTransaction(stmt, IsolationLevel.READ_COMMITTED, 0, false, true, true);
            }
        }
    }

    private static Stream<Arguments> prepareStatementCases() {
        return Stream.of(
                prepareStatementTestCase("prepareStatement(String)", Connection::prepareStatement),
                prepareStatementTestCase("prepareStatement(String, int)",
                        (connection, sql) -> connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)),
                prepareStatementTestCase("prepareStatement(String, int[])",
                        (connection, sql) -> connection.prepareStatement(sql, new int[] { 1, 2 })),
                prepareStatementTestCase("prepareStatement(String, String[])",
                        (connection, sql) -> connection.prepareStatement(sql, new String[] { "IGNORED1", "IGNORED2" })),
                prepareStatementTestCase("prepareStatement(String, int, int)",
                        (connection, sql) -> connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                                ResultSet.CONCUR_READ_ONLY)),
                prepareStatementTestCase("prepareStatement(String, int, int, int)",
                        (connection, sql) -> connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                                ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)));
    }

    private static Arguments prepareStatementTestCase(String description,
            ThrowingBiFunction<Connection, String, PreparedStatement> prepare) {
        return Arguments.of(description, prepare);
    }

    private static Stream<Arguments> prepareCallCases() {
        return Stream.of(
                prepareCallTestCase("prepareCall(String)", Connection::prepareCall),
                prepareCallTestCase("prepareCall(String, int, int)",
                        (connection, sql) -> connection.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY,
                                ResultSet.CONCUR_READ_ONLY)),
                prepareCallTestCase("prepareCall(String, int, int, int)",
                        (connection, sql) -> connection.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY,
                                ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)));
    }

    private static Arguments prepareCallTestCase(String description,
            ThrowingBiFunction<Connection, String, CallableStatement> prepare) {
        return Arguments.of(description, prepare);
    }

    private static Stream<Arguments> transactionManagementStatementsFailureCases() {
        return Stream.of(
                txFailureCase("commit", JaybirdErrorCodes.jb_commitStatementNotAllowed),
                txFailureCase("commit work", JaybirdErrorCodes.jb_commitStatementNotAllowed),
                txFailureCase("rollback", JaybirdErrorCodes.jb_rollbackStatementNotAllowed),
                txFailureCase("rollback work", JaybirdErrorCodes.jb_rollbackStatementNotAllowed),
                txFailureCase("set transaction", JaybirdErrorCodes.jb_setTransactionStatementNotAllowed),
                txFailureCase("SET TRANSACTION READ WRITE WAIT ISOLATION LEVEL SNAPSHOT",
                        JaybirdErrorCodes.jb_setTransactionStatementNotAllowed));
    }

    private static Arguments txFailureCase(String sql, Integer expectedErrorCode) {
        return Arguments.of(sql, expectedErrorCode);
    }

    private static Stream<Arguments> transactionManagementDisallowPrepareStatementCases() {
        return prepareStatementCases()
                .flatMap(prepareCase -> transactionManagementStatementsFailureCases()
                        .map(tmCase -> {
                            Object[] prepareCaseData = prepareCase.get();
                            Object[] tmCaseData = tmCase.get();
                            return Arguments.of(
                                    prepareCaseData[0] /* method description */,
                                    tmCaseData[0] /* sql */,
                                    prepareCaseData[1] /* prepareStatement method */,
                                    tmCaseData[1] /* expected error code */);
                        }));
    }

    private static Stream<String> transactionManagementStatements() {
        return transactionManagementStatementsFailureCases().map(arg -> (String) arg.get()[0]);
    }

    /**
     * Combines {@link #transactionManagementStatements()} with {@code true}/{@code false} for testing a statement both
     * with {@code allowTxStmts=true} and {@code allowTxStmts=false}.
     */
    private static Stream<Arguments> allowAndDisallowTransactionManagementStatements() {
        return transactionManagementStatements()
                .flatMap(stmt -> Stream.of(Arguments.of(stmt, true), Arguments.of(stmt, false)));
    }

    private static Stream<Arguments> transactionManagementAlwaysDisallowPrepareCallCases() {
        return prepareCallCases()
                .flatMap(prepareCase -> allowAndDisallowTransactionManagementStatements()
                        .map(tmCase -> {
                            Object[] prepareCaseData = prepareCase.get();
                            Object[] tmCaseData = tmCase.get();
                            return Arguments.of(
                                    prepareCaseData[0] /* method description */,
                                    tmCaseData[0] /* sql */,
                                    prepareCaseData[1] /* prepareCall method */,
                                    tmCaseData[1] /* txAllowStmts */);
                        }));
    }

}
