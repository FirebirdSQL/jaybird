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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBCachedBlob}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBCachedBlob {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    /**
     * Test if {@link FBCachedBlob#detach()} does not return itself.
     */
    @Test
    public void testDetach() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[0]);

        assertNotSame("FBCachedBlob.detach() should not return itself", blob, blob.detach());
    }

    /**
     * Test if {@link FBCachedBlob#isSegmented()} return <code>false</code>.
     */
    @Test
    public void testIsSegmented() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[0]);

        assertFalse("FBCachedBlob.isSegmented() should return false", blob.isSegmented());
    }

    /**
     * Test if {@link FBCachedBlob#length()} returns <code>-1</code> if data is <code>null</code>.
     */
    @Test
    public void testLength_null() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(null);

        assertEquals("Unexpected length for null data", -1, blob.length());
    }

    /**
     * Test if {@link FBCachedBlob#length()} returns <code>0</code> if data is an empty array.
     */
    @Test
    public void testLength_0() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[0]);

        assertEquals("Unexpected length for empty data", 0, blob.length());
    }

    /**
     * Test if {@link FBCachedBlob#length()} returns <code>10</code> if data is a 10 byte array.
     */
    @Test
    public void testLength_10() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[10]);

        assertEquals("Unexpected length for data", 10, blob.length());
    }

    /**
     * Test {@link FBCachedBlob#getBytes(long, int)} with <code>pos = 0</code>.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    public void testGetBytes_pos0() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        expectedException.expect(allOf(
                isA(SQLException.class),
                message(equalTo("Expected value of pos > 0, got 0")),
                sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE))));

        blob.getBytes(0, 0);
    }

    /**
     * Test {@link FBCachedBlob#getBytes(long, int)} with <code>length</code> negative.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    public void testGetBytes_lengthNegative() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        expectedException.expect(allOf(
                        isA(SQLException.class),
                        message(equalTo("Expected value of length >= 0, got -1")),
                        sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE)))
        );

        blob.getBytes(1, -1);
    }

    /**
     * Test {@link FBCachedBlob#getBytes(long, int)} with <code>length = 0</code>.
     */
    @Test
    public void testGetBytes_length0() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        byte[] data = blob.getBytes(1, 0);

        assertNotNull("Expected non-null array", data);
        assertEquals("Expected empty (zero-length) array", 0, data.length);
    }

    /**
     * Test if {@link FBCachedBlob#getBytes(long, int)} returns <code>null</code> if data is null.
     * <p>
     * TODO: Not certain if this behavior is allowed!
     * </p>
     */
    @Test
    public void testGetBytes_null() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(null);

        assertNull(blob.getBytes(1, 1));
    }

    /**
     * Test {@link FBCachedBlob#getBytes(long, int)}.
     */
    @Test
    public void testGetBytes() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        byte[] data = blob.getBytes(5, 5);

        assertNotNull("Expected non-null array", data);
        assertArrayEquals("Unexpected data", new byte[] { 5, 6, 7, 8, 9 }, data);
    }

    /**
     * Test if {@link FBCachedBlob#position(byte[], long)} throws SQLFeatureNotSupportedException.
     */
    @Test
    public void testPosition_byteArr_long() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        expectedException.expect(SQLFeatureNotSupportedException.class);

        blob.position(new byte[] { 3, 4 }, 1);
    }

    /**
     * Test if {@link FBCachedBlob#position(java.sql.Blob, long)} throws SQLFeatureNotSupportedException.
     */
    @Test
    public void testPosition_Blob_long() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        expectedException.expect(SQLFeatureNotSupportedException.class);

        blob.position(blob, 1);
    }

    /**
     * Test if {@link FBCachedBlob#getBinaryStream()} returns <code>null</code> when data is <code>null</code>.
     */
    @Test
    public void testGetBinaryStream_null() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(null);

        assertNull(blob.getBinaryStream());
    }

    /**
     * Test if {@link FBCachedBlob#getBinaryStream()} returns stream with all data.
     */
    @Test
    public void testGetBinaryStream() throws Exception {
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

        assertArrayEquals("Expected data and received data to be identical", data, receivedData);
    }

    /**
     * Tests if {@link FBCachedBlob#getBinaryStream(long, long)} throws SQLFeatureNotSupportedException
     */
    @Test
    public void testGetBinaryStream_long_long() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        expectedException.expect(SQLFeatureNotSupportedException.class);

        blob.getBinaryStream(1, 1);
    }

    /**
     * Test if {@link FBCachedBlob#setBytes(long, byte[])} throws an SQLException (read only).
     */
    @Test
    public void testSetBytes_long_byteArr() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        expectedException.expect(blobReadOnlySQLException());

        blob.setBytes(1, new byte[] { 2 });
    }

    /**
     * Test if {@link FBCachedBlob#setBytes(long, byte[], int, int)} throws an SQLException (read only).
     */
    @Test
    public void testSetBytes_long_byteArr_int_int() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        expectedException.expect(blobReadOnlySQLException());

        blob.setBytes(1, new byte[] { 2 }, 0, 1);
    }

    /**
     * Test if {@link FBCachedBlob#setBinaryStream(long)} throws an SQLException (read only).
     */
    @Test
    public void testSetBinaryStream() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        expectedException.expect(blobReadOnlySQLException());

        blob.setBinaryStream(1);
    }

    /**
     * Test if {@link FBCachedBlob#truncate(long)} throws SQLFeatureNotSupportedException.
     */
    @Test
    public void testTruncate() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        expectedException.expect(SQLFeatureNotSupportedException.class);

        blob.truncate(1);
    }

    /**
     * Test if repeated calls to {@link FBCachedBlob#getSynchronizationObject()} return the same object.
     */
    @Test
    public void testGetSynchronizationObject() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        final Object sync1 = blob.getSynchronizationObject();
        assertSame("Invocations of getSynchronizationObject() should return same object",
                sync1, blob.getSynchronizationObject());
    }

    /**
     * Test if {@link FBCachedBlob#free()} releases the data held internally.
     */
    @Test
    public void testFree() throws Exception {
        FBCachedBlob blob = new FBCachedBlob(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });

        assumeThat("Blob should have a length of 10", blob.length(), equalTo(10L));

        blob.free();

        expectedException.expect(blobFreedSQLException());
        blob.length();
    }

    private Matcher<SQLException> blobReadOnlySQLException() {
        return allOf(
                isA(SQLException.class),
                message(equalTo(FBCachedBlob.BLOB_READ_ONLY))
        );
    }

    private Matcher<SQLException> blobFreedSQLException() {
        return allOf(
                isA(SQLException.class),
                fbMessageStartsWith(JaybirdErrorCodes.jb_blobClosed)
        );
    }
}
