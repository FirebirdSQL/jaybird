// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version19;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.AbstractFbWireInputBlob;
import org.firebirdsql.gds.ng.wire.InlineBlob;
import org.firebirdsql.gds.ng.wire.version18.V18StatementTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link V19Statement} in the V19 protocol, reuses test for V18.
 *
 * @author Mark Rotteveel
 * @since 7
 */
public class V19StatementTest extends V18StatementTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(19);

    protected static final String PRODUCE_BLOB = """
            execute block(SIZE integer = ?) returns (DATA blob)
            as
            declare variable CONTENT char(32767) character set ASCII;
            declare variable REMAINING integer;
            begin
              CONTENT = rpad('', 32767, 'x');
              REMAINING = SIZE;
              DATA = RDB$BLOB_UTIL.NEW_BLOB(false, true);
              while (REMAINING > 32767) do
              begin
                DATA = blob_append(DATA, CONTENT);
                REMAINING = REMAINING - 32767;
              end
              if (REMAINING > 0) then
              begin
                DATA = blob_append(DATA, substring(CONTENT from 1 for REMAINING));
              end
              suspend;
            end
            """;

    /**
     * Actual maximum size that will produce an inline blob when using {@link #PRODUCE_BLOB} to generate a blob.
     * <p>
     * When using a stream blob in the execute block, the append calls result in receiving multiple segments, as
     * Firebird uses the largest segment size used when writing the stream blob as the segment size when sending the
     * stream blob. This means that given we append in blocks of 32767 characters, with a max inline blob size of
     * {@code 65535}, the actual blob can be {@code 65531} bytes (max size minus {@code 2 * 2} bytes for the segment
     * lengths).
     * </p>
     * <p>
     * This is an implementation detail of existing Firebird versions, and it might change in the future.
     * </p>
     */
    protected static final int INLINE_BLOB_TEST_MAX_SIZE = 65531;

    protected V19CommonConnectionInfo commonConnectionInfo() {
        return new V19CommonConnectionInfo();
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 500, INLINE_BLOB_TEST_MAX_SIZE })
    public void usesInlineBlob_defaultMax(int size) throws Exception {
        assertFbBlob(size, InlineBlob.class);
    }

    @Test
    public void usesNormalBlob_defaultMax() throws Exception {
        // First size that will produce a "normal" blob
        final int size = INLINE_BLOB_TEST_MAX_SIZE + 1;
        // NOTE: We're intentionally not checking for the actual type (e.g. V11InputBlob) for flexibility
        assertFbBlob(size, AbstractFbWireInputBlob.class);
    }

    @Test
    public void usesInlineBlob_max16384() throws Exception {
        replaceDbHandleWithMaxInlineBlobSize(16384);
        final int size = 16384 - 2;
        assertFbBlob(size, InlineBlob.class);
    }

    @Test
    public void usesNormalBlob_max16384() throws Exception {
        replaceDbHandleWithMaxInlineBlobSize(16384);
        final int size = 16384 - 1;
        // NOTE: We're intentionally not checking for the actual type (e.g. V11InputBlob) for flexibility
        assertFbBlob(size, AbstractFbWireInputBlob.class);
    }

    private void assertFbBlob(int size, Class<? extends FbBlob> expectedType) throws SQLException {
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

        assertThat(blob, instanceOf(expectedType));
        assertBlobLengthAndContent(blob, size);
    }

    private static void assertBlobLengthAndContent(FbBlob blob, int size) throws SQLException {
        blob.open();
        assertEquals(size, blob.length(), "unexpected blob size");
        if (size > 0) {
            byte[] data = new byte[size];
            blob.get(data, 0, size);
            assertEquals("x".repeat(size), new String(data, StandardCharsets.US_ASCII));
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected final void replaceDbHandleWithMaxInlineBlobSize(int maxInlineBlobSize) throws SQLException {
        db.close();
        connectionInfo.setMaxInlineBlobSize(maxInlineBlobSize);
        db = createDatabase();
        db.attach();
    }

}
