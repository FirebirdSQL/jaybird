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

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbBlob implements FbBlob, TransactionListener, DatabaseListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractFbBlob.class);

    private final Object syncObject = new Object();
    private final BlobParameterBuffer blobParameterBuffer;
    private FbTransaction transaction;
    private FbDatabase database;
    private boolean open;
    private boolean eof;

    protected AbstractFbBlob(FbDatabase database, FbTransaction transaction, BlobParameterBuffer blobParameterBuffer) {
        this.database = database;
        this.transaction = transaction;
        this.blobParameterBuffer = blobParameterBuffer;
        transaction.addTransactionListener(this);
        database.addDatabaseListener(this);
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

    /**
     * Resets the eof state of the blob to false (not eof).
     * <p>
     * This method should only be called by sub-classes of this class.
     * </p>
     */
    protected final void resetEof() {
        synchronized (getSynchronizationObject()) {
            eof = false;
        }
    }

    /**
     * Sets the open state of the blob to the specified value.
     * <p>
     * This method should only be called by sub-classes of this class.
     * </p>
     *
     * @param open New value of open.
     */
    protected final void setOpen(boolean open) {
        synchronized (getSynchronizationObject()) {
            this.open = open;
        }
    }

    @Override
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

    @Override
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
            return;
        }
        switch (newState) {
        case COMMITTED:
        case ROLLED_BACK:
            synchronized (getSynchronizationObject()) {
                clearTransaction();
                setOpen(false);
            }
            break;
        default:
            // Do nothing
            break;
        }
        // TODO Need additional handling for other transitions?
    }

    @Override
    public void detaching(FbDatabase database) {
        synchronized (getSynchronizationObject()) {
            if (this.database != database) {
                database.removeDatabaseListener(this);
            } else if (isOpen()) {
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
    protected final void checkTransactionActive() throws SQLException {
        TransactionHelper.checkTransactionActive(getTransaction(), ISCConstants.isc_segstr_no_trans);
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

    /**
     * @throws SQLException
     *         When the blob is closed.
     */
    protected void checkBlobOpen() throws SQLException {
        if (!isOpen()) {
            // TODO Use more specific exception message?
            throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_bad_segstr_handle).toSQLException();
        }
    }

    /**
     * @throws SQLException
     *         When the blob is open.
     */
    protected void checkBlobClosed() throws SQLException {
        if (isOpen()) {
            throw new FbExceptionBuilder().nonTransientException(ISCConstants.isc_no_segstr_close).toSQLException();
        }
    }

    protected FbTransaction getTransaction() {
        synchronized (getSynchronizationObject()) {
            return transaction;
        }
    }

    protected final void clearTransaction() {
        synchronized (getSynchronizationObject()) {
            if (transaction != null) {
                transaction.removeTransactionListener(this);
            }
            transaction = null;
        }
    }

    @Override
    public FbDatabase getDatabase() {
        synchronized (getSynchronizationObject()) {
            return database;
        }
    }

    @Override
    public <T> T getBlobInfo(final byte[] requestItems, final int bufferLength, final InfoProcessor<T> infoProcessor)
            throws SQLException {
        return infoProcessor.process(getBlobInfo(requestItems, bufferLength));
    }

    @Override
    public long length() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkDatabaseAttached();
            if (getBlobId() == FbBlob.NO_BLOB_ID) {
                throw new FbExceptionBuilder().exception(ISCConstants.isc_bad_segstr_id).toSQLException();
            }
            final BlobLengthProcessor blobLengthProcessor = createBlobLengthProcessor();
            return getBlobInfo(blobLengthProcessor.getBlobLengthItems(), 20, blobLengthProcessor);
        }
    }

    protected final void clearDatabase() {
        synchronized (getSynchronizationObject()) {
            if (database != null) {
                database.removeDatabaseListener(this);
            }
            database = null;
        }
    }

    /**
     * @return The blob parameter buffer of this blob.
     */
    protected BlobParameterBuffer getBlobParameterBuffer() {
        return blobParameterBuffer;
    }

    /**
     * @return New instance of {@link BlobLengthProcessor} (or subclass) for this blob.
     */
    protected BlobLengthProcessor createBlobLengthProcessor() {
        return new BlobLengthProcessor(this);
    }

    @Override
    public int getMaximumSegmentSize() {
        // TODO Max size in FB 3 is 2^16, not 2^15 - 1, is that for all versions, or only for newer protocols?
        // Subtracting 2 because the maximum size includes length TODO: verify if true
        return Short.MAX_VALUE - 2;
    }
}
