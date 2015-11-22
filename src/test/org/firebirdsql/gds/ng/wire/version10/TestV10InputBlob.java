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

import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.*;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10InputBlob}. This test class can
 * be sub-classed for tests running on newer protocol versions.
 * <p>
 * Tests from this class are also copied to {@link org.firebirdsql.gds.ng.jna.TestJnaBlob} TODO: Consider refactoring test hierarchy
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10InputBlob extends BaseTestV10Blob {

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(10);

    public TestV10InputBlob() {
        this(new V10CommonConnectionInfo());
    }

    protected TestV10InputBlob(V10CommonConnectionInfo commonConnectionInfo) {
        super(commonConnectionInfo);
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

        try (FbWireDatabase db = createDatabaseConnection()) {
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
    public void testBlobSeek_segmented() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 4 * Short.MAX_VALUE;
        populateBlob(testId, baseContent, requiredSize);

        try (FbWireDatabase db = createDatabaseConnection()) {
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
    public void testBlobSeek_streamed() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        // Use sufficiently large value so that multiple segments are used
        final int requiredSize = 200;
        populateStreamBlob(testId, baseContent, requiredSize);

        try (FbWireDatabase db = createDatabaseConnection()) {
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
     * Tests reopen is allowed.
     */
    @Test
    public void testReopen() throws Exception {
        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        populateBlob(testId, baseContent, requiredSize);

        try (FbWireDatabase db = createDatabaseConnection()) {
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
     * Tests double open not allowed.
     */
    @Test
    public void testDoubleOpen() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(allOf(
                errorCodeEquals(ISCConstants.isc_no_segstr_close),
                fbMessageEquals(ISCConstants.isc_no_segstr_close)
        ));

        final int testId = 1;
        final byte[] baseContent = generateBaseContent();
        final int requiredSize = 256;
        populateBlob(testId, baseContent, requiredSize);

        try (FbWireDatabase db = createDatabaseConnection()) {
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
}
