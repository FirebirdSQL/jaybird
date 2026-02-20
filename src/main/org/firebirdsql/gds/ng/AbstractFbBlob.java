// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListenerDispatcher;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLWarning;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import static java.util.Objects.requireNonNull;

/**
 * Base class for low-level blob operations.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbBlob implements FbBlob, TransactionListener, DatabaseListener {

    private static final System.Logger log = System.getLogger(AbstractFbBlob.class.getName());

    protected final ExceptionListenerDispatcher exceptionListenerDispatcher = new ExceptionListenerDispatcher(this);
    private final BlobParameterBuffer blobParameterBuffer;
    private final int maximumSegmentSize;
    private @Nullable FbTransaction transaction;
    private final FbDatabase database;
    private BlobState state = BlobState.NEW;
    private boolean eof;
    private @Nullable SQLException deferredException;

    protected AbstractFbBlob(FbDatabase database, FbTransaction transaction, BlobParameterBuffer blobParameterBuffer)
            throws SQLException {
        this.database = requireNonNull(database, "database");
        this.transaction = requireNonNull(transaction, "transaction");
        checkDatabaseAttached();
        checkTransactionActive();
        this.blobParameterBuffer = BlobParameterBuffer.orEmpty(blobParameterBuffer);
        maximumSegmentSize = maximumSegmentSize(database);
        transaction.addWeakTransactionListener(this);
    }

    @Override
    public final boolean isOpen() {
        // NOTE: Class implementation references field state directly under lock, when implementation changes, account for that
        try (var ignored = withLock()) {
            return state.isOpen();
        }
    }

    @Override
    public final boolean isEof() {
        try (var ignored = withLock()) {
            return eof || state.isClosed();
        }
    }

    /**
     * Marks this blob as EOF (end of file).
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
     * This method should only be called by subclasses of this class.
     * </p>
     */
    protected final void resetEof() {
        try (var ignored = withLock()) {
            eof = false;
        }
    }

    /**
     * Sets the state of the blob to the specified value.
     * <p>
     * This method should only be called by subclasses of this class.
     * </p>
     *
     * @param newState
     *         new value of state
     * @since 5.0.7
     */
    protected final void setState(BlobState newState) {
        // TODO Verify reopen behaviour, especially given close() shuts down exceptionListenerDispatcher
        try (var ignored = withLock()) {
            if (newState.isClosed()) {
                database.removeDatabaseListener(this);
                FbTransaction transaction = this.transaction;
                if (transaction != null) {
                    transaction.removeTransactionListener(this);
                }
            } else if (state.isClosed()) {
                // Going from closed to open
                database.addWeakDatabaseListener(this);
            }
            this.state = newState;
        }
    }

    /**
     * The current blob state.
     *
     * @return current blob state
     * @since 5.0.7
     */
    protected final BlobState getState() {
        try (var ignored = withLock()) {
            return state;
        }
    }

    @Override
    public final void close() throws SQLException {
        try (var ignored = withLock()) {
            if (state.isClosed()) return;
            try {
                if (isEndingTransaction()) {
                    releaseResources();
                } else {
                    checkDatabaseAttached();
                    checkTransactionActive();
                    closeImpl();
                }
            } finally {
                setState(BlobState.CLOSED);
            }
            throwAndClearDeferredException();
        } catch (SQLException e) {
            errorOccurred(e);
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
        try (var ignored = withLock()) {
            try {
                if (isEndingTransaction()) {
                    releaseResources();
                } else {
                    checkDatabaseAttached();
                    checkTransactionActive();
                    cancelImpl();
                }
            } finally {
                setState(BlobState.CLOSED);
            }
            throwAndClearDeferredException();
        } catch (SQLException e) {
            errorOccurred(e);
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

    @Override
    public final int get(byte[] b, int off, int len) throws SQLException {
        // requested length is minimum length
        return get(b, off, len, len);
    }

    @Override
    public final int get(byte[] b, int off, int len, float minFillFactor) throws SQLException {
        if (minFillFactor <= 0f || minFillFactor > 1f || Float.isNaN(minFillFactor)) {
            var invalidFloatFactor = new SQLNonTransientException(
                    "minFillFactor out of range, must be 0 < minFillFactor <= 1, was: " + minFillFactor);
            errorOccurred(invalidFloatFactor);
            throw invalidFloatFactor;
        }
        return get(b, off, len, len != 0 ? Math.max(1, (int) (minFillFactor * len)) : 0);
    }

    /**
     * Default implementation for {@link #get(byte[], int, int)} and {@link #get(byte[], int, int, float)}.
     *
     * @param b
     *         target byte array
     * @param off
     *         offset to start
     * @param len
     *         number of bytes
     * @param minLen
     *         minimum number of bytes to fill (must be {@code 0 < minLen <= len} if {@code len != 0}
     * @return actual number of bytes read; is {@code 0} if {@code len == 0}, will only be less than {@code minLen} if
     * end-of-blob is reached
     * @throws SQLException
     *         for database access errors, if {@code off < 0}, {@code len < 0}, or if {@code off + len > b.length},
     *         or {@code len != 0 && (minLen <= 0 || minLen > len)}
     */
    protected abstract int get(byte[] b, int off, int len, int minLen) throws SQLException;

    /**
     * Release Java resources held. This should not communicate with the Firebird server.
     */
    protected abstract void releaseResources();

    /**
     * Registers an exception as a deferred exception.
     * <p>
     * This should only be used for exceptions from deferred response that need to be thrown.
     * </p>
     *
     * @param deferredException
     *         deferred exception
     * @since 5.0.7
     */
    protected final void registerDeferredException(SQLException deferredException) {
        requireNonNull(deferredException, "deferredException");
        SQLException current = this.deferredException;
        if (current == null) {
            this.deferredException = deferredException;
        } else {
            current.setNextException(deferredException);
        }
    }

    /**
     * Clears the deferred exception.
     *
     * @since 5.0.7
     */
    protected final void clearDeferredException() {
        this.deferredException = null;
    }

    /**
     * If a deferred exception is registered it is cleared and thrown.
     *
     * @throws SQLException
     *         the current deferred exception, if any
     * @since 5.0.7
     */
    protected final void throwAndClearDeferredException() throws SQLException {
        SQLException current = this.deferredException;
        if (current != null) {
            clearDeferredException();
            throw current;
        }
    }

    /**
     * If a deferred exception is registered, it is cleared and set as a next exception on {@code target}.
     *
     * @param target
     *         the target exception to add the deferred exception to (not {@code null})
     * @throws NullPointerException
     *         if there is a deferred exception, and {@code target == null}
     * @since 5.0.7
     */
    protected final void transferDeferredExceptionTo(SQLException target) {
        try (var ignored = withLock()) {
            SQLException current = this.deferredException;
            if (current != null) {
                clearDeferredException();
                if (current != target) {
                    requireNonNull(target, "target").setNextException(current);
                }
            }
        }
    }

    /**
     * @see FbAttachment#withLock() 
     */
    protected final LockCloseable withLock() {
        return database.withLock();
    }

    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
            TransactionState previousState) {
        if (getTransaction() != transaction) {
            transaction.removeTransactionListener(this);
            return;
        }
        switch (newState) {
        case COMMITTING, ROLLING_BACK, PREPARING -> {
            try {
                close();
            } catch (SQLException e) {
                log.log(ERROR, "Exception while closing blob during transaction end", e);
            }
        }
        case COMMITTED, ROLLED_BACK -> {
            try (var ignored = withLock()) {
                clearTransaction();
                setState(BlobState.CLOSED);
                releaseResources();
            }
        }
        default -> {
            // Do nothing
        }
        }
    }

    @Override
    public void detaching(FbDatabase database) {
        if (this.database != database) {
            database.removeDatabaseListener(this);
            return;
        }
        try (var ignored = withLock()) {
            if (state.isOpen()) {
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
        try (var ignored = withLock()) {
            if (this.database == database) {
                state = BlobState.CLOSED;
                unregisterFromDb();
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
        return TransactionHelper.isTransactionEnding(getTransaction());
    }

    /**
     * @throws java.sql.SQLException
     *         when no transaction is set, or the transaction state is not {@link TransactionState#ACTIVE}
     */
    protected final void checkTransactionActive() throws SQLException {
        TransactionHelper.checkTransactionActive(getTransaction(), ISCConstants.isc_segstr_no_trans);
    }

    /**
     * @throws SQLException
     *         when no database is set, or the database is not attached
     */
    protected final void checkDatabaseAttached() throws SQLException {
        if (!database.isAttached()) {
            throw FbExceptionBuilder.toNonTransientException(ISCConstants.isc_segstr_wrong_db);
        }
    }

    /**
     * Checks if the blob is open.
     * <p>
     * NOTE: Subclasses may perform additional side effects, like queuing a server-side open for a deferred open blob.
     * </p>
     *
     * @throws SQLException
     *         when the blob is closed.
     */
    protected void checkBlobOpen() throws SQLException {
        BlobHelper.checkBlobOpen(this);
    }

    /**
     * @throws SQLException
     *         when the blob is open.
     */
    protected void checkBlobClosed() throws SQLException {
        BlobHelper.checkBlobClosed(this);
    }

    protected @Nullable FbTransaction getTransaction() {
        try (var ignored = withLock()) {
            return transaction;
        }
    }

    /**
     * @return transaction, if this blob has an active transaction
     * @throws SQLException
     *         if this blob has no transaction, or the transaction is not active
     * @since 7
     */
    protected FbTransaction requireActiveTransaction() throws SQLException {
        FbTransaction transaction = getTransaction();
        TransactionHelper.checkTransactionActive(transaction, ISCConstants.isc_segstr_no_trans);
        return transaction;
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
    public <T extends @Nullable Object> T getBlobInfo(byte[] requestItems, int bufferLength,
            InfoProcessor<T> infoProcessor) throws SQLException {
        byte[] blobInfo = getBlobInfo(requestItems, bufferLength);
        try {
            return infoProcessor.process(blobInfo);
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    /**
     * The known blob info items for the connected server as a blob info request buffer.
     *
     * @return the known blob info items (possibly empty under implementation-specific circumstances)
     * @since 7
     */
    protected byte[] getKnownBlobInfoItems() {
        if (getDatabase() instanceof AbstractFbDatabase<?> db) {
            return db.getServerVersionInformation().getBlobInfoRequestItems();
        }
        return ByteArrayHelper.emptyByteArray();
    }

    @Override
    public long length() throws SQLException {
        try (var ignored = withLock()) {
            checkDatabaseAttached();
            if (isOutput() && getBlobId() == FbBlob.NO_BLOB_ID && !getState().isDeferredOpen()) {
                throw FbExceptionBuilder.toException(ISCConstants.isc_bad_segstr_id);
            }
            BlobLengthProcessor blobLengthProcessor = createBlobLengthProcessor();
            return getBlobInfo(blobLengthProcessor.getBlobLengthItems(), 20, blobLengthProcessor);
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    /**
     * Notifies {@link ExceptionListenerDispatcher#errorOccurred(SQLException)}.
     * <p>
     * If there is a registered deferred exception, it is set as a next exception on {@code e} before notification.
     * </p>
     *
     * @param e
     *         exception to notify to exception listeners
     */
    protected final void errorOccurred(SQLException e) {
        transferDeferredExceptionTo(e);
        exceptionListenerDispatcher.errorOccurred(e);
    }

    @Override
    public final void addExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.addListener(listener);
    }

    @Override
    public final void removeExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.removeListener(listener);
    }

    protected final void unregisterFromDb() {
        database.removeDatabaseListener(this);
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
        /* Max size in FB 2.1 and higher is 2^16 - 1, not 2^15 - 3 (IB 6 docs mention max is 32KiB). However,
           Firebird 2.1 and 2.5 have issues with conversion from SSHORT to 32-bit (applying sign extension), leading to
           incorrect buffer sizes, instead of addressing that, we only apply the higher limit for Firebird 3.0 and
           higher. */
        //noinspection ConstantValue : null-check for robustness
        if (db != null && db.getServerVersion().isEqualOrAbove(3)) {
            /* NOTE: getSegment can retrieve at most 65533 bytes of blob data as the buffer to receive segments is
               max 65535 bytes, but the contents of the buffer are one or more segments prefixed with 2-byte lengths;
               putSegment can write max 65535 bytes, because the buffer *is* the segment */
            return 65535;
        }
        /* NOTE: This should probably be Short.MAX_VALUE, but we can no longer run the relevant tests on Firebird 2.0
           and older (which aren't supported any way), and for Firebird 2.1 and 2.5, this may cause the same issue with
           sign extension and buffer sizes mentioned above, so we leave this as is. */
        return Short.MAX_VALUE - 2;
    }

    /**
     * State of the blob.
     *
     * @since 5.0.7
     */
    protected enum BlobState {
        /**
         * Blob is new and not yet opened.
         */
        NEW(false, false),
        /**
         * Blob is deferred open, the open request is delayed (only client-side open).
         */
        DELAYED_OPEN(true, true),
        /**
         * Blob is deferred open, and open request is pending (already sent or in send buffer, and response not yet
         * processed).
         */
        PENDING_OPEN(true, true),
        /**
         * Blob is open client-side and server-side.
         */
        OPEN(true, false),
        /**
         * Blob is closed.
         */
        CLOSED(false, false),
        ;

        private final boolean open;
        private final boolean deferredOpen;

        BlobState(boolean open, boolean deferredOpen) {
            assert !deferredOpen || open : "open must be true when deferredOpen is true";
            this.open = open;
            this.deferredOpen = deferredOpen;
        }

        /**
         * @return {@code true} if this state is an open blob
         */
        public final boolean isOpen() {
            return open;
        }

        /**
         * @return {@code true} if this state is a closed blob (including not yet opened)
         */
        public final boolean isClosed() {
            return !open;
        }

        /**
         * @return {@code true} if this state is a deferred state (i.e. the {@link #DELAYED_OPEN} and
         * {@link #PENDING_OPEN} states)
         */
        public final boolean isDeferredOpen() {
            return deferredOpen;
        }

    }

}
