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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.jaybird.fb.constants.BpbItems;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10OutputBlob}. This test class can
 * be sub-classed for tests running on newer protocol versions.
 * <p>
 * Tests from this class are also copied to {@code org.firebirdsql.gds.ng.jna.JnaBlobTest} TODO: Consider refactoring test hierarchy
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10OutputBlobTest extends BaseTestV10Blob {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(10);

    @RegisterExtension
    @Order(1)
    public static final GdsTypeExtension testType = GdsTypeExtension.excludesNativeOnly();

    @Override
    protected V10CommonConnectionInfo commonConnectionInfo() {
        return new V10CommonConnectionInfo();
    }

    /**
     * Tests storage of a blob (what goes in is what comes out).
     */
    @Test
    public void testBlobStorage() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (FbWireDatabase db = createDatabaseConnection()) {
            writeBlob(testId, testBytes, db, null);
        }

        assertTrue(validateBlob(testId, baseContent, requiredSize), "Unexpected blob content");
    }

    /**
     * Tests storage of a stream blob (what goes in is what comes out).
     */
    @Test
    public void testBlobStorage_Stream() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (FbWireDatabase db = createDatabaseConnection()) {
            final BlobParameterBuffer blobParameterBuffer = db.createBlobParameterBuffer();
            blobParameterBuffer.addArgument(BpbItems.isc_bpb_type, BpbItems.TypeValues.isc_bpb_type_stream);
            writeBlob(testId, testBytes, db, blobParameterBuffer);
        }

        assertTrue(validateBlob(testId, baseContent, requiredSize), "Unexpected blob content");
    }

    /**
     * Test if blob is not eof after open.
     */
    @Test
    public void testIsEof_afterOpen() throws Exception {
        try (FbWireDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForOutput(transaction);
                assumeTrue(blob.isEof(), "Output blob before open should be eof");

                blob.open();
                assertFalse(blob.isEof(), "Output blob after open should not be eof");
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Test if blob is eof after close.
     */
    @Test
    public void testIsEof_afterClose() throws Exception {
        try (FbWireDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForOutput(transaction);
                assumeTrue(blob.isEof(), "Output blob before open should be eof");
                blob.open();

                blob.close();
                assertTrue(blob.isEof(), "Output blob after close should be eof");
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Test if blob is eof after cancel.
     */
    @Test
    public void testIsEof_afterCancel() throws Exception {
        try (FbWireDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForOutput(transaction);
                assumeTrue(blob.isEof(), "Output blob before open should be eof");
                blob.open();

                blob.cancel();
                assertTrue(blob.isEof(), "Output blob after cancel should be eof");
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Test whether a cancelled blob cannot be used (indicating it was indeed cancelled).
     */
    @Test
    public void testUsingCancelledBlob() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (FbWireDatabase db = createDatabaseConnection()) {
            final SimpleStatementListener listener = new SimpleStatementListener();
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbStatement statement = db.createStatement(transaction);
                statement.addStatementListener(listener);
                final FbBlob blob = db.createBlobForOutput(transaction);
                blob.open();
                int bytesWritten = 0;
                while (bytesWritten < testBytes.length) {
                    // TODO the interface for writing blobs should be simpler
                    byte[] buffer = new byte[Math.min(blob.getMaximumSegmentSize(), testBytes.length - bytesWritten)];
                    System.arraycopy(testBytes, bytesWritten, buffer, 0, buffer.length);
                    blob.putSegment(buffer);
                    bytesWritten += buffer.length;
                }

                blob.cancel();

                statement.prepare(INSERT_BLOB_TABLE);
                final DatatypeCoder datatypeCoder = db.getDatatypeCoder();
                RowValue rowValue = RowValue.of(
                        datatypeCoder.encodeInt(testId),
                        datatypeCoder.encodeLong(blob.getBlobId()));

                SQLException exception = assertThrows(SQLException.class, () -> statement.execute(rowValue));
                assertThat(exception, allOf(
                        errorCodeEquals(ISCConstants.isc_bad_segstr_id),
                        message(startsWith(getFbMessage(ISCConstants.isc_bad_segstr_id)))));
                statement.close();
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Test reopen is not allowed.
     */
    @Test
    public void testReopen() throws Exception {
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (FbWireDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbBlob blob = db.createBlobForOutput(transaction);
                blob.open();
                int bytesWritten = 0;
                while (bytesWritten < testBytes.length) {
                    // TODO the interface for writing blobs should be simpler
                    byte[] buffer = new byte[Math.min(blob.getMaximumSegmentSize(), testBytes.length - bytesWritten)];
                    System.arraycopy(testBytes, bytesWritten, buffer, 0, buffer.length);
                    blob.putSegment(buffer);
                    bytesWritten += buffer.length;
                }
                blob.close();

                // Reopen
                SQLException exception = assertThrows(SQLNonTransientException.class, blob::open);
                assertThat(exception, allOf(
                        errorCodeEquals(ISCConstants.isc_segstr_no_op),
                        fbMessageStartsWith(ISCConstants.isc_segstr_no_op)));
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Test double open is not allowed.
     */
    @Test
    public void testDoubleOpen() throws Exception {
        try (FbWireDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbBlob blob = db.createBlobForOutput(transaction);
                blob.open();
                SQLException exception = assertThrows(SQLNonTransientException.class, blob::open);
                assertThat(exception, allOf(
                        errorCodeEquals(ISCConstants.isc_no_segstr_close),
                        fbMessageStartsWith(ISCConstants.isc_no_segstr_close)));
            } finally {
                transaction.commit();
            }
        }
    }
}
