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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.BlobHelper;
import org.firebirdsql.gds.ng.CachedInfoResponse;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.InfoProcessor;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListenerDispatcher;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_blobGetSegmentNegative;
import static org.firebirdsql.jaybird.util.ByteArrayHelper.validateBufferLength;

/**
 * Implementation of {@link FbBlob} to hold an inline blob.
 * <p>
 * This blob may remain open on transaction end or database detach. However, this is considered an implementation detail
 * which may change in point releases.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 6.0.2
 */
@NullMarked
public final class InlineBlob implements FbBlob {

    private static final int LAST_REAL_HANDLE_ID = 0xFFFF;
    private static final AtomicInteger localHandleIdGenerator = new AtomicInteger(LAST_REAL_HANDLE_ID);

    private final int localHandleId = nextLocalHandleId();
    private final FbDatabase database;
    private final long blobId;
    private final int transactionHandle;
    private final CachedInfoResponse info;
    private final byte[] data;
    private final ExceptionListenerDispatcher exceptionListenerDispatcher = new ExceptionListenerDispatcher(this);
    // Current position in the blob, a negative value signals the blob is closed
    private int position = -1;

    /**
     * Creates an inline blob.
     *
     * @param database
     *         database that created this blob
     * @param blobId
     *         blob id
     * @param transactionHandle
     *         handle of the transaction for which this blob is valid
     * @param info
     *         blob info (cannot be {@code null}), if empty an array containing {@code isc_info_end} will be used
     * @param data
     *         actual blob data without segments (cannot be {@code null})
     */
    public InlineBlob(FbDatabase database, long blobId, int transactionHandle, byte[] info, byte[] data) {
        this(database, blobId, transactionHandle,
                requireNonNull(info, "info").length != 0 ? new CachedInfoResponse(info) : CachedInfoResponse.empty(),
                data);
    }

    private InlineBlob(FbDatabase database, long blobId, int transactionHandle, CachedInfoResponse info, byte[] data) {
        this.database = database;
        this.blobId = blobId;
        this.transactionHandle = transactionHandle;
        this.info = info;
        // NOTE: we're not copying data to avoid overhead
        this.data = requireNonNull(data, "data");
    }

    @Override
    public long getBlobId() {
        return blobId;
    }

    /**
     * @return the transaction handle this inline blob is valid for
     */
    public int getTransactionHandle() {
        return transactionHandle;
    }

    /**
     * {@inheritDoc}
     * <p>
     * An inline blob has no server-side counterpart, so we generate a value greater than 65535 as its handle.
     * </p>
     */
    @Override
    public int getHandle() {
        return localHandleId;
    }

    @Override
    public FbDatabase getDatabase() {
        try (var ignored = withLock()) {
            return database;
        }
    }

