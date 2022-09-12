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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBCachedBlob}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class FBCachedBlobTest {

    /**
     * Test if {@link FBCachedBlob#detach()} does not return itself.
     */
    @Test
    void testDetach() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[0]);

        assertNotSame(blob, blob.detach(), "FBCachedBlob.detach() should not return itself");
    }

    /**
     * Test if {@link FBCachedBlob#isSegmented()} return {@code false}.
     */
    @Test
    void testIsSegmented() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[0]);

        assertFalse(blob.isSegmented(), "FBCachedBlob.isSegmented() should return false");
    }

    /**
     * Test if {@link FBCachedBlob#length()} returns {@code -1} if data is {@code null}.
     */
    @Test
    void testLength_null() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(null);

        assertEquals(-1, blob.length(), "Unexpected length for null data");
    }

    /**
     * Test if {@link FBCachedBlob#length()} returns {@code 0} if data is an empty array.
     */
    @Test
    void testLength_0() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[0]);

        assertEquals(0, blob.length(), "Unexpected length for empty data");
    }

    /**
     * Test if {@link FBCachedBlob#length()} returns {@code 10} if data is a 10 byte array.
     */
    @Test
    void testLength_10() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[10]);

        assertEquals(10, blob.length(), "Unexpected length for data");
    }

    /**
     * Test {@link FBCachedBlob#getBytes(long, int)} with {@code pos = 0}.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    void testGetBytes_pos0() {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        SQLException exception = assertThrows(SQLException.class, () -> blob.getBytes(0, 0));
        assertThat(exception, allOf(
                message(equalTo("Expected value of pos > 0, got 0")),
                sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE))));
    }

    /**
     * Test {@link FBCachedBlob#getBytes(long, int)} with {@code length} negative.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    void testGetBytes_lengthNegative() {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        SQLException exception = assertThrows(SQLException.class, () -> blob.getBytes(1, -1));
        assertThat(exception, allOf(
                message(equalTo("Expected value of length >= 0, got -1")),
                sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE))));
    }

    /**
     * Test {@link FBCachedBlob#getBytes(long, int)} with {@code length = 0}.
     */
    @Test
    void testGetBytes_length0() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        byte[] data = blob.getBytes(1, 0);

        assertNotNull(data, "Expected non-null array");
        assertEquals(0, data.length, "Expected empty (zero-length) array");
    }

    /**
     * Test if {@link FBCachedBlob#getBytes(long, int)} returns {@code null} if data is null.
     * <p>
     * TODO: Not certain if this behavior is allowed!
     * </p>
     */
    @Test
    void testGetBytes_null() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(null);

        assertNull(blob.getBytes(1, 1));
    }

    /**
     * Test {@link FBCachedBlob#getBytes(long, int)}.
     */
    @Test
    void testGetBytes() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        byte[] data = blob.getBytes(5, 5);

        assertNotNull(data, "Expected non-null array");
        assertArrayEquals(new byte[] { 5, 6, 7, 8, 9 }, data, "Unexpected data");
    }

    /**
     * Test if {@link FBCachedBlob#position(byte[], long)} throws SQLFeatureNotSupportedException.
     */
    @Test
    void testPosition_byteArr_long() {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        assertThrows(SQLFeatureNotSupportedException.class, () -> blob.position(new byte[] { 3, 4 }, 1));
    }

    /**
     * Test if {@link FBCachedBlob#position(java.sql.Blob, long)} throws SQLFeatureNotSupportedException.
     */
    @Test
    void testPosition_Blob_long() {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        assertThrows(SQLFeatureNotSupportedException.class, () -> blob.position(blob, 1));
    }

    /**
     * Test if {@link FBCachedBlob#getBinaryStream()} returns {@code null} when data is {@code null}.
     */
    @Test
    void testGetBinaryStream_null() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(null);

        assertNull(blob.getBinaryStream());
    }

    /**
     * Test if {@link FBCachedBlob#getBinaryStream()} returns stream with all data.
     */
    @Test
    void testGetBinaryStream() throws Exception {
        final byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        FBCachedBlob blob = new FBCachedBlob(data);

        InputStream stream = blob.getBinaryStream();
        assertNotNull(stream);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int value;
        while ((value = stream.read()) != -1) {
            bos.write(value);
        }
        byte[] receivedData = bos.toByteArray();

        assertArrayEquals(data, receivedData, "Expected data and received data to be identical");
    }

    /**
     * Tests if {@link FBCachedBlob#getBinaryStream(long, long)} throws SQLFeatureNotSupportedException
     */
    @Test
    void testGetBinaryStream_long_long() {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        assertThrows(SQLFeatureNotSupportedException.class, () -> blob.getBinaryStream(1, 1));
    }

    /**
     * Test if {@link FBCachedBlob#setBytes(long, byte[])} throws an SQLException (read only).
     */
    @Test
    void testSetBytes_long_byteArr() {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        SQLException exception = assertThrows(SQLException.class, () -> blob.setBytes(1, new byte[] { 2 }));
        assertThat(exception, blobReadOnlySQLException());
    }

    /**
     * Test if {@link FBCachedBlob#setBytes(long, byte[], int, int)} throws an SQLException (read only).
     */
    @Test
    void testSetBytes_long_byteArr_int_int() {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        SQLException exception = assertThrows(SQLException.class, () -> blob.setBytes(1, new byte[] { 2 }, 0, 1));
        assertThat(exception, blobReadOnlySQLException());
    }

    /**
     * Test if {@link FBCachedBlob#setBinaryStream(long)} throws an SQLException (read only).
     */
    @Test
    void testSetBinaryStream() {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        //noinspection resource
        SQLException exception = assertThrows(SQLException.class, () -> blob.setBinaryStream(1));
        assertThat(exception, blobReadOnlySQLException());
    }

    /**
     * Test if {@link FBCachedBlob#truncate(long)} throws SQLFeatureNotSupportedException.
     */
    @Test
    void testTruncate() {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        assertThrows(SQLFeatureNotSupportedException.class, () -> blob.truncate(1));
    }

    /**
     * Test if {@link FBCachedBlob#free()} releases the data held internally.
     */
    @Test
    void testFree() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        assumeThat("Blob should have a length of 10", blob.length(), equalTo(10L));

        blob.free();

        SQLException exception = assertThrows(SQLException.class, blob::length);
        assertThat(exception, blobFreedSQLException());
    }

    private Matcher<SQLException> blobReadOnlySQLException() {
        return message(equalTo(FBCachedBlob.BLOB_READ_ONLY));
    }

    private Matcher<SQLException> blobFreedSQLException() {
        return fbMessageStartsWith(JaybirdErrorCodes.jb_blobClosed);
    }
}
