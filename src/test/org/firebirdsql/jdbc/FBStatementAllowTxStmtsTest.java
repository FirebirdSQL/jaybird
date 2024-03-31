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
import org.firebirdsql.common.function.ThrowingBiConsumer;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientException;
import java.sql.Statement;
import java.util.Set;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNoNextRow;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.in;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for allowing or disallowing transaction management statements in {@link FBStatement}.
 */
@SuppressWarnings("SqlSourceToSinkFlow")
class FBStatementAllowTxStmtsTest {

    private static final String RECREATE_TABLE = "recreate table TEST (COL1 integer primary key)";
    private static final String INSERT_COL1_IS_1 = "insert into TEST (COL1) values (1)";
    private static final String SELECT_COL1_IS_1 = "select COL1 from TEST where COL1 = 1";
    private static final String SELECT_TRANSACTION = "select CURRENT_TRANSACTION from RDB$DATABASE";
    private static final String SELECT_TRANSACTION_CONFIG = """
            select MON$ISOLATION_MODE, MON$LOCK_TIMEOUT, MON$READ_ONLY, MON$AUTO_COMMIT, MON$AUTO_UNDO
            from MON$TRANSACTIONS
            where MON$TRANSACTION_ID = CURRENT_TRANSACTION""";

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
    @ParameterizedTest(name = "{0} - {1}")
    @MethodSource("transactionManagementDisallowExecutionCases")
    void executeMethods_disallowTransactionManagementStatements(String description, String sql,
            ThrowingBiConsumer<Statement, String> execute, int expectedErrorCode) throws Exception {
        try (var stmt = getConnection(false).createStatement()) {
            var exception = assertThrows(SQLNonTransientException.class, () -> execute.accept(stmt, sql));
            assertThat(exception, errorCodeEquals(expectedErrorCode));
        }
    }

