/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.resource.ResourceException;

import org.firebirdsql.jca.FirebirdLocalTransaction;
import org.firebirdsql.jdbc.FBStatement.CompletionReason;
import org.firebirdsql.util.SQLExceptionChainBuilder;

/**
 * Transaction coordinator for the {@link org.firebirdsql.jdbc.FBConnection} class.
 */
public class InternalTransactionCoordinator implements FBObjectListener.StatementListener, FBObjectListener.BlobListener, Synchronizable {

    private AbstractTransactionCoordinator coordinator;

    public InternalTransactionCoordinator() {
        // empty
    }
    
    public Object getSynchronizationObject() throws SQLException {
        if (coordinator instanceof AutoCommitCoordinator)
            return coordinator.getConnection();
        // TODO Suspicious using new sync-object every time
        return new Object();
    }

    public InternalTransactionCoordinator(AbstractTransactionCoordinator coordinator) {
        this.coordinator = coordinator;
    }
    
    public void executionStarted(FBStatement stmt) throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            coordinator.executionStarted(stmt);
        }
    }

    public FBConnection getConnection() throws SQLException {
        return coordinator.getConnection();
    }
    
    public void statementClosed(FBStatement stmt) throws SQLException {
        coordinator.statementClosed(stmt);
    }
    
    public void statementCompleted(FBStatement stmt) throws SQLException {
        statementCompleted(stmt, true);
    }
    
    public void statementCompleted(FBStatement stmt, boolean success) throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            coordinator.statementCompleted(stmt, success);
        }
    }

    public void executionCompleted(FirebirdBlob blob) throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            coordinator.executionCompleted(blob);
        }
    }
    
    public void executionStarted(FirebirdBlob blob) throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            coordinator.executionStarted(blob);
        }
    }
    
    public void ensureTransaction() throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            coordinator.ensureTransaction();
        }
    }
    
    public void commit() throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            coordinator.commit();
        }
    }
    
    public void rollback() throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            coordinator.rollback();
        }
    }
    
    public void setCoordinator(AbstractTransactionCoordinator coordinator) throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            if (this.coordinator != null) {
                this.coordinator.completeStatements(CompletionReason.COMMIT);
                coordinator.setStatements(this.coordinator.getStatements());
            }
            this.coordinator = coordinator;
        }
    }
    
    public abstract static class AbstractTransactionCoordinator implements FBObjectListener.StatementListener, FBObjectListener.BlobListener {
        protected FirebirdLocalTransaction localTransaction;
        protected FBConnection connection;
        
        protected Collection<FBStatement> statements = new ArrayList<FBStatement>();

        protected AbstractTransactionCoordinator(FBConnection connection, FirebirdLocalTransaction localTransaction) {
            this.localTransaction = localTransaction;
            this.connection = connection;
        }

        /**
         * Get the connection which owns this coordinator.
         * 
         * @return instance of {@link FBConnection}
         */
        public FBConnection getConnection() throws SQLException {
            return connection;
        }
        
        protected Collection<FBStatement> getStatements() {
            return statements;
        }
        
        protected void setStatements(Collection<FBStatement> statements) {
            this.statements.addAll(statements);
        }
        
        protected void completeStatements(CompletionReason reason) throws SQLException {
            SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<SQLException>();
            
            // we have to loop through the array, since the 
            // statement.completeStatement() call causes the 
            // ConcurrentModificationException
            Object[] statementsToComplete = statements.toArray(); 
            for (int i = 0; i < statementsToComplete.length; i++) {
                
                FBStatement statement = 
                    (FBStatement)statementsToComplete[i];
                
                try {
                    statement.completeStatement(reason);
                } catch(SQLException ex) {
                    chain.append(ex);
                }
            }
            
            // clear the statements (usually needed only for those that
            // were not removed in the statement.completeStatement() call
            statements.clear();
            
            try {
                if (localTransaction.inTransaction())
                    localTransaction.commit();
            } catch(ResourceException ex) {
                throw new FBSQLException(ex);
            }
            
            if (chain.hasException())
                throw chain.getException();
        }
        
        public abstract void ensureTransaction() throws SQLException;
        
        public abstract void commit() throws SQLException;
        
        public abstract void rollback() throws SQLException;
    }
    
    public static class AutoCommitCoordinator extends AbstractTransactionCoordinator {
        
        /**
         * @param connection
         * @param localTransaction
         */
        public AutoCommitCoordinator(FBConnection connection,
                FirebirdLocalTransaction localTransaction) {
            super(connection, localTransaction);
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#executionStarted(java.sql.Statement)
         */
        public void executionStarted(FBStatement stmt) throws SQLException {
            List<FBStatement> tempList = new ArrayList<FBStatement>(statements);
            SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<SQLException>();
            
            // complete all open statements for the connection
            // (there should be only one anyway)
            for (Iterator<FBStatement> iter = tempList.iterator(); iter.hasNext();) {
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
            
            if (!statements.contains(stmt))
                statements.add(stmt);
            
            ensureTransaction();
        }

        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator.AbstractTransactionCoordinator#ensureTransaction()
         */
        public void ensureTransaction() throws SQLException {
            try {
                if (!localTransaction.inTransaction())
                    localTransaction.begin();
            } catch(ResourceException ex) {
                throw new FBSQLException(ex);
            }
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementClosed(java.sql.Statement)
         */
        public void statementClosed(FBStatement stmt) throws SQLException {
            stmt.completeStatement();
            connection.notifyStatementClosed(stmt);
        }
        
        public void statementCompleted(FBStatement stmt) throws SQLException {
            statementCompleted(stmt, true);
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementCompleted(java.sql.Statement)
         */
        public void statementCompleted(FBStatement stmt, boolean success) throws SQLException {
            statements.remove(stmt);
            try {
                if (!localTransaction.inTransaction())
                    return;
                
                if (success)
                    localTransaction.commit();
                else
                    localTransaction.rollback();
                
            } catch(ResourceException ex) {
                
                try {
                    if (localTransaction.inTransaction())
                        localTransaction.rollback();
                } catch(ResourceException ex1) {
                    throw new FBSQLException(ex1);
                }
                
                throw new FBSQLException(ex);
            }
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.BlobListener#executionCompleted(org.firebirdsql.jdbc.FirebirdBlob)
         */
        public void executionCompleted(FirebirdBlob blob) throws SQLException {

        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.BlobListener#executionStarted(org.firebirdsql.jdbc.FirebirdBlob)
         */
        public void executionStarted(FirebirdBlob blob) throws SQLException {
            ensureTransaction();
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#commit()
         */
        public void commit() throws SQLException {
            throw new FBSQLException(
                    "Calling commit() in auto-commit mode is not allowed.");
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#rollback()
         */
        public void rollback() throws SQLException {
            throw new FBSQLException(
                "Calling rollback() in auto-commit mode is not allowed.");
        }
    }
    
    public static class LocalTransactionCoordinator extends AbstractTransactionCoordinator {
        
        /**
         * @param connection
         * @param localTransaction
         */
        public LocalTransactionCoordinator(FBConnection connection,
                FirebirdLocalTransaction localTransaction) {
            super(connection, localTransaction);
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator.AbstractTransactionCoordinator#ensureTransaction()
         */
        public void ensureTransaction() throws SQLException {
            try {
                if (!localTransaction.inTransaction())
                    localTransaction.begin();
            } catch(ResourceException ex) {
                throw new FBSQLException(ex);
            }
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#commit()
         */
        public void commit() throws SQLException {
            try {
                if (localTransaction.inTransaction())
                    localTransaction.commit();
            } catch(ResourceException ex) {
                throw new FBSQLException(ex);
            }
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#rollback()
         */
        public void rollback() throws SQLException {
            try {
                if (localTransaction.inTransaction())
                    localTransaction.rollback();
            } catch(ResourceException ex) {
                throw new FBSQLException(ex);
            }
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#executionStarted(java.sql.Statement)
         */
        public void executionStarted(FBStatement stmt) throws SQLException {
            ensureTransaction();
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementClosed(java.sql.Statement)
         */
        public void statementClosed(FBStatement stmt) throws SQLException {
            stmt.completeStatement();
            connection.notifyStatementClosed(stmt);
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementCompleted(java.sql.Statement)
         */
        public void statementCompleted(FBStatement stmt) throws SQLException {

        }
        
        public void statementCompleted(FBStatement stmt, boolean success) throws SQLException {
            
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.BlobListener#executionCompleted(org.firebirdsql.jdbc.FirebirdBlob)
         */
        public void executionCompleted(FirebirdBlob blob) throws SQLException {

        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.BlobListener#executionStarted(org.firebirdsql.jdbc.FirebirdBlob)
         */
        public void executionStarted(FirebirdBlob blob) throws SQLException {
            ensureTransaction();
        }
    }
    
    public static class ManagedTransactionCoordinator extends LocalTransactionCoordinator {
        
        /**
         * Create instance of this class for the specified connection.
         * 
         * @param connection connection to coordinate.
         */
        public ManagedTransactionCoordinator(FBConnection connection) {
            super(connection, null);
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator.AbstractTransactionCoordinator#completeStatements()
         */
        protected void completeStatements() throws SQLException {
            SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<SQLException>();

            // we have to loop through the array, since the 
            // statement.completeStatement() call causes the 
            // ConcurrentModificationException
            Object[] statementsToComplete = statements.toArray(); 
            for (int i = 0; i < statementsToComplete.length; i++) {
                
                FBStatement statement = 
                    (FBStatement)statementsToComplete[i];

                try {
                    statement.completeStatement();
                } catch(SQLException ex) {
                    chain.append(ex);
                }
            }
            
            statements.clear();
            
            if (chain.hasException()) {
                throw chain.getException();
            }
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator.AbstractTransactionCoordinator#ensureTransaction()
         */
        public void ensureTransaction() throws SQLException {
            // do nothing, we are in managed environment.
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#executionStarted(java.sql.Statement)
         */
        public void executionStarted(FBStatement stmt) throws SQLException {
            // NO TRANSACTION MANAGEMENT HERE - empty method
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#commit()
         */
        public void commit() throws SQLException {
            // do nothing, we are in managed environment.
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#rollback()
         */
        public void rollback() throws SQLException {
            // do nothing, we are in managed environment.
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.BlobListener#executionStarted(org.firebirdsql.jdbc.FirebirdBlob)
         */
        public void executionStarted(FirebirdBlob blob) throws SQLException {

        }
    }

    public static class MetaDataTransactionCoordinator extends AbstractTransactionCoordinator {
        
        private InternalTransactionCoordinator tc;
        
        public MetaDataTransactionCoordinator(InternalTransactionCoordinator tc) {
            super(tc.coordinator.connection, tc.coordinator.connection.getLocalTransaction());
            
            this.tc = tc;
        }
        
        /**
         * Create coordinator for the unspecified connection. 
         */
        public MetaDataTransactionCoordinator() {
            super(null, null);
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator.AbstractTransactionCoordinator#ensureTransaction()
         */
        public void ensureTransaction() throws SQLException {
            throw new UnsupportedOperationException();
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#commit()
         */
        public void commit() throws SQLException {
            throw new UnsupportedOperationException();
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#rollback()
         */
        public void rollback() throws SQLException {
            throw new UnsupportedOperationException();
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#executionStarted(java.sql.Statement)
         */
        public void executionStarted(FBStatement stmt) throws SQLException {
            if (tc == null)
                return;

            tc.ensureTransaction();
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementClosed(java.sql.Statement)
         */
        public void statementClosed(FBStatement stmt) throws SQLException {
            if (tc == null)
                return;
            
            stmt.completeStatement();
            tc.coordinator.connection.notifyStatementClosed(stmt);
        }
        
        public void statementCompleted(FBStatement stmt) throws SQLException {
            statementCompleted(stmt, true);
        }

        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementCompleted(java.sql.Statement)
         */
        public void statementCompleted(FBStatement stmt, boolean success) throws SQLException {
            if (connection == null)
                return;
            
            if (!connection.getAutoCommit())
                return;
            
            // commit in case of auto-commit mode to end the transaction that we started
            try {
                if (!localTransaction.inTransaction())
                    return;
                
                if (success)
                    localTransaction.commit();
                else
                    localTransaction.rollback();
                
            } catch(ResourceException ex) {
                
                try {
                    if (localTransaction.inTransaction())
                        localTransaction.rollback();
                } catch(ResourceException ex1) {
                    throw new FBSQLException(ex1);
                }
                
                throw new FBSQLException(ex);
            }
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.BlobListener#executionCompleted(org.firebirdsql.jdbc.FirebirdBlob)
         */
        public void executionCompleted(FirebirdBlob blob) throws SQLException {

        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.BlobListener#executionStarted(org.firebirdsql.jdbc.FirebirdBlob)
         */
        public void executionStarted(FirebirdBlob blob) throws SQLException {

        }
    }
}
