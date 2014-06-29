/*
 * $Id$
 *
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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.wire.BlobParameterBufferImp;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10InputBlob extends BaseTestV10Blob {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private SimpleStatementListener listener;
    private FbTransaction transaction;
    private FbStatement statement;

    /**
     * Queries the blob table for the blob id of the record with the specified (row) id.
     * @param testId Id of the row
     * @param db database to use
     * @return Blob id
     * @throws SQLException For errors executing the query
     */
    private long getBlobId(int testId, FbWireDatabase db) throws SQLException {
        listener = new SimpleStatementListener();
        transaction = getTransaction(db);
        statement = db.createStatement(transaction);
        statement.addStatementListener(listener);
        statement.allocateStatement();
        statement.prepare(SELECT_BLOB_TABLE);

        RowDescriptor descriptor = statement.getParameterDescriptor();
        FieldValue param1 = new FieldValue(descriptor.getFieldDescriptor(0), XSQLVAR.intToBytes(testId));

        statement.execute(RowValue.of(param1));

        assertEquals("Expected hasResultSet to be set to true", Boolean.TRUE, listener.hasResultSet());

        statement.fetchRows(1);
        assertEquals("Expected a row", 1, listener.getRows().size());

        RowValue row = listener.getRows().get(0);
        FieldValue blobIdFieldValue = row.getFieldValue(0);
        return new XSQLVAR().decodeLong(blobIdFieldValue.getFieldData());
    }

    /**
     * Tests retrieval of a blob (what goes in is what comes out).
     */
    @Test
    public void testBlobRetrieval() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        populateBlob(testId, baseContent, requiredSize);

        final FbWireDatabase db = createDatabaseConnection();
        try {
            long blobId = getBlobId(testId, db);

            final FbBlob blob = db.createBlobForInput(transaction, null, blobId);
            blob.open();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(requiredSize);
            while (!blob.isEof()) {
                bos.write(blob.getSegment(blob.getMaximumSegmentSize()));
            }
            blob.close();
            transaction.commit();
            statement.close();
            byte[] result = bos.toByteArray();
            assertEquals("Unexpected length read from blob", requiredSize, result.length);
            assertTrue("Unexpected blob content", validateBlobContent(result, baseContent, requiredSize));
        } finally {
            db.detach();
        }
    }

    /**
     * Tests absolute seek on a segmented blob. Expectation: fails with an exception
     */
    @Test
    public void testBlobSeek_segmented() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        populateBlob(testId, baseContent, requiredSize);

        final FbWireDatabase db = createDatabaseConnection();
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
            db.detach();
        }
    }

    /**
     * Tests absolute seek on a stream blob.
     */
    @Test
    public void testBlobSeek() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 200;
        populateStreamBlob(testId, baseContent, requiredSize);

        final FbWireDatabase db = createDatabaseConnection();
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
            transaction.commit();
            statement.close();
            assertEquals("Unexpected length read from blob", 100, segment.length);
            assertArrayEquals("Unexpected segment content", expected, segment);
        } finally {
            db.detach();
        }
    }

    /**
     * Populates a stream blob for testing.
     *
     * @param testId Id of the record to be inserted.
     * @throws SQLException
     */
    private void populateStreamBlob(int testId, byte[] baseContent, int requiredSize) throws SQLException {
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        final FbWireDatabase db = createDatabaseConnection();
        try {
            listener = new SimpleStatementListener();
            transaction = getTransaction(db);
            statement = db.createStatement(transaction);
            statement.addStatementListener(listener);
            statement.allocateStatement();

            final BlobParameterBuffer blobParameterBuffer = new BlobParameterBufferImp();
            blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE, BlobParameterBuffer.TYPE_STREAM);
            final FbBlob blob = db.createBlobForOutput(transaction, blobParameterBuffer);
            blob.open();
            int bytesWritten = 0;
            while (bytesWritten < testBytes.length) {
                byte[] buffer = new byte[Math.min(blob.getMaximumSegmentSize(), testBytes.length - bytesWritten)];
                System.arraycopy(testBytes, bytesWritten, buffer, 0, buffer.length);
                blob.putSegment(buffer);
                bytesWritten += buffer.length;
            }
            blob.close();

            statement.prepare(INSERT_BLOB_TABLE);
            RowDescriptor descriptor = statement.getParameterDescriptor();
            FieldValue param1 = new FieldValue(descriptor.getFieldDescriptor(0), XSQLVAR.intToBytes(testId));
            FieldValue param2 = new FieldValue(descriptor.getFieldDescriptor(1), XSQLVAR.longToBytes(blob.getBlobId()));
            statement.execute(RowValue.of(param1, param2));
            statement.close();
            transaction.commit();
        } finally {
            db.detach();
        }
    }

    /**
     * Tests reopen is allowed.
     */
    @Test
    public void testReopen() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        populateBlob(testId, baseContent, requiredSize);

        final FbWireDatabase db = createDatabaseConnection();
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

            transaction.commit();
            statement.close();
            byte[] result = bos.toByteArray();
            assertEquals("Unexpected length read from blob", requiredSize, result.length);
            assertTrue("Unexpected blob content", validateBlobContent(result, baseContent, requiredSize));
        } finally {
            db.detach();
        }
    }

    /**
     * Tests double open not allowed.
     */
    @Test
    public void testDoubleOpen() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_op),
                fbMessageEquals(ISCConstants.isc_segstr_no_op)
        ));

        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        populateBlob(testId, baseContent, requiredSize);

        final FbWireDatabase db = createDatabaseConnection();
        try {
            long blobId = getBlobId(testId, db);

            final FbBlob blob = db.createBlobForInput(transaction, null, blobId);
            blob.open();
            // Double open
            blob.open();
        } finally {
            db.detach();
        }
    }
}
