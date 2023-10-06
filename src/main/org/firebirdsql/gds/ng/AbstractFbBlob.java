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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListenerDispatcher;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jdbc.SQLStateConstants;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLWarning;
import java.util.Objects;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbBlob implements FbBlob, TransactionListener, DatabaseListener {

    private static final System.Logger log = System.getLogger(AbstractFbBlob.class.getName());

    protected final ExceptionListenerDispatcher exceptionListenerDispatcher = new ExceptionListenerDispatcher(this);
    private final BlobParameterBuffer blobParameterBuffer;
    private final int maximumSegmentSize;
    private FbTransaction transaction;
    private FbDatabase database;
    private boolean open;
    private boolean eof;

    protected AbstractFbBlob(FbDatabase database, FbTransaction transaction, BlobParameterBuffer blobParameterBuffer) {
        this.database = database;
        this.transaction = transaction;
        this.blobParameterBuffer = blobParameterBuffer;
        maximumSegmentSize = maximumSegmentSize(database);
        transaction.addWeakTransactionListener(this);
    }

    @Override
    public final boolean isOpen() {
        // NOTE: Class implementation references field open directly under lock, when implementation changes, account for that
        try (LockCloseable ignored = withLock()) {
            return open;
        }
    }

    @Override
    public final boolean isEof() {
        try (LockCloseable ignored = withLock()) {
            return eof || !open;
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
        try (LockCloseable ignored = withLock()) {
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
        try (LockCloseable ignored = withLock()) {
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
        try (LockCloseable ignored = withLock()) {
            final FbDatabase database = this.database;
            if (open) {
                database.addWeakDatabaseListener(this);
            } else {
                database.removeDatabaseListener(this);
                final FbTransaction transaction = this.transaction;
                if (transaction != null) {
                    transaction.removeTransactionListener(this);
                }
            }
            this.open = open;
        }
    }

    @Override
    public final void close() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (!open) return;
            try {
                if (isEndingTransaction()) {
                    releaseResources();
                } else {
                    checkDatabaseAttached();
                    checkTransactionActive();
                    closeImpl();
                }
            } finally {
                setOpen(false);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        } finally {
            exceptionListenerDispatcher.shutdown();
        }
    }

    /**
     * Internal implementation of {@link #close()}. The implementation does not need
     * to check for attached database and active transaction, nor does it need to mark this blob as closed.
     */
    protected abstract void closeImpl() throws SQLException;

    @Override
    public final void cancel() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            try {
                if (isEndingTransaction()) {
                    releaseResources();
                } else {
                    checkDatabaseAttached();
                    checkTransactionActive();
                    cancelImpl();
                }
            } finally {
                setOpen(false);
            }
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    /**
     * Internal implementation of {@link #cancel()}. The implementation does not need
     * to check for attached database and active transaction, nor does it need to mark this blob as closed.
     */
    protected abstract void cancelImpl() throws SQLException;

    @Override
    public void putSegment(byte[] segment) throws SQLException {
        put(segment, 0, segment.length);
    }

    /**
     * Release Java resources held. This should not communicate with the Firebird server.
     */
    protected abstract void releaseResources();

    /**
     * @see FbAttachment#withLock() 
     */
    protected final LockCloseable withLock() {
        FbDatabase database = this.database;
        if (database != null) {
            return database.withLock();
        }
        // No need or operation to lock, so return a no-op to unlock.
        return LockCloseable.NO_OP;
    }

    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
            TransactionState previousState) {
        if (getTransaction() != transaction) {
            transaction.removeTransactionListener(this);
            return;
        }
        switch (newState) {
        case COMMITTING:
        case ROLLING_BACK:
        case PREPARING:
            try {
                close();
            } catch (SQLException e) {
                log.log(ERROR, "Exception while closing blob during transaction end", e);
            }
            break;
        case COMMITTED:
        case ROLLED_BACK:
            try (LockCloseable ignored = withLock()) {
                clearTransaction();
                setOpen(false);
                releaseResources();
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
        if (this.database != database) {
            database.removeDatabaseListener(this);
            return;
        }
        try (LockCloseable ignored = withLock()) {
            if (open) {
                log.log(TRACE, "blob with blobId {0} still open on database detach", getBlobId());
                try {
                    close();
                } catch (SQLException e) {
                    log.log(ERROR, "Blob close in detaching event failed", e);
                }
            }
        }
    }

    @Override
    public void detached(FbDatabase database) {
        try (LockCloseable ignored = withLock()) {
            if (this.database == database) {
                open = false;
                clearDatabase();
                clearTransaction();
                releaseResources();
            }
        } finally {
            database.removeDatabaseListener(this);
        }
    }

    @Override
    public void warningReceived(FbDatabase database, SQLWarning warning) {
        // Do nothing
    }

    /**
     * @return {@code true} if the transaction is committing, rolling back or preparing
     */
    protected final boolean isEndingTransaction() {
        FbTransaction transaction = getTransaction();
        if (transaction != null) {
            TransactionState transactionState = transaction.getState();
            return transactionState == TransactionState.COMMITTING
                    || transactionState == TransactionState.ROLLING_BACK
                    || transactionState == TransactionState.PREPARING;
        }
        return false;
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
        FbDatabase database = this.database;
        if (database == null || !database.isAttached()) {
            throw FbExceptionBuilder.forNonTransientException(ISCConstants.isc_segstr_wrong_db).toSQLException();
        }
    }

    /**
     * @throws SQLException
     *         When the blob is closed.
     */
    protected void checkBlobOpen() throws SQLException {
        if (!isOpen()) {
            // TODO Use more specific exception message?
            throw FbExceptionBuilder.forNonTransientException(ISCConstants.isc_bad_segstr_handle).toSQLException();
        }
    }

    /**
     * @throws SQLException
     *         When the blob is open.
     */
    protected void checkBlobClosed() throws SQLException {
        if (isOpen()) {
            throw FbExceptionBuilder.forNonTransientException(ISCConstants.isc_no_segstr_close).toSQLException();
        }
    }

    protected FbTransaction getTransaction() {
        try (LockCloseable ignored = withLock()) {
            return transaction;
        }
    }

    protected final void clearTransaction() {
        final FbTransaction transaction;
        try (LockCloseable ignored = withLock()) {
            transaction = this.transaction;
            this.transaction = null;
        }
        if (transaction != null) {
            transaction.removeTransactionListener(this);
        }
    }

    @Override
    public FbDatabase getDatabase() {
        try (LockCloseable ignored = withLock()) {
            return database;
        }
    }

    @Override
    public <T> T getBlobInfo(final byte[] requestItems, final int bufferLength, final InfoProcessor<T> infoProcessor)
            throws SQLException {
        final byte[] blobInfo = getBlobInfo(requestItems, bufferLength);
        try {
            return infoProcessor.process(blobInfo);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public long length() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            checkDatabaseAttached();
            if (isOutput() && getBlobId() == FbBlob.NO_BLOB_ID) {
                throw FbExceptionBuilder.forException(ISCConstants.isc_bad_segstr_id).toSQLException();
            }
            BlobLengthProcessor blobLengthProcessor = createBlobLengthProcessor();
            return getBlobInfo(blobLengthProcessor.getBlobLengthItems(), 20, blobLengthProcessor);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public final void addExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.addListener(listener);
    }

    @Override
    public final void removeExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.removeListener(listener);
    }

    protected final void clearDatabase() {
        final FbDatabase database;
        try (LockCloseable ignored = withLock()) {
            database = this.database;
            this.database = null;
        }
        if (database != null) {
            database.removeDatabaseListener(this);
        }
    }

    /**
     * @return The blob parameter buffer of this blob.
     */
    protected BlobParameterBuffer getBlobParameterBuffer() {
        return blobParameterBuffer;
    }

    /**
     * @return New instance of {@link BlobLengthProcessor} for this blob.
     */
    protected BlobLengthProcessor createBlobLengthProcessor() {
        return new BlobLengthProcessor();
    }

    @Override
    public int getMaximumSegmentSize() {
        return maximumSegmentSize;
    }

    private static int maximumSegmentSize(FbDatabase db) {
        // Max size in FB 2.1 and higher is 2^16 - 1, not 2^15 - 3 (IB 6 docs mention max is 32KiB)
        if (db != null && (db.getOdsMajor() > 11 || db.getOdsMajor() == 11 && db.getOdsMinor() >= 1)) {
            /* ODS 11.1 is Firebird 2.1
               NOTE: getSegment can retrieve at most 65533 bytes of blob data as the buffer to receive segments is
               max 65535 bytes, but the contents of the buffer are one or more segments prefixed with 2-byte lengths;
               putSegment can write max 65535 bytes, because the buffer *is* the segment */
            return 65535;
        }
        // NOTE: This should probably be Short.MAX_VALUE, but we can no longer run the relevant tests on Firebird 2.0
        // and older (which aren't supported any way), so we leave this as is
        return Short.MAX_VALUE - 2;
    }

    /**
     * Validates requested offset ({@code off}) and length ({@code len}) against the array ({@code b}).
     *
     * @param b
     *         array
     * @param off
     *         position in array
     * @param len
     *         length from {@code off}
     * @throws SQLException
     *         if {@code off < 0}, {@code len < 0}, or if {@code off + len > b.length}
     * @since 6
     */
    protected final void validateBufferLength(byte[] b, int off, int len) throws SQLException {
        try {
            Objects.checkFromIndexSize(off, len, b.length);
        } catch (IndexOutOfBoundsException e) {
            throw new SQLNonTransientException(e.toString(), SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
        }
    }

}
