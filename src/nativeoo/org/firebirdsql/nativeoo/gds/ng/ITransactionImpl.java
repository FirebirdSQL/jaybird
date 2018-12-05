package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ng.AbstractFbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbTransaction} for native client access using OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class ITransactionImpl extends AbstractFbTransaction {

    private static final Logger log = LoggerFactory.getLogger(ITransactionImpl.class);

    private final FbClientLibrary clientLibrary;
    private final ITransaction transaction;
    private final IStatus status;

    public ITransactionImpl(IDatabaseImpl database, ITransaction iTransaction, TransactionState initialState) {
        super(initialState, database);
        transaction = iTransaction;
        clientLibrary = database.getClientLibrary();
        status = database.getStatus();
    }

    @Override
    public IDatabaseImpl getDatabase() {
        return (IDatabaseImpl)super.getDatabase();
    }

    @Override
    public int getHandle() {
        throw new UnsupportedOperationException( "Native OO API not support transaction handle" );
    }

    public ITransaction getTransaction() {
        return transaction;
    }

    @Override
    public void commit() throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                final IDatabaseImpl db = getDatabase();
                db.checkConnected();
                switchState(TransactionState.COMMITTING);
                transaction.commit(getStatus());
                processStatus();
                switchState(TransactionState.COMMITTED);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            final TransactionState transactionState = getState();
            if (transactionState != TransactionState.COMMITTED) {
                log.warn("Commit not completed, state was " + transactionState,
                        new RuntimeException("Commit not completed"));
            }
        }
    }

    @Override
    public void rollback() throws SQLException {
        try {
            synchronized (getSynchronizationObject()) {
                final IDatabaseImpl db = getDatabase();
                db.checkConnected();
                switchState(TransactionState.ROLLING_BACK);
                transaction.rollback(getStatus());
                processStatus();
                switchState(TransactionState.ROLLED_BACK);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            final TransactionState transactionState = getState();
            if (transactionState != TransactionState.ROLLED_BACK) {
                log.warn("Rollback not completed, state was " + transactionState,
                        new RuntimeException("Rollback not completed"));
            }
        }
    }

    @Override
    public void prepare(byte[] recoveryInformation) throws SQLException {
        boolean noRecoveryInfo = recoveryInformation == null || recoveryInformation.length == 0;
        try {
            synchronized (getSynchronizationObject()) {
                final IDatabaseImpl db = getDatabase();
                db.checkConnected();
                switchState(TransactionState.PREPARING);
                if (noRecoveryInfo) {
                    // TODO check for recovery information
                    transaction.prepare(getStatus(), 0,
                            null);
                } else {
                    transaction.prepare(getStatus(), (short) recoveryInformation.length,
                            recoveryInformation);
                }
                processStatus();
                switchState(TransactionState.PREPARED);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            if (getState() != TransactionState.PREPARED) {
                log.warn("Prepare not completed", new RuntimeException("Prepare not completed"));
            }
        }
    }

    @Override
    public byte[] getTransactionInfo(byte[] requestItems, int maxBufferLength) throws SQLException {
        try {
            final byte[] responseArray = new byte[maxBufferLength];
            synchronized (getSynchronizationObject()) {
                final IDatabaseImpl db = getDatabase();
                db.checkConnected();
                transaction.getInfo(getStatus(), (short) requestItems.length, requestItems,
                        (short) maxBufferLength, responseArray);
                processStatus();
            }
            return responseArray;
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    private IStatus getStatus() {
        status.init();
        return status;
    }

    private void processStatus() throws SQLException {
        getDatabase().processStatus(status, null);
    }
}
