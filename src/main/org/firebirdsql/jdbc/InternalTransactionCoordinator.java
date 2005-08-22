package org.firebirdsql.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.resource.ResourceException;

import org.firebirdsql.jca.FBLocalTransaction;


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
        else
            return new Object();
    }

    public InternalTransactionCoordinator(AbstractTransactionCoordinator coordinator) {
        this.coordinator = coordinator;
    }
    
    public void executionStarted(AbstractStatement stmt) throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            coordinator.executionStarted(stmt);
        }
    }

    public AbstractConnection getConnection() throws SQLException {
        return coordinator.getConnection();
    }
    public void statementClosed(AbstractStatement stmt) throws SQLException {
        
        coordinator.statementClosed(stmt);
    }
    public void statementCompleted(AbstractStatement stmt) throws SQLException {
        Object syncObject = getSynchronizationObject();
        synchronized (syncObject) {
            coordinator.statementCompleted(stmt);
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
                this.coordinator.completeStatements();
                coordinator.setStatements(this.coordinator.getStatements());
            }
            
            this.coordinator = coordinator;
        }
    }
    
    public abstract static class AbstractTransactionCoordinator implements FBObjectListener.StatementListener, FBObjectListener.BlobListener {
        protected FBLocalTransaction localTransaction;
        protected AbstractConnection connection;
        
        protected Collection statements = new ArrayList();

        protected AbstractTransactionCoordinator(AbstractConnection connection, FBLocalTransaction localTransaction) {
            this.localTransaction = localTransaction;
            this.connection = connection;
        }

        /**
         * Get the connection which owns this coordinator.
         * 
         * @return instance of {@link Connection}
         */
        public AbstractConnection getConnection() throws SQLException {
            return connection;
        }
        
        protected Collection getStatements() {
            return statements;
        }
        
        protected void setStatements(Collection statements) {
            this.statements.addAll(statements);
        }
        protected void completeStatements() throws SQLException {
            SQLException resultEx = null;
            for (Iterator iter = statements.iterator(); iter.hasNext();) {
                AbstractStatement statement = (AbstractStatement) iter.next();
                try {
                    statement.completeStatement();
                } catch(SQLException ex) {
                    if (resultEx == null)
                        resultEx = ex;
                    else
                        resultEx.setNextException(ex);
                }
                iter.remove();
            }
            
            try {
                if (localTransaction.inTransaction())
                    localTransaction.commit();
            } catch(ResourceException ex) {
                throw new FBSQLException(ex);
            }
            
            if (resultEx != null)
                throw resultEx;
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
        public AutoCommitCoordinator(AbstractConnection connection,
                FBLocalTransaction localTransaction) {
            super(connection, localTransaction);
        }
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#executionStarted(java.sql.Statement)
         */
        public void executionStarted(AbstractStatement stmt) throws SQLException {

            ArrayList tempList = new ArrayList(statements);
            
            // complete all open statements for the connection
            // (there should be only one anyway)
            for (Iterator iter = tempList.iterator(); iter.hasNext();) {
                AbstractStatement tempStatement = (AbstractStatement) iter.next();
                
                // enable re-entrancy for the same statement
                if (tempStatement == stmt) {
                    iter.remove();
                    continue;
                }
                
                tempStatement.completeStatement();
            }

            statements.removeAll(tempList);
            
            if (statements.contains(stmt))
                return;
            
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
        public void statementClosed(AbstractStatement stmt) throws SQLException {
            stmt.completeStatement();
            connection.notifyStatementClosed(stmt);
        }
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementCompleted(java.sql.Statement)
         */
        public void statementCompleted(AbstractStatement stmt) throws SQLException {
            statements.remove(stmt);
            try {
                if (localTransaction.inTransaction())
                    localTransaction.commit();
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
            // do nothing, next line exists only for breakpoint
            int i = 0;
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
        public LocalTransactionCoordinator(AbstractConnection connection,
                FBLocalTransaction localTransaction) {
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
        public void executionStarted(AbstractStatement stmt) throws SQLException {
            ensureTransaction();
        }
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementClosed(java.sql.Statement)
         */
        public void statementClosed(AbstractStatement stmt) throws SQLException {
            stmt.completeStatement();
            connection.notifyStatementClosed(stmt);
        }
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementCompleted(java.sql.Statement)
         */
        public void statementCompleted(AbstractStatement stmt) throws SQLException {

        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.BlobListener#executionCompleted(org.firebirdsql.jdbc.FirebirdBlob)
         */
        public void executionCompleted(FirebirdBlob blob) throws SQLException {
            // empty
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
         * @param connection
         * @param localTransaction
         */
        public ManagedTransactionCoordinator(AbstractConnection connection) {
            super(connection, null);
        }
        
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator.AbstractTransactionCoordinator#completeStatements()
         */
        protected void completeStatements() throws SQLException {
            SQLException resultEx = null;
            for (Iterator iter = statements.iterator(); iter.hasNext();) {
                AbstractStatement statement = (AbstractStatement) iter.next();
                try {
                    statement.completeStatement();
                } catch(SQLException ex) {
                    if (resultEx == null)
                        resultEx = ex;
                    else
                        resultEx.setNextException(ex);
                }
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
        public void executionStarted(AbstractStatement stmt) throws SQLException {
            // NO TRANSACTION MANAGEMENT HERE - empty method
        }
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#commit()
         */
        public void commit() throws SQLException {
            throw new FBSQLException("Calling commit() in managed environment is not allowed.");
        }
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.InternalTransactionCoordinator#rollback()
         */
        public void rollback() throws SQLException {
            throw new FBSQLException("Calling rollback() in managed environment is not allowed.");
        }
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.BlobListener#executionStarted(org.firebirdsql.jdbc.FirebirdBlob)
         */
        public void executionStarted(FirebirdBlob blob) throws SQLException {
            // empty
        }
    }

    public static class MetaDataTransactionCoordinator extends AbstractTransactionCoordinator {
        
        private InternalTransactionCoordinator tc;
        
        public MetaDataTransactionCoordinator(InternalTransactionCoordinator tc) {
            super(tc.coordinator.connection, tc.coordinator.connection.getLocalTransaction());
            
            this.tc = tc;
        }
        
        /**
         * @param connection
         * @param localTransaction
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
        public void executionStarted(AbstractStatement stmt) throws SQLException {

            if (tc == null)
                return;

            tc.ensureTransaction();
        }
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementClosed(java.sql.Statement)
         */
        public void statementClosed(AbstractStatement stmt) throws SQLException {
            if (tc == null)
                return;
            
            stmt.completeStatement();
            tc.coordinator.connection.notifyStatementClosed(stmt);
        }
        /* (non-Javadoc)
         * @see org.firebirdsql.jdbc.FBObjectListener.StatementListener#statementCompleted(java.sql.Statement)
         */
        public void statementCompleted(AbstractStatement stmt) throws SQLException {
            if (!connection.getAutoCommit())
                return;
            
            
            // commit in case of auto-commit mode to end the transaction that we started
            try {
                if (localTransaction.inTransaction())
                    localTransaction.commit();
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
