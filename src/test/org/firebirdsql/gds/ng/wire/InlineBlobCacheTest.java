// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.LongStream;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InlineBlobCache}.
 *
 * @author Mark Rotteveel
 */
@ExtendWith(MockitoExtension.class)
class InlineBlobCacheTest {

    private static final int USE_DEFAULT = Integer.MIN_VALUE;
    private final FbConnectionProperties props = new FbConnectionProperties();

    @Mock
    private FbDatabase database;

    @BeforeEach
    void setupMock() {
        when(database.getConnectionProperties()).thenAnswer(invocation -> props.asImmutable());
    }

    @ParameterizedTest
    @ValueSource(ints = { USE_DEFAULT, 0, 1024 * 1024 })
    void constructor(int maxBlobCacheSize) {
        int expectedMaxCacheSize = maxBlobCacheSize > 0 ? maxBlobCacheSize : -1;
        if (maxBlobCacheSize == USE_DEFAULT) {
            // verifying use of default value
            expectedMaxCacheSize = PropertyConstants.DEFAULT_MAX_BLOB_CACHE_SIZE;
        } else {
            props.setMaxBlobCacheSize(maxBlobCacheSize);
        }
        var cache = new InlineBlobCache(database);

        assertEquals(expectedMaxCacheSize, cache.maxSize(), "maxSize");
        assertEquals(0, cache.size(), "size");
        verify(database).addDatabaseListener(cache);
    }

    @Test
    void getAndRemove_transactionAndBlobNotInCache() {
        var cache = new InlineBlobCache(database);

        assertThat("expected no blob", cache.getAndRemove(1, 2), is(emptyOptional()));
    }

    @Test
    void getAndRemove_transactionInCache_blobNotInCache() {
        var cache = new InlineBlobCache(database);
        final int transactionHandle = 1;
        FbTransaction transaction = createTransactionMock(transactionHandle);
        final long blobId = 2;
        // Add other blob to cache
        cache.add(transaction, createInlineBlob(transactionHandle, blobId + 1, 10));

        assertThat("expected no blob", cache.getAndRemove(transaction, blobId), is(emptyOptional()));
    }

