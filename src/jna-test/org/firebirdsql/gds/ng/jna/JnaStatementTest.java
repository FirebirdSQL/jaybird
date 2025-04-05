// SPDX-FileCopyrightText: Copyright 2014-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.AbstractStatementTest;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.firebirdsql.common.FbAssumptions.assumeFeature;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isOtherNativeType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.gds.ISCConstants.fb_info_wire_rcv_bytes;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for JNA statement.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class JnaStatementTest extends AbstractStatementTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

    /**
     * See also {@code INLINE_BLOB_TEST_MAX_SIZE} in {@link org.firebirdsql.gds.ng.wire.version19.V19StatementTest}.
     */
    protected static final int INLINE_BLOB_TEST_MAX_SIZE = 65531;

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    @Override
    protected Class<? extends FbDatabase> getExpectedDatabaseType() {
        return JnaDatabase.class;
    }

    @Override
    protected FbDatabase createDatabase() throws SQLException {
        return factory.connect(connectionInfo);
    }

    @Test
    @Override
    public void testSelect_NoParameters_Execute_and_Fetch() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");
    }

    @Test
    public void testMultipleExecute() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.closeCursor();
        final SimpleStatementListener statementListener2 = new SimpleStatementListener();
        statement.addStatementListener(statementListener2);
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener2.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener2.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener2.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener2.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener2.getRows().size(), "Expected a single row to have been fetched");
    }

    @Test
    public void testMultiplePrepare() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener.getRows().size(), "Expected a single row to have been fetched");

        statement.closeCursor();

        statement.prepare(SELECT_FROM_RDB$DATABASE);

        final SimpleStatementListener statementListener2 = new SimpleStatementListener();
        statement.addStatementListener(statementListener2);
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, statementListener2.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, statementListener2.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, statementListener2.getRows().size(), "Expected no rows to be fetched yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertNotEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, statementListener2.getRows().size(), "Expected a single row to have been fetched");

        statement.fetchRows(1);

        assertEquals(Boolean.TRUE, statementListener2.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, statementListener2.getRows().size(), "Expected a single row to have been fetched");
    }

    @Test
    public void testSelect_WithParameters_Execute_and_Fetch() throws Exception {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare(SELECT_CHARSET_BY_ID_OR_SIZE);

        final DatatypeCoder coder = db.getDatatypeCoder();
        RowValue rowValue = RowValue.of(
                coder.encodeShort(3),  // smallint = 3 (id of UNICODE_FSS)
                coder.encodeShort(1)); // smallint = 1 (single byte character sets)

        statement.execute(rowValue);

        assertEquals(Boolean.TRUE, listener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, listener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");
        assertNull(listener.getSqlCounts(), "Expected no SQL counts yet");

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(100);

        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to haven't been called yet");
        assertEquals(1, listener.getRows().size(), "Expected a single row to have been fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected a single row to have been fetched");

        // 100 should be sufficient to fetch all character sets; limit to prevent infinite loop with bugs in fetchRows
        int count = 0;
        while(listener.isAfterLast() != Boolean.TRUE && count < 100) {
            statement.fetchRows(1);
            count++;
        }

        assertEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to be set to true");
        // Number is database dependent (unicode_fss + all single byte character sets)
        assertTrue(listener.getRows().size() > 2, "Expected more than two rows");
        assertEquals(0, listener.getLastFetchCount(), "Expected the last fetch to have produced no row");

        assertNull(listener.getSqlCounts(), "expected no SQL counts immediately after retrieving all rows");

        statement.getSqlCounts();

        assertNotNull(listener.getSqlCounts(), "Expected SQL counts");
        assertEquals(listener.getRows().size(), listener.getSqlCounts().selectCount(), "Unexpected select count");
    }

    // NOTE The following tests are similar to the tests in V19StatementTest with the same name. In case of the JNA
    // tests, they might have been more appropriate in JnaBlobTest, but for consistency with the pure Java tests, we put
    // them here.

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 500, INLINE_BLOB_TEST_MAX_SIZE })
    public void usesInlineBlob_defaultMax(int size) throws Exception {
        assumeInlineBlobSupport();
        assertFbBlob(size, true);
    }

    @Test
    public void usesNormalBlob_defaultMax() throws Exception {
        assumeInlineBlobSupport();
        // First size that will produce a "normal" blob
        final int size = INLINE_BLOB_TEST_MAX_SIZE + 1;
        assertFbBlob(size, false);
    }

    @Test
    public void usesInlineBlob_max16384() throws Exception {
        assumeInlineBlobSupport();
        replaceDbHandleWithInlineBlobConfig(16384, null);
        final int size = 16384 - 2;
        assertFbBlob(size, true);
    }

    @Test
    public void usesNormalBlob_max16384() throws Exception {
        assumeInlineBlobSupport();
        replaceDbHandleWithInlineBlobConfig(16384, null);
        final int size = 16384 - 1;
        assertFbBlob(size, false);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void usesNormalBlob_cacheSizeZero(int size) throws Exception {
        assumeInlineBlobSupport();
        replaceDbHandleWithInlineBlobConfig(null, 0);
        assertFbBlob(size, false);
    }

    private void assumeInlineBlobSupport() {
        assumeThat("Test requires non-embedded type", FBTestProperties.GDS_TYPE, isOtherNativeType());
        // Formally, we should also check if we make a TCP/IP connection
        assumeFeature(FirebirdSupportInfo::supportsInlineBlobs, "Test requires inline blob support");
        GDSServerVersion clientVersion = ((JnaDatabase) db).getClientVersion();
        assumeTrue(clientVersion.isEqualOrAbove(5, 0, 3),
                "Test requires fbclient version supporting inline blobs, was: " + clientVersion);
    }

    private void assertFbBlob(int size, boolean expectInlineBlob) throws SQLException {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare(PRODUCE_BLOB);

        final DatatypeCoder coder = db.getDatatypeCoder();
        var params = RowValue.of(coder.encodeInt(size));
        statement.execute(params);
        statement.fetchRows(1);

        assertThat("expected a row", listener.getRows(), hasSize(1));
        RowValue rowValue = listener.getRows().get(0);
        long blobId = coder.decodeLong(rowValue.getFieldData(0));
        FbBlob blob = db.createBlobForInput(getOrCreateTransaction(), blobId);

        assertBlobLengthAndContent(blob, size, expectInlineBlob);
    }

    private void assertBlobLengthAndContent(FbBlob blob, int size, boolean expectInlineBlob) throws SQLException {
        long receivedBytesBefore = getReceivedBytes();
        blob.open();
        assertEquals(size, blob.length(), "unexpected blob size");
        if (size > 0) {
            byte[] data = new byte[size];
            blob.get(data, 0, size);
            assertEquals("x".repeat(size), new String(data, StandardCharsets.US_ASCII));
        }
        long receivedBytesAfter = getReceivedBytes();
        long receivedBytesDifference = receivedBytesAfter - receivedBytesBefore;
        if (expectInlineBlob) {
            assertEquals(0L, receivedBytesDifference, "expected an inline blob, so no received network data");
        } else {
            // For server-side blob, more bytes than size will have been received (i.e. open, get segment, etc.)
            assertThat("expected a server-side blob", receivedBytesDifference, greaterThan((long) size));
        }
    }

    private long getReceivedBytes() throws SQLException {
        return db.getDatabaseInfo(new byte[] { (byte) fb_info_wire_rcv_bytes, isc_info_end }, 20, infoResponse -> {
            var reader = new ClumpletReader(ClumpletReader.Kind.InfoResponse, infoResponse);
            if (reader.find(fb_info_wire_rcv_bytes)) {
                return reader.getLong();
            }
            throw new IllegalStateException(
                    "Did not receive item fb_info_wire_rcv_bytes (157), this is probably a 5.0.1 or older fbclient");
        });
    }

}
