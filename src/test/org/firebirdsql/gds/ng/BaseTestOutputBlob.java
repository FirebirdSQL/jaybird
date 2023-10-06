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
package org.firebirdsql.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.jaybird.fb.constants.BpbItems;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.getFbMessage;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.firebirdsql.gds.ISCConstants.isc_bad_segstr_id;
import static org.firebirdsql.gds.ISCConstants.isc_no_segstr_close;
import static org.firebirdsql.gds.ISCConstants.isc_segstr_no_op;
import static org.firebirdsql.jaybird.fb.constants.BpbItems.TypeValues.isc_bpb_type_segmented;
import static org.firebirdsql.jaybird.fb.constants.BpbItems.TypeValues.isc_bpb_type_stream;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Abstract test class for output blob related tests shared by the wire and JNA implementation.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public abstract class BaseTestOutputBlob extends BaseTestBlob {

    /**
     * Tests storage of a blob (what goes in is what comes out).
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testBlobStorage(boolean useStreamBlobs) throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;

        try (FbDatabase db = createDatabaseConnection()) {
            final BlobParameterBuffer blobParameterBuffer = db.createBlobParameterBuffer();
            blobParameterBuffer.addArgument(BpbItems.isc_bpb_type,
                    useStreamBlobs ? isc_bpb_type_stream : isc_bpb_type_segmented);
            writeBlob(testId, generateBlobContent(baseContent, requiredSize), db, blobParameterBuffer);
        }

        assertTrue(validateBlob(testId, baseContent, requiredSize), "Unexpected blob content");
    }

    /**
     * Test if blob is not eof after open.
     */
    @Test
    public void testIsEof_afterOpen() throws Exception {
        try (FbDatabase db = createDatabaseConnection()) {
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
        try (FbDatabase db = createDatabaseConnection()) {
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
        try (FbDatabase db = createDatabaseConnection()) {
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

        try (FbDatabase db = createDatabaseConnection()) {
            final SimpleStatementListener listener = new SimpleStatementListener();
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbStatement statement = db.createStatement(transaction);
                statement.addStatementListener(listener);
                final FbBlob blob = db.createBlobForOutput(transaction);
                blob.open();
                blob.putSegment(testBytes);
                blob.cancel();

                statement.prepare(INSERT_BLOB_TABLE);
                final DatatypeCoder datatypeCoder = db.getDatatypeCoder();
                RowValue rowValue = RowValue.of(
                        datatypeCoder.encodeInt(testId),
                        datatypeCoder.encodeLong(blob.getBlobId()));

                SQLException exception = assertThrows(SQLException.class, () -> statement.execute(rowValue));
                assertThat(exception, allOf(
                        errorCodeEquals(isc_bad_segstr_id),
                        message(startsWith(getFbMessage(isc_bad_segstr_id)))));
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

        try (FbDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbBlob blob = db.createBlobForOutput(transaction);
                blob.open();
                blob.putSegment(testBytes);
                blob.close();

                // Reopen
                SQLException exception = assertThrows(SQLNonTransientException.class, blob::open);
                assertThat(exception, allOf(
                        errorCodeEquals(isc_segstr_no_op),
                        fbMessageStartsWith(isc_segstr_no_op)));
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
        try (FbDatabase db = createDatabaseConnection()) {
            final FbTransaction transaction = getTransaction(db);
            try {
                final FbBlob blob = db.createBlobForOutput(transaction);
                blob.open();
                SQLException exception = assertThrows(SQLNonTransientException.class, blob::open);
                // TODO Resolve difference between pure Java and native
                assertThat(exception, isPureJavaType().matches(FBTestProperties.GDS_TYPE)
                        ? allOf(errorCodeEquals(isc_no_segstr_close), fbMessageStartsWith(isc_no_segstr_close))
                        : allOf(errorCodeEquals(isc_segstr_no_op), fbMessageStartsWith(isc_segstr_no_op)));
            } finally {
                transaction.commit();
            }
        }
    }

}