    @Test
    void add_andSubsequent_getAndRemove_happyPath() {
        var cache = new InlineBlobCache(database);
        final int transactionHandle = 2;
        final long blobId = 3;
        final int blobSize = 123;
        FbTransaction transaction = createTransactionMock(transactionHandle);
        InlineBlob blob = createInlineBlob(transactionHandle, blobId, blobSize);

        boolean added = cache.add(transaction, blob);
        assertTrue(added, "should add blob");
        assertEquals(blobSize, cache.size(), "size");
        verify(transaction).addTransactionListener(cache);

        assertThat("should return blob", cache.getAndRemove(transaction, blobId), is(optionalWithValue(blob)));
        assertEquals(0, cache.size(), "size");
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            maxBlobCacheSize, blob1Size, blob2Size, blob1Added, blob2Added
            100,              100,       0,         true,       true
            100,              100,       1,         true,       false
            100,              101,       100,       false,      true
            100,              101,       101,       false,      false
            100,              50,        50,        true,       true
            100,              50,        51,        true,       false
            100,              50,        100,       true,       false
            """)
    void add_multipleBlobs(int maxBlobCacheSize, int blob1Size, int blob2Size, boolean blob1Added, boolean blob2Added) {
        props.setMaxBlobCacheSize(maxBlobCacheSize);
        var cache = new InlineBlobCache(database);
        final int transactionHandle = 5;
        final long blobId1 = 6;
        final long blobId2 = 7;
        FbTransaction transaction = createTransactionMock(transactionHandle);
        InlineBlob blob1 = createInlineBlob(transactionHandle, blobId1, blob1Size);
        InlineBlob blob2 = createInlineBlob(transactionHandle, blobId2, blob2Size);
        int expectedSize = 0;

        assertEquals(blob1Added, cache.add(transaction, blob1), "adding blob1");
        if (blob1Added) {
            verify(transaction).addTransactionListener(cache);
            expectedSize += blob1Size;
        } else {
            verify(transaction, never()).addTransactionListener(any());
        }
        assertEquals(expectedSize, cache.size(), "size after blob1");

        clearInvocations(transaction);
        assertEquals(blob2Added, cache.add(transaction, blob2), "adding blob2");
        if (blob2Added) {
            verify(transaction).addTransactionListener(cache);
            expectedSize += blob2Size;
        } else {
            verify(transaction, never()).addTransactionListener(any());
        }
        assertEquals(expectedSize, cache.size(), "size after blob2");
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 10 })
    void add_doesNotAddBlobIfDisabled(int blobSize) {
        props.setMaxBlobCacheSize(0);
        var cache = new InlineBlobCache(database);
        final int transactionHandle = 5;
        final long blobId = 6;
        FbTransaction transaction = createTransactionMock(transactionHandle);
        InlineBlob blob = createInlineBlob(transactionHandle, blobId, blobSize);

        boolean added = cache.add(transaction, blob);
        assertFalse(added, "should not add blob");
        assertEquals(0, cache.size(), "size");
        assertThat("should not get blob", cache.getAndRemove(transaction, blobId), is(emptyOptional()));
        verify(transaction, never()).addTransactionListener(any());
    }

    @ParameterizedTest
    @EnumSource(value = TransactionState.class, names = "ACTIVE", mode = EnumSource.Mode.EXCLUDE)
    void add_doesNotAddBlobIfTransactionNotActive(TransactionState state) {
        var cache = new InlineBlobCache(database);
        final int transactionHandle = 5;
        final long blobId = 6;
        FbTransaction transaction = createTransactionMock(transactionHandle, state);
        InlineBlob blob = createInlineBlob(transactionHandle, blobId, 10);

        boolean added = cache.add(transaction, blob);
        assertFalse(added, "should not add blob");
        assertEquals(0, cache.size(), "size");
        assertThat("should not get blob", cache.getAndRemove(transaction, blobId), is(emptyOptional()));
        verify(transaction, never()).addTransactionListener(any());
    }

    @Test
    void add_doesNotAddBlobIfTransactionDoesNotMatch() {
        var cache = new InlineBlobCache(database);
        final int transaction1Handle = 4;
        final int transaction2Handle = 5;
        final long blobId = 6;
        FbTransaction transaction2 = createTransactionMock(transaction2Handle);
        InlineBlob blob = createInlineBlob(transaction1Handle, blobId, 10);

        boolean added = cache.add(transaction2, blob);
        assertFalse(added, "should not add blob with wrong transaction");
        assertEquals(0, cache.size(), "size");
        assertThat("should not get blob", cache.getAndRemove(transaction1Handle, blobId), is(emptyOptional()));
        assertThat("should not get blob", cache.getAndRemove(transaction2Handle, blobId), is(emptyOptional()));
        verify(transaction2, never()).addTransactionListener(any());
    }

    @Test
    void add_sameTransactionAndBlobId_notAdded() {
        var cache = new InlineBlobCache(database);
        final int transactionHandle = 5;
        final long blobId = 6;
        FbTransaction transaction = createTransactionMock(transactionHandle);
        final int blob1Size = 10;
        InlineBlob blob1 = createInlineBlob(transactionHandle, blobId, blob1Size);
        // same blobId, different size
        final int blob2Size = 100;
        InlineBlob blob2 = createInlineBlob(transactionHandle, blobId, blob2Size);

        assertTrue(cache.add(transaction, blob1), "should add blob1");
        assertEquals(blob1Size, cache.size(), "size after blob1");
        verify(transaction).addTransactionListener(cache);

        clearInvocations(transaction);
        assertFalse(cache.add(transaction, blob2), "should not add blob2");
        assertEquals(blob1Size, cache.size(), "size after blob2 unchanged");
        verify(transaction, never()).addTransactionListener(any());
    }

    @Test
    void add_sameBlobIdAndDifferentTransaction_addBoth() {
        var cache = new InlineBlobCache(database);
        final int blobId = 10;
        final int transactionHandle1 = 5;
        FbTransaction transaction1 = createTransactionMock(transactionHandle1);
        final int blob1Size = 10;
        InlineBlob blob1 = createInlineBlob(transactionHandle1, blobId, blob1Size);
        final int transactionHandle2 = 7;
        FbTransaction transaction2 = createTransactionMock(transactionHandle2);
        final int blob2Size = 100;
        InlineBlob blob2 = createInlineBlob(transactionHandle2, blobId, blob2Size);

        assertTrue(cache.add(transaction1, blob1), "should add blob1");
        assertEquals(blob1Size, cache.size(), "size after blob1");
        verify(transaction1).addTransactionListener(cache);

        assertTrue(cache.add(transaction2, blob2), "should add blob2");
        assertEquals(blob1Size + blob2Size, cache.size(), "size after blob2");
        verify(transaction2).addTransactionListener(cache);
    }

    @ParameterizedTest
    @EnumSource(value = TransactionState.class, names = { "PREPARED", "COMMITTED", "ROLLED_BACK" },
            mode = EnumSource.Mode.EXCLUDE )
    void transactionStateChangesThatDoNotRemoveBlobsForThatTransaction(TransactionState newState) {
        var cache = new InlineBlobCache(database);
        final int transaction1Handle = 1;
        FbTransaction transaction1 = createTransactionMock(transaction1Handle);
        List<InlineBlob> transaction1Blobs =
                LongStream.range(10, 20)
                        .mapToObj(blobId -> createInlineBlob(transaction1Handle, blobId, 10))
                        .toList();
        final int transaction2Handle = 2;
        FbTransaction transaction2 = createTransactionMock(transaction2Handle);
        List<InlineBlob> transaction2Blobs =
                LongStream.range(15, 30)
                        .mapToObj(blobId -> createInlineBlob(transaction2Handle, blobId, 10))
                        .toList();

        transaction1Blobs.forEach(blob -> cache.add(transaction1, blob));
        transaction2Blobs.forEach(blob -> cache.add(transaction2, blob));

        assertEquals((transaction1Blobs.size() + transaction2Blobs.size()) * 10, cache.size(), "size with all blobs");

        // This includes transitions that aren't valid, but the implementation only checks newState
        cache.transactionStateChanged(transaction1, newState, TransactionState.ACTIVE);

        assertEquals((transaction1Blobs.size() + transaction2Blobs.size()) * 10, cache.size(), "size with all blobs");
        assertTrue(transaction1Blobs.stream()
                        .allMatch(blob -> cache.contains(blob.getTransactionHandle(), blob.getBlobId())),
                "cache should contain all transaction1 blobs");
        assertTrue(transaction2Blobs.stream()
                        .allMatch(blob -> cache.contains(blob.getTransactionHandle(), blob.getBlobId())),
                "cache should contain all transaction2 blobs");
    }

    @ParameterizedTest
    @EnumSource(value = TransactionState.class, names = { "PREPARED", "COMMITTED", "ROLLED_BACK" })
    void transactionStateChangesThatRemoveBlobsForThatTransaction(TransactionState newState) {
        var cache = new InlineBlobCache(database);
        final int transaction1Handle = 1;
        FbTransaction transaction1 = createTransactionMock(transaction1Handle);
        List<InlineBlob> transaction1Blobs =
                LongStream.range(10, 20)
                        .mapToObj(blobId -> createInlineBlob(transaction1Handle, blobId, 10))
                        .toList();
        final int transaction2Handle = 2;
        FbTransaction transaction2 = createTransactionMock(transaction2Handle);
        List<InlineBlob> transaction2Blobs =
                LongStream.range(15, 30)
                        .mapToObj(blobId -> createInlineBlob(transaction2Handle, blobId, 10))
                        .toList();

        transaction1Blobs.forEach(blob -> cache.add(transaction1, blob));
        transaction2Blobs.forEach(blob -> cache.add(transaction2, blob));

        assertEquals((transaction1Blobs.size() + transaction2Blobs.size()) * 10, cache.size(), "size with all blobs");

        // This includes transitions that aren't valid, but the implementation only checks newState
        cache.transactionStateChanged(transaction1, newState, TransactionState.ACTIVE);

        assertEquals(transaction2Blobs.size() * 10, cache.size(), "size with only transaction2 blobs");
        assertTrue(transaction1Blobs.stream()
                .noneMatch(blob -> cache.contains(blob.getTransactionHandle(), blob.getBlobId())),
                "cache should not contain any transaction1 blobs");
        assertTrue(transaction2Blobs.stream()
                .allMatch(blob -> cache.contains(blob.getTransactionHandle(), blob.getBlobId())),
                "cache should contain all transaction2 blobs");
    }

    @Test
    void databaseDetached_clearsCacheAndMakesItImmutable() {
        var cache = new InlineBlobCache(database);
        final int transactionHandle = 1;
        final long blobId = 2;
        FbTransaction transaction = createTransactionMock(transactionHandle);
        cache.add(transaction, createInlineBlob(transactionHandle, blobId, 10));
        assertEquals(10, cache.size(), "size before detach");

        cache.detached(database);

        assertEquals(0, cache.size(), "size after detach");
        assertFalse(cache.contains(transactionHandle, blobId), "cache should not contain blob");

        assertThrows(UnsupportedOperationException.class,
                () -> cache.add(transaction, createInlineBlob(transactionHandle, blobId, 10)),
                "cache should be immutable after detach");
    }

    private FbTransaction createTransactionMock(int transactionHandle) {
        return createTransactionMock(transactionHandle, TransactionState.ACTIVE);
    }

    private FbTransaction createTransactionMock(int transactionHandle, TransactionState state) {
        var transaction = mock(FbTransaction.class);
        lenient().when(transaction.getHandle()).thenReturn(transactionHandle);
        lenient().when(transaction.getState()).thenReturn(state);
        return transaction;
    }

    private InlineBlob createInlineBlob(int transactionHandle, long blobId, int blobSize) {
        return new InlineBlob(database, blobId, transactionHandle, new byte[0], new byte[blobSize]);
    }

}