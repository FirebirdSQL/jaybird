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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test for blobs in the JNA implementation.
 * <p>
 * This class has copied tests from {@link org.firebirdsql.gds.ng.wire.version10.V10OutputBlobTest} and
 * {@link org.firebirdsql.gds.ng.wire.version10.V10InputBlobTest}. TODO: Consider refactoring test hierarchy
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class JnaBlobTest extends BaseTestBlob {

    @RegisterExtension
    @Order(1)
    public static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    /**
     * Tests retrieval of a blob (what goes in is what comes out).
     */
    @Test
    void testInputBlobRetrieval() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        populateBlob(testId, baseContent, requiredSize);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                final FbBlob blob = db.createBlobForInput(transaction, null, blobId);
                blob.open();
                ByteArrayOutputStream bos = new ByteArrayOutputStream(requiredSize);
                while (!blob.isEof()) {
                    bos.write(blob.getSegment(blob.getMaximumSegmentSize()));
                }
                blob.close();
                statement.close();
                byte[] result = bos.toByteArray();
                assertEquals(requiredSize, result.length, "Unexpected length read from blob");
                assertTrue(validateBlobContent(result, baseContent, requiredSize), "Unexpected blob content");
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests absolute seek on a segmented blob. Expectation: fails with an exception
     */
    @Test
    void testInputBlobSeek_segmented() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        populateBlob(testId, baseContent, requiredSize);

        try (JnaDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                // NOTE: What matters is if the blob on the server is stream or segment
                final FbBlob blob = db.createBlobForInput(transaction, null, blobId);
                blob.open();
                int offset = baseContent.length / 2;

                SQLException exception = assertThrows(SQLException.class,
                        () -> blob.seek(offset, FbBlob.SeekMode.ABSOLUTE));
                assertThat(exception, allOf(
                        errorCodeEquals(ISCConstants.isc_bad_segstr_type),
                        message(startsWith(getFbMessage(ISCConstants.isc_bad_segstr_type)))));
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests absolute seek on a stream blob.
     */
    @Test
    void testInputBlobSeek_streamed() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 200;
        populateStreamBlob(testId, baseContent, requiredSize);

        try (JnaDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                // NOTE: What matters is if the blob on the server is stream or segment
                final FbBlob blob = db.createBlobForInput(transaction, null, blobId);
                blob.open();
                final int offset = requiredSize / 2;

                blob.seek(offset, FbBlob.SeekMode.ABSOLUTE);
                byte[] segment = blob.getSegment(100);
                byte[] expected = Arrays.copyOfRange(baseContent, offset, offset + 100);

                blob.close();
                statement.close();
                assertEquals(100, segment.length, "Unexpected length read from blob");
                assertArrayEquals(expected, segment, "Unexpected segment content");
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests reopen of input blob is allowed.
     */
    @Test
    void testInputBlobReopen() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        populateBlob(testId, baseContent, requiredSize);

        try (JnaDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                final FbBlob blob = db.createBlobForInput(transaction, null, blobId);
                blob.open();
                ByteArrayOutputStream bos = new ByteArrayOutputStream(requiredSize);
                while (!blob.isEof()) {
                    bos.write(blob.getSegment(blob.getMaximumSegmentSize()));
                }
                blob.close();
                // Reopen
                blob.open();
                bos = new ByteArrayOutputStream(requiredSize);
                while (!blob.isEof()) {
                    bos.write(blob.getSegment(blob.getMaximumSegmentSize()));
                }
                blob.close();

                statement.close();
                byte[] result = bos.toByteArray();
                assertEquals(requiredSize, result.length, "Unexpected length read from blob");
                assertTrue(validateBlobContent(result, baseContent, requiredSize), "Unexpected blob content");
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests double open of input blob is not allowed.
     */
    @Test
    void testInputBlobDoubleOpen() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        populateBlob(testId, baseContent, requiredSize);

        try (JnaDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                final FbBlob blob = db.createBlobForInput(transaction, null, blobId);
                blob.open();
                // Double open
                SQLException exception = assertThrows(SQLNonTransientException.class, blob::open);
                assertThat(exception, allOf(
                        errorCodeEquals(ISCConstants.isc_no_segstr_close),
                        fbMessageStartsWith(ISCConstants.isc_no_segstr_close)));
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests storage of a blob (what goes in is what comes out).
     */
    @Test
    void testOutputBlobStorage() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (JnaDatabase db = createDatabaseConnection()) {
            writeBlob(testId, testBytes, db, null);
        }

        assertTrue(validateBlob(testId, baseContent, requiredSize), "Unexpected blob content");
    }

    /**
     * Tests storage of a stream blob (what goes in is what comes out).
     */
    @Test
    void testOutputBlobStorage_Stream() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (JnaDatabase db = createDatabaseConnection()) {
            final BlobParameterBuffer blobParameterBuffer = db.createBlobParameterBuffer();
            blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE, BlobParameterBuffer.TYPE_STREAM);
            writeBlob(testId, testBytes, db, blobParameterBuffer);
        }

        assertTrue(validateBlob(testId, baseContent, requiredSize), "Unexpected blob content");
    }

    /**
     * Test if blob is not eof after open.
     */
    @Test
    void testOutputBlobIsEof_afterOpen() throws Exception {
        try (JnaDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForOutput(transaction, null);
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
    void testOutputBlobIsEof_afterClose() throws Exception {
        try (JnaDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForOutput(transaction, null);
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
    void testOutputBlobIsEof_afterCancel() throws Exception {
        try (JnaDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForOutput(transaction, null);
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
    void testOutputBlobUsingCancelledBlob() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (JnaDatabase db = createDatabaseConnection()) {
            final SimpleStatementListener listener = new SimpleStatementListener();
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbStatement statement = db.createStatement(transaction);
                statement.addStatementListener(listener);
                final FbBlob blob = db.createBlobForOutput(transaction, null);
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
                SQLException exception = assertThrows(SQLException.class, () ->statement.execute(rowValue));
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
    void testOutputBlobReopen() throws Exception {
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (JnaDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbBlob blob = db.createBlobForOutput(transaction, null);
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
    void testOutputBlobDoubleOpen() throws Exception {
        try (JnaDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbBlob blob = db.createBlobForOutput(transaction, null);
                blob.open();
                SQLException exception = assertThrows(SQLNonTransientException.class, blob::open);
                assertThat(exception, allOf(
                        errorCodeEquals(ISCConstants.isc_segstr_no_op),
                        fbMessageStartsWith(ISCConstants.isc_segstr_no_op)));
            } finally {
                transaction.commit();
            }
        }
    }

    @Override
    protected JnaDatabase createFbDatabase(FbConnectionProperties connectionInfo) throws SQLException {
        final JnaDatabase db = (JnaDatabase) factory.connect(connectionInfo);
        db.attach();
        return db;
    }

    @Override
    protected JnaDatabase createDatabaseConnection() throws SQLException {
        return (JnaDatabase) super.createDatabaseConnection();
    }
}