    @Override
    public void open() throws SQLException {
        try (var ignored = withLock()) {
            checkBlobClosed();
            position = 0;
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public boolean isOpen() {
        try (var ignored = withLock()) {
            return position >= 0;
        }
    }

    @Override
    public boolean isEof() {
        try (var ignored = withLock()) {
            return position >= data.length || position < 0;
        }
    }

    @Override
    public void close() {
        try (var ignored = withLock()) {
            position = -1;
        }
    }

    @Override
    public void cancel() {
        close();
    }

    @Override
    public boolean isOutput() {
        return false;
    }

    @Override
    public byte[] getSegment(int sizeRequested) throws SQLException {
        try (var ignored = withLock()) {
            if (sizeRequested <= 0) {
                throw FbExceptionBuilder.forException(jb_blobGetSegmentNegative)
                        .messageParameter(sizeRequested)
                        .toSQLException();
            }
            checkBlobOpen();
            final int dataLength = data.length;
            final int originalPosition = position;
            if (originalPosition >= dataLength) return ByteArrayHelper.emptyByteArray();
            if (originalPosition == 0 && sizeRequested >= dataLength) {
                position = dataLength;
                return data.clone();
            }
            final int newPosition = position = Math.min(dataLength, Math.addExact(originalPosition, sizeRequested));
            return Arrays.copyOfRange(data, originalPosition, newPosition);
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public int get(byte[] b, int off, int len) throws SQLException {
        try (var ignored = withLock()) {
            validateBufferLength(b, off, len);
            checkBlobOpen();
            if (isEof() || len == 0) return 0;

            final int originalPosition = position;
            final int size = Math.min(len, data.length - originalPosition);
            System.arraycopy(data, originalPosition, b, off, size);
            position += size;
            return size;
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public int get(byte[] b, int off, int len, float minFillFactor) throws SQLException {
        // minFillFactor not relevant, as we have all data locally.
        return get(b, off, len);
    }

    @Override
    public void putSegment(byte[] segment) throws SQLException {
        throw writeNotSupported();
    }

    @Override
    public void put(byte[] b, int off, int len) throws SQLException {
        throw writeNotSupported();
    }

    @Override
    public void seek(int offset, SeekMode seekMode) throws SQLException {
        try (var ignored = withLock()) {
            checkBlobOpen();
            // NOTE: We're not distinguishing between stream or segmented blob, as the internal representation is
            // the same in this implementation; this may result in behavioural difference if a segmented blob doesn't
            // get cached or is too big to be sent inline.
            position = switch (seekMode) {
            case ABSOLUTE -> Math.min(Math.max(0, offset), data.length);
            case ABSOLUTE_FROM_END -> Math.max(0, Math.min(data.length + offset, data.length));
            case RELATIVE -> Math.max(0, Math.min(position + offset, data.length));
            };
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public int getMaximumSegmentSize() {
        // not actually relevant, report normal maximum
        return 65535;
    }

    @Override
    public <T> T getBlobInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException {
        final byte[] blobInfo = getBlobInfo(requestItems, bufferLength);
        try {
            return infoProcessor.process(blobInfo);
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public long length() {
        return data.length;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Unknown blob info items are ignored, and only known items are returned.
     * </p>
     */
    @Override
    public byte[] getBlobInfo(byte[] requestItems, int bufferLength) throws SQLException {
        try (var ignored = withLock()) {
            checkBlobOpen();
            return info.filtered(requestItems);
        } catch (SQLException e) {
            errorOccurred(e);
            throw e;
        }
    }

    @Override
    public void addExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.addListener(listener);
    }

    @Override
    public void removeExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.removeListener(listener);
    }

    /**
     * @return a copy of this inline blob, it is initially closed.
     */
    public InlineBlob copy() {
        // We intentionally do not copy data array
        return new InlineBlob(database, blobId, transactionHandle, info, data);
    }

    private LockCloseable withLock() {
        FbDatabase database = this.database;
        //noinspection ConstantValue
        if (database != null) {
            return database.withLock();
        }
        // No need or operation to lock, so return a no-op to unlock.
        return LockCloseable.NO_OP;
    }

    private void checkBlobOpen() throws SQLException {
        BlobHelper.checkBlobOpen(this);
    }

    private void checkBlobClosed() throws SQLException {
        BlobHelper.checkBlobClosed(this);
    }

    private void errorOccurred(SQLException e) {
        exceptionListenerDispatcher.errorOccurred(e);
    }

    private SQLException writeNotSupported() {
        SQLException e = FbExceptionBuilder.toNonTransientException(ISCConstants.isc_segstr_no_write);
        errorOccurred(e);
        return e;
    }

    private static int nextLocalHandleId() {
        // Reset generator after integer overflow
        int handleId = localHandleIdGenerator.incrementAndGet();
        while (handleId < 0) {
            localHandleIdGenerator.compareAndSet(handleId, LAST_REAL_HANDLE_ID);
            handleId = localHandleIdGenerator.incrementAndGet();
        }
        return handleId;
    }

}
