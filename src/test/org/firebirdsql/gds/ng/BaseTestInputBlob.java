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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertAll;
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
     * Blob size sufficiently large that multiple segments are used.
     */
    protected static final int MULTI_SEGMENT_SIZE = 4 * Short.MAX_VALUE;

    /**
     * Tests retrieval of a blob (what goes in is what comes out).
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testBlobRetrieval(boolean useStreamBlobs) throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        populateBlob(testId, baseContent, MULTI_SEGMENT_SIZE, useStreamBlobs);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                try (FbBlob blob = db.createBlobForInput(transaction, blobId)) {
                    blob.open();
                    var bos = new ByteArrayOutputStream(MULTI_SEGMENT_SIZE);
                    while (!blob.isEof()) {
                        bos.write(blob.getSegment(blob.getMaximumSegmentSize()));
                    }
                    byte[] result = bos.toByteArray();
                    assertBlobContent(result, baseContent, MULTI_SEGMENT_SIZE);
                } finally {
                    statement.close();
                }
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
        populateBlob(testId, baseContent, MULTI_SEGMENT_SIZE, useStreamBlobs);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                try (FbBlob blob = db.createBlobForInput(transaction, blobId)) {
                    blob.open();
                    byte[] result = new byte[MULTI_SEGMENT_SIZE];
                    assertEquals(MULTI_SEGMENT_SIZE, blob.get(result, 0, MULTI_SEGMENT_SIZE));
                    assertBlobContent(result, baseContent, MULTI_SEGMENT_SIZE);
                } finally {
                    statement.close();
                }
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    @Test
    public void testBlobGet_minFillFactor_outOfRange() throws Exception {
        try (FbDatabase db = createDatabaseConnection()) {
            FbTransaction transaction = getTransaction(db);
            try (FbBlob blob = db.createBlobForInput(transaction, 0)) {
                blob.open();

                byte[] segment = new byte[1];
                assertAll(
                        () -> assertThrows(SQLNonTransientException.class, () -> blob.get(segment, 0, 1, 0f)),
                        () -> assertThrows(SQLNonTransientException.class,
                                () -> blob.get(segment, 0, 1, 0f - Math.ulp(0f))),
                        () -> assertThrows(SQLNonTransientException.class,
                                () -> blob.get(segment, 0, 1, 1f + Math.ulp(1f))));
            } finally {
                transaction.commit();
            }
        }
    }

    @Test
    public void testBlobGet_minFillFactor_inRange() throws Exception {
        final int testId = 1;
        populateStreamBlob(testId, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, 10);
        try (FbDatabase db = createDatabaseConnection()) {
            long blobId = getBlobId(testId, db);
            try (FbBlob blob = db.createBlobForInput(transaction, blobId)) {
                blob.open();
                byte[] segment = new byte[1];
                assertEquals(0, blob.get(segment, 0, 0, 0f + Math.ulp(0f)),
                        "fetch with length 0 should allow minFillFactor just greater than 0f");
                assertEquals(0, blob.get(segment, 0, 0, 1f), "fetch with length 0 should allow minFillFactor = 1f");
                assertEquals(1, blob.get(segment, 0, 1, 0f + Math.ulp(0f)),
                        "fetch with length 1 should allow minFillFactor just greater than 0f");
                assertEquals(1, segment[0]);
                assertEquals(1, blob.get(segment, 0, 1, 1f), "fetch with length 1 should allow minFillFactor = 1f");
                assertEquals(2, segment[0]);
                segment = new byte[8];
                assertEquals(8, blob.get(segment, 0, 8, 0.1f),
                        "fetch with length 8 and minFillFactor = 0.1f should fetch remainder");
                assertArrayEquals(new byte[] { 3, 4, 5, 6, 7, 8, 9, 10 }, segment);
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Tests retrieval of a blob (what goes in is what comes out).
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testBlobGet_withMinFillFactor(boolean useStreamBlobs) throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        populateBlob(testId, baseContent, MULTI_SEGMENT_SIZE, useStreamBlobs);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                try (FbBlob blob = db.createBlobForInput(transaction, blobId)) {
                    blob.open();
                    final int maximumSegmentSize = blob.getMaximumSegmentSize();
                    byte[] result = new byte[(int) (maximumSegmentSize * 1.05f)];
                    int readBytes = blob.get(result, 0, result.length, 0.9f);
                    assertThat(readBytes, allOf(
                            greaterThanOrEqualTo((int) (0.9f * result.length)),
                            /* NOTE: for pure Java, the max segment size is the determining factor, but for native it is
                             (multiples of) blobBufferSize. Fudging it a bit, so the result works both for pure Java and
                             native (with blobBufferSize=16384) */
                            lessThanOrEqualTo(maximumSegmentSize + 1)));
                    assertBlobContent(Arrays.copyOf(result, readBytes), baseContent, readBytes);
                } finally {
                    statement.close();
                }
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
        populateSegmentedBlob(testId, baseContent, MULTI_SEGMENT_SIZE);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                // NOTE: What matters is if the blob on the server is stream or segment
                try (FbBlob blob = db.createBlobForInput(transaction, blobId)) {
                    blob.open();
                    int offset = baseContent.length / 2;

                    SQLException exception = assertThrows(SQLException.class,
                            () -> blob.seek(offset, FbBlob.SeekMode.ABSOLUTE));
                    assertThat(exception, allOf(
                            errorCodeEquals(ISCConstants.isc_bad_segstr_type),
                            message(startsWith(getFbMessage(ISCConstants.isc_bad_segstr_type)))));
                }
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
        final int requiredSize = 200;
        populateStreamBlob(testId, baseContent, requiredSize);

        try (FbDatabase db = createDatabaseConnection()) {
            try {
                long blobId = getBlobId(testId, db);

                // NOTE: What matters is if the blob on the server is stream or segment
                try (FbBlob blob = db.createBlobForInput(transaction, blobId)) {
                    blob.open();
                    final int offset = requiredSize / 2;
                    blob.seek(offset, FbBlob.SeekMode.ABSOLUTE);
                    byte[] segment = blob.getSegment(100);
                    assertEquals(100, segment.length, "Unexpected length read from blob");
                    assertArrayEquals(Arrays.copyOfRange(baseContent, offset, offset + 100), segment,
                            "Unexpected segment content");
                } finally {
                    statement.close();
                }

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

                try (FbBlob blob = db.createBlobForInput(transaction, blobId)) {
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
                    byte[] result = bos.toByteArray();
                    assertBlobContent(result, baseContent, requiredSize);
                } finally {
                    statement.close();
                }
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

                try (FbBlob blob = db.createBlobForInput(transaction, blobId)) {
                    blob.open();
                    // Double open
                    SQLException exception = assertThrows(SQLNonTransientException.class, blob::open);
                    assertThat(exception, allOf(
                            errorCodeEquals(ISCConstants.isc_no_segstr_close),
                            fbMessageStartsWith(ISCConstants.isc_no_segstr_close)));
                }
            } finally {
                if (transaction != null) transaction.commit();
            }
        }
    }

    @Test
    public void readBlobIdZero() throws Exception {
        try (FbDatabase db = createDatabaseConnection()) {
            FbTransaction transaction = getTransaction(db);
            try (FbBlob blob = db.createBlobForInput(transaction, 0)) {
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