    @ParameterizedTest
    @MethodSource("allowAndDisallowTransactionManagementStatements")
    void executeQuery_alwaysDisallowsTransactionManagementStatements(String sql, boolean allowTxStmts)
            throws Exception {
        try (var stmt = getConnection(allowTxStmts).createStatement()) {
            var exception = assertThrows(SQLNonTransientException.class, () -> stmt.executeQuery(sql));
            assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_executeQueryWithTxStmt));
        }
    }

    @ParameterizedTest
    @MethodSource("allowAndDisallowTransactionManagementStatements")
    void addBatch_alwaysDisallowsTransactionManagementStatements(String sql, boolean allowTxStmts) throws Exception {
        try (var stmt = getConnection(allowTxStmts).createStatement()) {
            var exception = assertThrows(SQLFeatureNotSupportedException.class, () -> stmt.addBatch(sql));
            assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_addBatchWithTxStmt));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("executeCases")
    void executeMethods_allowsCommit(String description, ThrowingBiConsumer<Statement, String> execute)
            throws Exception {
        Connection con = getConnection(true);
        try (var stmt = con.createStatement()) {
            stmt.execute(RECREATE_TABLE);
            con.setAutoCommit(false);
            stmt.execute(INSERT_COL1_IS_1);
            long currentTxId = getTransactionId(stmt);
            assertDoesNotThrow(() -> execute.accept(stmt, "commit"));
            // Expected a new transaction
            assertNotEquals(currentTxId, getTransactionId(stmt));
            try (var rs = stmt.executeQuery(SELECT_COL1_IS_1)) {
                assertNextRow(rs, "Expected data in TEST to have been committed");
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("executeCases")
    void executeMethods_allowsRollback(String description, ThrowingBiConsumer<Statement, String> execute)
            throws Exception {
        Connection con = getConnection(true);
        try (var stmt = con.createStatement()) {
            stmt.execute(RECREATE_TABLE);
            con.setAutoCommit(false);
            stmt.execute(INSERT_COL1_IS_1);
            long currentTxId = getTransactionId(stmt);
            assertDoesNotThrow(() -> execute.accept(stmt, "rollback"));
            // Expected a new transaction
            assertNotEquals(currentTxId, getTransactionId(stmt));
            try (var rs = stmt.executeQuery(SELECT_COL1_IS_1)) {
                // should have rolled back data
                assertNoNextRow(rs, "Expected data in TEST to have been rolled back");
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("executeCases")
    void executeMethods_allowsSetTransaction(String description, ThrowingBiConsumer<Statement, String> execute)
            throws Exception {
        Connection con = getConnection(true);
        try (var stmt = con.createStatement()) {
            con.setAutoCommit(false);
            assertDoesNotThrow(() -> execute.accept(stmt, "set transaction"));
            assertCurrentTransaction(stmt, IsolationLevel.SNAPSHOT, -1, false, false, true);
            con.commit();
            assertDoesNotThrow(() -> execute.accept(stmt,
                    "set transaction read only read committed wait lock timeout 500 no auto undo"));
            assertCurrentTransaction(stmt, IsolationLevel.READ_COMMITTED, 500, true, false, false);
            assertEquals(getTransactionId(stmt), getTransactionId(stmt), "Expected same transaction");
            con.commit();
            assertDoesNotThrow(() -> execute.accept(stmt,
                    "set transaction snapshot table stability reserving TEST for shared write"));
            assertCurrentTransaction(stmt, IsolationLevel.SNAPSHOT_TABLE_STABILITY, -1, false, false, true);

            if (FirebirdSupportInfo.supportInfoFor(con).isVersionEqualOrAbove(4)) {
                con.commit();
                assertDoesNotThrow(() -> execute.accept(stmt,
                        "set transaction read write read committed no wait auto commit"));
                assertCurrentTransaction(stmt, IsolationLevel.READ_COMMITTED, 0, false, true, true);
            }
        }
    }

    private static Stream<Arguments> executeCases() {
        return Stream.of(
                statementTestCase("execute(String)", Statement::execute),
                statementTestCase("execute(String,int)",
                        (stmt, sql) -> stmt.execute(sql, Statement.RETURN_GENERATED_KEYS)),
                statementTestCase("execute(String, int[])", (stmt, sql) -> stmt.execute(sql, new int[] { 1, 2 })),
                statementTestCase("execute(String, String[])",
                        (stmt, sql) -> stmt.execute(sql, new String[] { "IGNORED1", "IGNORED2" })),
                statementTestCase("executeUpdate(String)", Statement::executeUpdate),
                statementTestCase("executeLargeUpdate(String)", Statement::executeLargeUpdate),
                statementTestCase("executeUpdate(String, int)",
                        (stmt, sql) -> stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS)),
                statementTestCase("executeLargeUpdate(String, int)",
                        (stmt, sql) -> stmt.executeLargeUpdate(sql, Statement.RETURN_GENERATED_KEYS)),
                statementTestCase("executeUpdate(String, int[])",
                        (stmt, sql) -> stmt.executeUpdate(sql, new int[] { 1, 2 })),
                statementTestCase("executeLargeUpdate(String, int[])",
                        (stmt, sql) -> stmt.executeLargeUpdate(sql, new int[] { 1, 2 })),
                statementTestCase("executeUpdate(String, String[])",
                        (stmt, sql) -> stmt.executeUpdate(sql, new String[] { "IGNORED1", "IGNORED2" })),
                statementTestCase("executeLargeUpdate(String, String[])",
                        (stmt, sql) -> stmt.executeLargeUpdate(sql, new String[] { "IGNORED1", "IGNORED2" })));
    }

    private static Arguments statementTestCase(String description, ThrowingBiConsumer<Statement, String> executeCall) {
        return Arguments.of(description, executeCall);
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

    private static Stream<Arguments> transactionManagementDisallowExecutionCases() {
        return executeCases()
                .flatMap(executeCase -> transactionManagementStatementsFailureCases()
                        .map(tmCase -> {
                            Object[] executeCaseData = executeCase.get();
                            Object[] tmCaseData = tmCase.get();
                            return Arguments.of(
                                    executeCaseData[0] /* method description */,
                                    tmCaseData[0] /* sql */,
                                    executeCaseData[1] /* execute method */,
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

    static long getTransactionId(Statement stmt) throws SQLException {
        try (var rs = stmt.executeQuery(SELECT_TRANSACTION)) {
            assertNextRow(rs);
            return rs.getLong(1);
        }
    }

    static void assertCurrentTransaction(Statement stmt, IsolationLevel isolationLevel, int lockTimeout,
            boolean readOnly, boolean autoCommit, boolean autoUndo) throws SQLException {
        try (var rs = stmt.executeQuery(SELECT_TRANSACTION_CONFIG)) {
            assertNextRow(rs);
            isolationLevel.assertIsolationMode(rs.getInt(1));
            assertEquals(lockTimeout, rs.getInt(2));
            assertEquals(readOnly ? 1 : 0, rs.getInt(3));
            assertEquals(autoCommit ? 1 : 0, rs.getInt(4));
            assertEquals(autoUndo ? 1 : 0, rs.getInt(5));
        }
    }

    enum IsolationLevel {
        SNAPSHOT_TABLE_STABILITY {
            @Override
            void assertIsolationMode(int isolationMode) {
                assertEquals(MODE_SNAPSHOT_TABLE_STABILITY, isolationMode);
            }
        },
        SNAPSHOT {
            @Override
            void assertIsolationMode(int isolationMode) {
                assertEquals(MODE_SNAPSHOT, isolationMode);
            }
        },
        READ_COMMITTED {
            @Override
            void assertIsolationMode(int isolationMode) {
                // Not testing specific mode, as default Firebird settings always uses READ CONSISTENCY (if supported)
                assertThat(isolationMode, in(Set.of(
                        MODE_READ_COMMITTED_REC_VERSION,
                        MODE_READ_COMMITTED_NO_REC_VERSION,
                        MODE_READ_COMMITTED_READ_CONSISTENCY)));
            }
        };

        private static final int MODE_SNAPSHOT_TABLE_STABILITY = 0;
        private static final int MODE_SNAPSHOT = 1;
        private static final int MODE_READ_COMMITTED_REC_VERSION = 2;
        private static final int MODE_READ_COMMITTED_NO_REC_VERSION = 3;
        private static final int MODE_READ_COMMITTED_READ_CONSISTENCY = 4;

        /**
         * @param isolationMode
         *         value of the {@code RDB$ISOLATION_MODE} column to assert
         */
        abstract void assertIsolationMode(int isolationMode);
    }

}
