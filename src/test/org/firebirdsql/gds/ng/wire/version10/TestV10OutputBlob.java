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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10OutputBlob}. This test class can
 * be sub-classed for tests running on newer protocol versions.
 * <p>
 * Tests from this class are also copied to {@link org.firebirdsql.gds.ng.jna.TestJnaBlob} TODO: Consider refactoring test hierarchy
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10OutputBlob extends BaseTestV10Blob {

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

        assertTrue("Unexpected blob content", validateBlob(testId, baseContent, requiredSize));
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
            blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE, BlobParameterBuffer.TYPE_STREAM);
            writeBlob(testId, testBytes, db, blobParameterBuffer);
        }

        assertTrue("Unexpected blob content", validateBlob(testId, baseContent, requiredSize));
    }

    /**
     * Test if blob is not eof after open.
     */
    @Test
    public void testIsEof_afterOpen() throws Exception {
        try (FbWireDatabase db = createDatabaseConnection()) {
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
    public void testIsEof_afterClose() throws Exception {
        try (FbWireDatabase db = createDatabaseConnection()) {
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
    public void testIsEof_afterCancel() throws Exception {
        try (FbWireDatabase db = createDatabaseConnection()) {
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
    public void testUsingCancelledBlob() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_bad_segstr_id),
                message(startsWith(getFbMessage(ISCConstants.isc_bad_segstr_id)))
        ));

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
    public void testReopen() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_segstr_no_op),
                fbMessageEquals(ISCConstants.isc_segstr_no_op)
        ));

        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (FbWireDatabase db = createDatabaseConnection()) {
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
    public void testDoubleOpen() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_no_segstr_close),
                fbMessageEquals(ISCConstants.isc_no_segstr_close)
        ));

        try (FbWireDatabase db = createDatabaseConnection()) {
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
}
