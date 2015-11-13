/*
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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Test for blobs in the JNA implementation.
 * <p>
 * This class has copied tests from {@link org.firebirdsql.gds.ng.wire.version10.TestV10OutputBlob} and
 * {@link org.firebirdsql.gds.ng.wire.version10.TestV10InputBlob}. TODO: Consider refactoring test hierarchy
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestJnaBlob extends BaseTestBlob {

    private final FbClientDatabaseFactory factory = new FbClientDatabaseFactory();

    /**
     * Tests retrieval of a blob (what goes in is what comes out).
     */
    @Test
    public void testInputBlobRetrieval() throws Exception {
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
                assertEquals("Unexpected length read from blob", requiredSize, result.length);
                assertTrue("Unexpected blob content", validateBlobContent(result, baseContent, requiredSize));
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests absolute seek on a segmented blob. Expectation: fails with an exception
     */
    @Test
    public void testInputBlobSeek_segmented() throws Exception {
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

                expectedException.expect(SQLException.class);
                expectedException.expect(allOf(
                        errorCodeEquals(ISCConstants.isc_bad_segstr_type),
                        message(startsWith(getFbMessage(ISCConstants.isc_bad_segstr_type)))
                ));

                blob.seek(offset, FbBlob.SeekMode.ABSOLUTE);
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests absolute seek on a stream blob.
     */
    @Test
    public void testInputBlobSeek_streamed() throws Exception {
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
                assertEquals("Unexpected length read from blob", 100, segment.length);
                assertArrayEquals("Unexpected segment content", expected, segment);
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests reopen of input blob is allowed.
     */
    @Test
    public void testInputBlobReopen() throws Exception {
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
                assertEquals("Unexpected length read from blob", requiredSize, result.length);
                assertTrue("Unexpected blob content", validateBlobContent(result, baseContent, requiredSize));
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests double open of input blob is not allowed.
     */
    @Test
    public void testInputBlobDoubleOpen() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_no_segstr_close),
                fbMessageEquals(ISCConstants.isc_no_segstr_close)
        ));

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
                blob.open();
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests storage of a blob (what goes in is what comes out).
     */
    @Test
    public void testOutputBlobStorage() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (JnaDatabase db = createDatabaseConnection()) {
            writeBlob(testId, testBytes, db, null);
        }

        assertTrue("Unexpected blob content", validateBlob(testId, baseContent, requiredSize));
    }

    /**
     * Tests storage of a stream blob (what goes in is what comes out).
     */
    @Test
    public void testOutputBlobStorage_Stream() throws Exception {
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

        assertTrue("Unexpected blob content", validateBlob(testId, baseContent, requiredSize));
    }

    /**
     * Test if blob is not eof after open.
     */
    @Test
    public void testOutputBlobIsEof_afterOpen() throws Exception {
        try (JnaDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForOutput(transaction, null);
                assumeTrue("Output blob before open should be eof", blob.isEof());

                blob.open();
                assertFalse("Output blob after open should not be eof", blob.isEof());
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Test if blob is eof after close.
     */
    @Test
    public void testOutputBlobIsEof_afterClose() throws Exception {
        try (JnaDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForOutput(transaction, null);
                assumeTrue("Output blob before open should be eof", blob.isEof());
                blob.open();

                blob.close();
                assertTrue("Output blob after close should be eof", blob.isEof());
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Test if blob is eof after cancel.
     */
    @Test
    public void testOutputBlobIsEof_afterCancel() throws Exception {
        try (JnaDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForOutput(transaction, null);
                assumeTrue("Output blob before open should be eof", blob.isEof());
                blob.open();

                blob.cancel();
                assertTrue("Output blob after cancel should be eof", blob.isEof());
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Test whether a cancelled blob cannot be used (indicating it was indeed cancelled).
     */
    @Test
    public void testOutputBlobUsingCancelledBlob() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_bad_segstr_id),
                message(startsWith(getFbMessage(ISCConstants.isc_bad_segstr_id)))
        ));

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
                FieldValue param1 = new FieldValue(datatypeCoder.encodeInt(testId));
                FieldValue param2 = new FieldValue(datatypeCoder.encodeLong(blob.getBlobId()));
                statement.execute(RowValue.of(param1, param2));
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
    public void testOutputBlobReopen() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_op),
                fbMessageEquals(ISCConstants.isc_segstr_no_op)
        ));

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
                blob.open();
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Test double open is not allowed.
     */
    @Test
    public void testOutputBlobDoubleOpen() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_op),
                fbMessageEquals(ISCConstants.isc_segstr_no_op)
        ));

        try (JnaDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbBlob blob = db.createBlobForOutput(transaction, null);
                blob.open();
                blob.open();
            } finally {
                transaction.commit();
            }
        }
    }

    @Override
    protected JnaDatabase createFbDatabase(FbConnectionProperties connectionInfo) throws SQLException {
        final JnaDatabase db = factory.connect(connectionInfo);
        db.attach();
        return db;
    }

    @Override
    protected JnaDatabase createDatabaseConnection() throws SQLException {
        return (JnaDatabase) super.createDatabaseConnection();
    }
}
