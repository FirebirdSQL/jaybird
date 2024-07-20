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

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.StatementType;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;
import org.firebirdsql.jaybird.xca.FBLocalTransaction;
import org.firebirdsql.jaybird.xca.FBManagedConnection;
import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_GENERAL_ERROR;

/**
 * Transaction coordinator for the {@link org.firebirdsql.jdbc.FBConnection} class.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * </p>
 */
@InternalApi
public final class InternalTransactionCoordinator implements FBObjectListener.StatementListener,
        FBObjectListener.BlobListener {

    private final FBConnection connection;
    private AbstractTransactionCoordinator coordinator;

    InternalTransactionCoordinator(FBConnection connection) {
        this.connection = requireNonNull(connection, "connection");
    }

    private LockCloseable withLock() {
        return connection.withLock();
    }

    /**
     * Switches the auto commit coordinator if currently in a different auto commit mode.
     *
     * @param autoCommit
     *         Value for auto commit
     * @throws SQLException
     *         If {@code autoCommit} is {@code true} and the connection is currently enlisted in a
     *         distributed transaction
     */
    void switchTransactionCoordinator(boolean autoCommit) throws SQLException {
        if (coordinator != null && getAutoCommit() == autoCommit) {
            return;
        }

        setTransactionCoordinator(false, autoCommit);
    }

    /**
     * Sets the transaction coordinator.
     * <p>
     * In most cases {@link #switchTransactionCoordinator(boolean)} should be called.
     * </p>
     *
     * @param managedConnection
     *         Whether this is a managed connection
     * @param autoCommit
     *         Value for auto commit
     * @throws SQLException
     *         If {@code autoCommit} is {@code true}, and {@code managedConnection} is {@code false} and
     *         the connection is currently enlisted in a distributed transaction
     */
    void setTransactionCoordinator(boolean managedConnection, boolean autoCommit) throws SQLException {
        FBManagedConnection mc = connection.getManagedConnection();
        InternalTransactionCoordinator.AbstractTransactionCoordinator coordinator;
        if (managedConnection && mc.inDistributedTransaction()) {
            coordinator = new ManagedTransactionCoordinator(connection);
        } else if (autoCommit) {
            if (mc.inDistributedTransaction()) {
                throw new SQLException("Connection enlisted in distributed transaction",
                        SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }
            if (connection.isUseFirebirdAutoCommit()) {
                coordinator = new FirebirdAutoCommitCoordinator(connection, connection.getLocalTransaction());
            } else {
                coordinator = new AutoCommitCoordinator(connection, connection.getLocalTransaction());
            }
        } else {
            coordinator = new LocalTransactionCoordinator(connection, connection.getLocalTransaction());
        }
        setCoordinator(coordinator);
    }

    public boolean getAutoCommit() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            return coordinator != null && coordinator.isAutoCommit();
        }
    }

    @Override
    public void executionStarted(FBStatement stmt) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            coordinator.executionStarted(stmt);
        }
    }

    @Override
    public FBConnection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public void statementClosed(FBStatement stmt) throws SQLException {
        coordinator.statementClosed(stmt);
    }

    @Override
    public void statementCompleted(FBStatement stmt) throws SQLException {
        statementCompleted(stmt, true);
    }

    @Override
    public void statementCompleted(FBStatement stmt, boolean success) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            coordinator.statementCompleted(stmt, success);
        }
    }

    @Override
    public void executionCompleted(FirebirdBlob blob) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            coordinator.executionCompleted(blob);
        }
    }

    @Override
    public void executionStarted(FirebirdBlob blob) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            coordinator.executionStarted(blob);
        }
    }

    public void ensureTransaction() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            coordinator.ensureTransaction();
        }
    }

    /**
     * Starts a new transaction using {@code setTransactionSql}.
     *
     * @param sql
     *         the {@code SET TRANSACTION} statement to execute
     * @throws SQLException
     *         if executing transaction management statements is not allowed ({@code allowTxStmts=false}),
     *         the connection is in auto-commit mode, or if a transaction is currently active (including distributed
     *         transactions)
     * @since 6
     */
    void startSqlTransaction(String sql) throws SQLException {
        if (!connection.isAllowTxStmts()) {
            throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_setTransactionStatementNotAllowed)
                    .toSQLException();
        }
        try (LockCloseable ignored = withLock()) {
            coordinator.startSqlTransaction(sql);
        }
    }

    public void commit() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            coordinator.commit();
        }
    }

    public void rollback() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            coordinator.rollback();
        }
    }

    void handleConnectionClose() throws SQLException {
        coordinator.handleConnectionClose();
    }

    void handleConnectionAbort() throws SQLException {
        coordinator.handleConnectionAbort();
    }

    private void setCoordinator(AbstractTransactionCoordinator coordinator) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (this.coordinator != null) {
                var chain = new SQLExceptionChainBuilder();
                try {
                    this.coordinator.completeStatements(CompletionReason.COMMIT);
                } catch (SQLException ex) {
                    chain.append(ex);
                } finally {
                    try {
                        this.coordinator.internalCommit();
                    } catch (SQLException ex) {
                        chain.append(ex);
                    }
                }

                if (chain.hasException()) {
                    throw chain.getException();
                }

                coordinator.setStatements(this.coordinator.getStatements());
            }

            this.coordinator = coordinator;
        }
    }

    /**
     * Transaction coordinator base implementation.
     * <p>
     * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
     * </p>
     */
    public abstract static class AbstractTransactionCoordinator implements FBObjectListener.StatementListener,
            FBObjectListener.BlobListener {
        protected final FBLocalTransaction localTransaction;
        protected final FBConnection connection;

        protected final Collection<FBStatement> statements = new HashSet<>();

        protected AbstractTransactionCoordinator(FBConnection connection, FBLocalTransaction localTransaction) {
            this.localTransaction = localTransaction;
            this.connection = connection;
        }

        /**
         * Get the connection which owns this coordinator.
         *
         * @return instance of {@link FBConnection}
         */
        @Override
        public final FBConnection getConnection() throws SQLException {
            return connection;
        }

        protected final Collection<FBStatement> getStatements() {
            return statements;
        }

        protected final void setStatements(Collection<FBStatement> statements) {
            this.statements.addAll(statements);
        }

        protected void completeStatements(CompletionReason reason) throws SQLException {
            var chain = new SQLExceptionChainBuilder();

            // we have to loop through the array, since the statement.completeStatement() call causes
            // ConcurrentModificationException
            FBStatement[] statementsToComplete = statements.toArray(new FBStatement[0]);
            for (FBStatement statement : statementsToComplete) {
                try {
                    statement.completeStatement(reason);
                } catch (SQLException ex) {
                    chain.append(ex);
                }
            }

            // clear the statements (usually needed only for those that
            // were not removed in the statement.completeStatement() call
            statements.clear();

            if (chain.hasException()) {
                throw chain.getException();
            }
        }

        final void internalCommit() throws SQLException {
            if (localTransaction != null && localTransaction.inTransaction()) {
                localTransaction.commit();
            }
        }

        final void internalRollback() throws SQLException {
            if (localTransaction != null && localTransaction.inTransaction()) {
                localTransaction.rollback();
            }
        }

        public void ensureTransaction() throws SQLException {
            configureFirebirdAutoCommit();
            configureReadOnly();
            if (!localTransaction.inTransaction()) {
                localTransaction.begin();
            }
        }

        private void configureFirebirdAutoCommit() throws SQLException {
            // Handle Firebird autocommit support
            if (connection.isUseFirebirdAutoCommit()) {
                connection.getManagedConnection().setTpbAutoCommit(connection.getAutoCommit());
            }
        }

        private void configureReadOnly() throws SQLException {
            connection.getManagedConnection().setTpbReadOnly(connection.isReadOnly());
        }

        public abstract void commit() throws SQLException;

        public abstract void rollback() throws SQLException;

        abstract void handleConnectionClose() throws SQLException;

        void handleConnectionAbort() throws SQLException {
            completeStatements(CompletionReason.CONNECTION_ABORT);
        }

        boolean isAutoCommit() throws SQLException {
            return false;
        }

        /**
         * @see InternalTransactionCoordinator#startSqlTransaction(String)
         */
        abstract void startSqlTransaction(String setTransactionSql) throws SQLException;

        @Override
        public final void statementCompleted(FBStatement stmt) throws SQLException {
            statementCompleted(stmt, true);
        }

    }

    static final class AutoCommitCoordinator extends AbstractTransactionCoordinator {

        public AutoCommitCoordinator(FBConnection connection, FBLocalTransaction localTransaction) {
            super(connection, localTransaction);
        }

        @Override
        @SuppressWarnings("resource")
        public void executionStarted(FBStatement stmt) throws SQLException {
            List<FBStatement> tempList = new ArrayList<>(statements);
            var chain = new SQLExceptionChainBuilder();

            // complete all open statements for the connection (there should be only one anyway)
            for (Iterator<FBStatement> iter = tempList.iterator(); iter.hasNext(); ) {
                FBStatement tempStatement = iter.next();

                // enable re-entrancy for the same statement
                if (tempStatement == stmt) {
                    iter.remove();
                    continue;
                }
                // Autocommit, so reason of completion is COMMIT
                try {
                    tempStatement.completeStatement(CompletionReason.COMMIT);
                } catch (SQLException e) {
                    chain.append(e);
                }
            }

            statements.removeAll(tempList);
            if (chain.hasException()) {
                throw chain.getException();
            }

            if (!statements.contains(stmt)) {
                statements.add(stmt);
            }

            ensureTransaction();
        }

        @Override
        public void statementClosed(FBStatement stmt) throws SQLException {
            stmt.completeStatement();
            connection.notifyStatementClosed(stmt);
        }

        @Override
        public void statementCompleted(FBStatement stmt, boolean success) throws SQLException {
            statements.remove(stmt);
            try {
                if (!localTransaction.inTransaction()) {
                    return;
                }

                if (success) {
                    localTransaction.commit();
                } else {
                    localTransaction.rollback();
                }
            } catch (SQLException ex) {
                try {
                    internalRollback();
                } catch (SQLException ex2) {
                    ex.setNextException(ex2);
                }

                throw ex;
            }
        }

        @Override
        public void executionStarted(FirebirdBlob blob) throws SQLException {
            ensureTransaction();
        }

        @Override
        public void commit() throws SQLException {
            throw new SQLException("Calling commit() in auto-commit mode is not allowed.", SQL_STATE_GENERAL_ERROR);
        }

        @Override
        public void rollback() throws SQLException {
            throw new SQLException("Calling rollback() in auto-commit mode is not allowed.", SQL_STATE_GENERAL_ERROR);
        }

        @Override
        void handleConnectionClose() throws SQLException {
            if (localTransaction.inTransaction()) {
                try {
                    completeStatements(CompletionReason.COMMIT);
                } finally {
                    internalCommit();
                }
            }
        }

        @Override
        boolean isAutoCommit() {
            return true;
        }

        @Override
        void startSqlTransaction(String setTransactionSql) throws SQLException {
            throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_setTransactionNotAllowedInAutoCommit)
                    .toSQLException();
        }
    }

    static sealed class LocalTransactionCoordinator extends AbstractTransactionCoordinator {

        public LocalTransactionCoordinator(FBConnection connection, FBLocalTransaction localTransaction) {
            super(connection, localTransaction);
        }

        @Override
        public void commit() throws SQLException {
            try {
                completeStatements(CompletionReason.COMMIT);
            } finally {
                internalCommit();
            }
        }

        @Override
        public void rollback() throws SQLException {
            try {
                completeStatements(CompletionReason.ROLLBACK);
            } finally {
                internalRollback();
            }
        }

        @Override
        void handleConnectionClose() throws SQLException {
            if (localTransaction.inTransaction()) {
                rollback();
            }
        }

        @Override
        public void executionStarted(FBStatement stmt) throws SQLException {
            if (!statements.contains(stmt)) {
                statements.add(stmt);
            }
            ensureTransaction();
        }

        @Override
        public void statementClosed(FBStatement stmt) throws SQLException {
            stmt.completeStatement();
            connection.notifyStatementClosed(stmt);
        }

        @Override
        public void statementCompleted(FBStatement stmt, boolean success) throws SQLException {
            statements.remove(stmt);
        }

        @Override
        public void executionStarted(FirebirdBlob blob) throws SQLException {
            ensureTransaction();
        }

        @Override
        void startSqlTransaction(String sql) throws SQLException {
            localTransaction.begin(sql);
        }
    }

    static final class FirebirdAutoCommitCoordinator extends LocalTransactionCoordinator {

        public FirebirdAutoCommitCoordinator(FBConnection connection, FBLocalTransaction localTransaction) {
            super(connection, localTransaction);
        }

        @Override
        @SuppressWarnings("resource")
        public void executionStarted(FBStatement stmt) throws SQLException {
            List<FBStatement> tempList = new ArrayList<>(statements);
            var chain = new SQLExceptionChainBuilder();

            // complete all open statements for the connection (there should be only one anyway)
            for (Iterator<FBStatement> iter = tempList.iterator(); iter.hasNext(); ) {
                FBStatement tempStatement = iter.next();

                // enable re-entrancy for the same statement
                if (tempStatement == stmt) {
                    iter.remove();
                    continue;
                }
                // Firebird Autocommit doesn't really commit, but we use CompletionReason.COMMIT to get desired behavior
                // for holdable result sets, and the performance win of not having a server roundtrip to close
                // the cursor. This might lead to a slight cursor 'leak' until statement or connection close
                try {
                    tempStatement.completeStatement(CompletionReason.COMMIT);
                } catch (SQLException e) {
                    chain.append(e);
                }
            }

            statements.removeAll(tempList);
            if (chain.hasException()) {
                throw chain.getException();
            }

            if (!statements.contains(stmt)) {
                statements.add(stmt);
            }

            ensureTransaction();
        }

        @Override
        public void statementCompleted(FBStatement stmt, boolean success) throws SQLException {
            statements.remove(stmt);

            if (stmt.getStatementType() == StatementType.DDL.getStatementTypeCode()) {
                try {
                    if (!localTransaction.inTransaction()) {
                        return;
                    }

                    if (success) {
                        localTransaction.commit();
                    } else {
                        localTransaction.rollback();
                    }
                } catch (SQLException ex) {
                    try {
                        internalRollback();
                    } catch (SQLException ex2) {
                        ex.setNextException(ex2);
                    }

                    throw ex;
                }
            }
        }

        @Override
        public void commit() throws SQLException {
            throw new SQLException("Calling commit() in auto-commit mode is not allowed.", SQL_STATE_GENERAL_ERROR);
        }

        @Override
        public void rollback() throws SQLException {
            throw new SQLException("Calling rollback() in auto-commit mode is not allowed.", SQL_STATE_GENERAL_ERROR);
        }

        @Override
        void handleConnectionClose() throws SQLException {
            if (localTransaction.inTransaction()) {
                try {
                    completeStatements(CompletionReason.COMMIT);
                } finally {
                    internalCommit();
                }
            }
        }

        @Override
        boolean isAutoCommit() {
            return true;
        }

        @Override
        void startSqlTransaction(String sql) throws SQLException {
            throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_setTransactionNotAllowedInAutoCommit)
                    .toSQLException();
        }
    }

    static final class ManagedTransactionCoordinator extends LocalTransactionCoordinator {

        /**
         * Create instance of this class for the specified connection.
         *
         * @param connection
         *         connection to coordinate.
         */
        public ManagedTransactionCoordinator(FBConnection connection) {
            super(connection, null);
        }

        @Override
        public void ensureTransaction() {
            // do nothing, we are in managed environment.
        }

        @Override
        public void executionStarted(FBStatement stmt) {
            // NO TRANSACTION MANAGEMENT HERE - empty method
        }

        @Override
        public void commit() throws SQLException {
            // do nothing, we are in managed environment.
        }

        @Override
        public void rollback() throws SQLException {
            // do nothing, we are in managed environment.
        }

        @Override
        public void executionStarted(FirebirdBlob blob) {
            // nothing to do
        }

        @Override
        void handleConnectionClose() {
            // do nothing, we are in a managed environment
        }

        @Override
        void startSqlTransaction(String sql) throws SQLException {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_setTransactionNotAllowedActiveTx)
                    .toSQLException();
        }
        
    }

    static final class MetaDataTransactionCoordinator extends AbstractTransactionCoordinator {

        private final InternalTransactionCoordinator tc;

        public MetaDataTransactionCoordinator(InternalTransactionCoordinator tc) {
            super(tc.connection, tc.connection.getLocalTransaction());
            this.tc = tc;
        }

        @Override
        public void ensureTransaction() {
            throw new UnsupportedOperationException("ensureTransaction");
        }

        @Override
        public void commit() throws SQLException {
            throw new UnsupportedOperationException("commit");
        }

        @Override
        public void rollback() throws SQLException {
            throw new UnsupportedOperationException("rollback");
        }

        @Override
        void handleConnectionClose() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void executionStarted(FBStatement stmt) throws SQLException {
            tc.ensureTransaction();
        }

        @Override
        public void statementClosed(FBStatement stmt) throws SQLException {
            stmt.completeStatement();
        }

        @Override
        public void statementCompleted(FBStatement stmt, boolean success) {
            // nothing to do
        }

        @Override
        boolean isAutoCommit() throws SQLException {
            return tc.getAutoCommit();
        }

        @Override
        void startSqlTransaction(String setTransactionSql) {
            throw new UnsupportedOperationException("startSqlTransaction");
        }
    }
}
