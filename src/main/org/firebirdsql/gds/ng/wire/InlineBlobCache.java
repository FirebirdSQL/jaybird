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

import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.TransactionListener;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Cache for inline blobs of a single attachment.
 *
 * @author Mark Rotteveel
 * @since 5.0.8
 */
public final class InlineBlobCache implements DatabaseListener, TransactionListener {

    private final FbDatabase database;
    private final int maxSize;
    private long size;
    // Sizing at 4 instead of default (16); in normal use, there will usually only be 1 or 2 transactions active
    private Map<Integer, Map<Long, InlineBlob>> transactionToBlobIdToBlob = new HashMap<>(4);

    /**
     * Creates an inline blob cache with the {@code maxBlobCacheSize} configured for {@code database}.
     *
     * @param database
     *         database instance
     */
    public InlineBlobCache(FbDatabase database) {
        this.database = database;
        int maxBlobCacheSize = database.getConnectionProperties().getMaxBlobCacheSize();
        // Using -1 if the size is 0 to prevent zero-sized blobs from getting cached, so really disabling the cache
        maxSize = maxBlobCacheSize > 0 ? maxBlobCacheSize : -1;
        database.addDatabaseListener(this);
    }

    /**
     * @return the maximum cache size, {@code -1} if the cache is disabled
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * @return the current cache size
     */
    public int size() {
        try (LockCloseable ignored = withLock()) {
            return Math.toIntExact(size);
        }
    }

    /**
     * Checks if the cache holds a blob.
     * <p>
     * This method is primarily intended for tests.
     * </p>
     *
     * @param transactionHandle
     *         transaction handle
     * @param blobId
     *         blob id
     * @return {@code true} if the cache holds this blob, {@code false} otherwise
     */
    boolean contains(int transactionHandle, long blobId) {
        try (LockCloseable ignored = withLock()) {
            Map<Long, InlineBlob> blobIdToBlob = transactionToBlobIdToBlob.get(transactionHandle);
            if (blobIdToBlob == null) return false;
            return blobIdToBlob.containsKey(blobId);
        }
    }

    /**
     * Gets an inline blob for the transaction and blob id and removes it from the cache.
     *
     * @param transaction
     *         transaction
     * @param blobId
     *         blob id
     * @return inline blob or empty if not present in the cache
     * @see #getAndRemove(int, long)
     */
    public Optional<InlineBlob> getAndRemove(FbTransaction transaction, long blobId) {
        return getAndRemove(transaction.getHandle(), blobId);
    }

    /**
     * Gets an inline blob for the transaction and blob id and removes it from the cache.
     *
     * @param transactionHandle
     *         transaction handle
     * @param blobId
     *         blob id
     * @return inline blob or empty if not present in the cache
     * @see #getAndRemove(FbTransaction, long)
     */
    Optional<InlineBlob> getAndRemove(int transactionHandle, long blobId) {
        try (LockCloseable ignored = withLock()) {
            Map<Long, InlineBlob> blobIdToBlob = transactionToBlobIdToBlob.get(transactionHandle);
            if (blobIdToBlob == null) return Optional.empty();
            InlineBlob blob = blobIdToBlob.remove(blobId);
            if (blob == null) return Optional.empty();
            size = Math.max(0, size - blob.length());
            return Optional.of(blob);
        }
    }

    /**
     * Adds an inline blob to the cache for the transaction and blob id.
     * <p>
     * The inline blob is not stored in the cache if:
     * </p>
     * <ul>
     *     <li>{@code transaction} is not active</li>
     *     <li>{@link FbTransaction#getHandle()} is not equal to {@link InlineBlob#getTransactionHandle()}</li>
     *     <li>Adding the blob would exceed the maximum cache size</li>
     * </ul>
     * <p>
     * If the blob is stored, this cache is added as a transaction listener of {@code transaction}.
     * </p>
     *
     * @param transaction
     *         transaction
     * @param blob
     *         inline blob
     * @return {@code true} if the blob was added to the cache, {@code false} otherwise
     */
    public boolean add(FbTransaction transaction, InlineBlob blob) {
        int transactionHandle = blob.getTransactionHandle();
        if (transaction.getState() != TransactionState.ACTIVE
                || transaction.getHandle() != transactionHandle) {
            // do not register
            return false;
        }
        try (LockCloseable ignored = withLock()) {
            // don't exceed cache size
            long newSize = size + blob.length();
            if (newSize > maxSize) return false;
            InlineBlob previous = transactionToBlobIdToBlob
                    .computeIfAbsent(transactionHandle, tr -> new HashMap<>())
                    .putIfAbsent(blob.getBlobId(), blob);
            if (previous != null) {
                // Blob already exists
                return false;
            }
            size = newSize;
            transaction.addTransactionListener(this);
            return true;
        }
    }

    /**
     * Transaction states where the blobs for that transaction should be removed from the cache.
     * <p>
     * We're not cleaning on {@code PREPARING}, {@code COMMITTING}, and {@code ROLLING_BACK}, so listeners that are
     * notified after the cache might still be able to access the cache before the prepare, commit or roll back is done.
     * </p>
     */
    private static final Set<TransactionState> CLEAN_CACHE_ON_TRANSACTION_STATES = unmodifiableSet(
            EnumSet.of(TransactionState.PREPARED, TransactionState.COMMITTED, TransactionState.ROLLED_BACK));

    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
            TransactionState previousState) {
        if (CLEAN_CACHE_ON_TRANSACTION_STATES.contains(newState)) {
            Integer transactionHandle = transaction.getHandle();
            try (LockCloseable ignored = withLock()) {
                Map<Long, InlineBlob> blobIdToInlineBlob = transactionToBlobIdToBlob.remove(transactionHandle);
                if (blobIdToInlineBlob == null || blobIdToInlineBlob.isEmpty()) return;
                long removedSize = blobIdToInlineBlob.values().stream().mapToLong(InlineBlob::length).sum();
                size = Math.max(0, size - removedSize);
            }
        }
    }

    @Override
    public void detached(FbDatabase database) {
        try (LockCloseable ignored = withLock()) {
            transactionToBlobIdToBlob = Collections.emptyMap();
            size = 0;
        }
    }

    private LockCloseable withLock() {
        return database.withLock();
    }

}
