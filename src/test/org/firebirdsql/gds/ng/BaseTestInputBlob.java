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

import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.getFbMessage;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Abstract test class for input blob related tests shared by the wire and JNA implementation.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public abstract class BaseTestInputBlob extends BaseTestBlob {

    /**
     * Tests retrieval of a blob (what goes in is what comes out).
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testBlobRetrieval(boolean useStreamBlobs) throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        populateBlob(testId, baseContent, requiredSize, useStreamBlobs);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                final FbBlob blob = db.createBlobForInput(transaction, blobId);
                blob.open();
                var bos = new ByteArrayOutputStream(requiredSize);
                while (!blob.isEof()) {
                    bos.write(blob.getSegment(blob.getMaximumSegmentSize()));
                }
                blob.close();
                statement.close();
                byte[] result = bos.toByteArray();
                assertBlobContent(result, baseContent, requiredSize);
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests retrieval of a blob (what goes in is what comes out).
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testBlobGet(boolean useStreamBlobs) throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple roundtrips are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        populateBlob(testId, baseContent, requiredSize, useStreamBlobs);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                FbBlob blob = db.createBlobForInput(transaction, blobId);
                blob.open();
                byte[] result = new byte[requiredSize];
                blob.get(result, 0, requiredSize);
                blob.close();
                statement.close();
                assertBlobContent(result, baseContent, requiredSize);
            } finally {
                if (transaction != null) transaction.commit();
            }
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
        populateSegmentedBlob(testId, baseContent, requiredSize);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                // NOTE: What matters is if the blob on the server is stream or segment
                final FbBlob blob = db.createBlobForInput(transaction, blobId);
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
    public void testBlobSeek_streamed() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 200;
        populateStreamBlob(testId, baseContent, requiredSize);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                // NOTE: What matters is if the blob on the server is stream or segment
                final FbBlob blob = db.createBlobForInput(transaction, blobId);
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
     * Tests reopen is allowed.
     */
    @Test
    public void testReopen() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        populateSegmentedBlob(testId, baseContent, requiredSize);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                final FbBlob blob = db.createBlobForInput(transaction, blobId);
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
                assertBlobContent(result, baseContent, requiredSize);
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    /**
     * Tests double open not allowed.
     */
    @Test
    public void testDoubleOpen() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        populateSegmentedBlob(testId, baseContent, requiredSize);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                final FbBlob blob = db.createBlobForInput(transaction, blobId);
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

    @Test
    public void readBlobIdZero() throws Exception {
        try (FbDatabase db = createDatabaseConnection()) {
            FbTransaction transaction = getTransaction(db);
            try {
                FbBlob blob = db.createBlobForInput(transaction, 0);
                blob.open();
                assertEquals(0, blob.length());
                byte[] segment = blob.getSegment(500);
                assertEquals(0, segment.length, "expected empty segment");
            } finally {
                transaction.commit();
            }
        }
    }

}
