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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLWarning;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbBlob implements FbBlob, TransactionListener, DatabaseListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractFbBlob.class, false);

    private final Object syncObject = new Object();
    private FbTransaction transaction;
    private FbDatabase database;
    private long blobId;
    private final boolean output;
    private boolean open;
    private int blobHandle;
    private boolean eof;

    protected AbstractFbBlob(FbDatabase database, FbTransaction transaction, long blobId, boolean output) {
        assert output ^ blobId != 0 : (output ? "Output blob should have blobId 0" : "input blob should have blobId non-zero");
        this.database = database;
        this.transaction = transaction;
        this.blobId = blobId;
        this.output = output;
        transaction.addTransactionListener(this);
        database.addDatabaseListener(this);
    }

    @Override
    public final long getBlobId() {
        return blobId;
    }

    /**
     * Sets the blob id.
     *
     * @param blobId
     *         Blob id.
     * @throws SQLException
     *         If this is an input blob, or if this is an output blob whose blobId was already set.
     */
    protected final void setBlobId(long blobId) throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (!isOutput() || getBlobId() != 0) {
                // TODO SQL State
                throw new SQLNonTransientException(isOutput() ? "The blob id is already set" : "Attempt to set the blob id of an input blob");
            }
            this.blobId = blobId;
        }
    }

    @Override
    public final int getHandle() {
        return blobHandle;
    }

    /**
     * @param blobHandle
     *         The Firebird blob handle identifier
     */
    protected final void setHandle(int blobHandle) {
        synchronized (getSynchronizationObject()) {
            this.blobHandle = blobHandle;
        }
    }

    @Override
    public final boolean isOutput() {
        return output;
    }

    @Override
    public final boolean isOpen() {
        synchronized (getSynchronizationObject()) {
            return open;
        }
    }

    @Override
    public final boolean isEof() {
        synchronized (getSynchronizationObject()) {
            return eof || !isOpen();
        }
    }

    /**
     * Marks this blob as EOF (End of file).
     * <p>
     * For an output blob this is a no-op (as those are never end of file, unless explicitly closed)
     * </p>
     */
    protected final void setEof() {
        if (isOutput()) return;
        synchronized (getSynchronizationObject()) {
            // TODO Can stream blobs be 'reopened' using seek?
            eof = true;
        }
    }

    protected final void setOpen(boolean open) {
        synchronized (getSynchronizationObject()) {
            this.open = open;
        }
    }

    public final void close() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (!isOpen()) return;
            checkDatabaseAttached();
            checkTransactionActive();
            try {
                closeImpl();
            } finally {
                setOpen(false);
            }
        }
    }

    /**
     * Internal implementation of {@link #close()}. The implementation does not need
     * to check for attached database and active transaction, nor does it need to mark this blob as closed.
     */
    protected abstract void closeImpl() throws SQLException;

    public final void cancel() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkDatabaseAttached();
            checkTransactionActive();
            try {
                cancelImpl();
            } finally {
                setOpen(false);
            }
        }
    }

    /**
     * Internal implementation of {@link #cancel()}. The implementation does not need
     * to check for attached database and active transaction, nor does it need to mark this blob as closed.
     */
    protected abstract void cancelImpl() throws SQLException;

    @Override
    public final Object getSynchronizationObject() {
        return syncObject;
    }

    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState, TransactionState previousState) {
        if (getTransaction() != transaction) {
            transaction.removeTransactionListener(this);
        }
        if (newState == TransactionState.NO_TRANSACTION) {
            synchronized (getSynchronizationObject()) {
                clearTransaction();
                setOpen(false);
            }
        }
        // TODO Need additional handling for other transitions?
    }

    @Override
    public void detaching(FbDatabase database) {
        synchronized (getSynchronizationObject()) {
            if (isOpen() && this.database == database) {
                log.debug(String.format("blob with blobId %d still open on database detach", getBlobId()));
                try {
                    close();
                } catch (SQLException e) {
                    log.error("Blob close in detaching event failed", e);
                }
            }
        }
    }

    @Override
    public void detached(FbDatabase database) {
        synchronized (getSynchronizationObject()) {
            if (this.database == database) {
                open = false;
                clearDatabase();
                clearTransaction();
            }
            database.removeDatabaseListener(this);
        }
    }

    @Override
    public void warningReceived(FbDatabase database, SQLWarning warning) {
        // Do nothing
    }

    /**
     * @throws java.sql.SQLException
     *         When no transaction is set, or the transaction state is not {@link TransactionState#ACTIVE}
     */
    protected void checkTransactionActive() throws SQLException {
        synchronized (getSynchronizationObject()) {
            FbTransaction transaction = getTransaction();
            if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
                throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_no_trans).toSQLException();
            }
        }
    }

    /**
     * @throws SQLException
     *         When no database is set, or the database is not attached
     */
    protected void checkDatabaseAttached() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (database == null || !database.isAttached()) {
                throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_segstr_wrong_db).toSQLException();
            }
        }
    }

    protected FbTransaction getTransaction() {
        // TODO Add to public interface in FbBlob?
        synchronized (getSynchronizationObject()) {
            return transaction;
        }
    }

    protected final void clearTransaction() {
        synchronized (getSynchronizationObject()) {
            transaction = null;
        }
    }

    protected FbDatabase getDatabase() {
        // TODO Add to public interface in FbBlob?
        synchronized (getSynchronizationObject()) {
            return database;
        }
    }

    protected final void clearDatabase() {
        synchronized (getSynchronizationObject()) {
            database = null;
        }
    }
}
