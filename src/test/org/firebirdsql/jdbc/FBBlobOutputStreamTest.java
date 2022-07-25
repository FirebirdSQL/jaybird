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

import org.firebirdsql.common.DataGenerator;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.sql.Connection;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlobOutputStream}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class FBBlobOutputStreamTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static Connection conn;
    private FBBlobOutputStream stream;

    @BeforeAll
    static void setupAll() throws Exception{
        conn = getConnectionViaDriverManager();
    }

    @BeforeEach
    void setUp() throws Exception {
        conn.setAutoCommit(true);
        FBBlob blob = (FBBlob) conn.createBlob();
        stream = (FBBlobOutputStream) blob.setBinaryStream(1);
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            conn.close();
        } finally {
            conn = null;
        }
    }

    @Test
    void testWrite_byteArr_lengthEqualToBuffer_notWrittenImmediately() throws Exception {
        assumePureJavaTestType();

        stream.write(new byte[]{ 1, 2, 3, 4 });

        assertEquals(0, stream.length(), "Complete array writes (smaller than internal buffer) are buffered");

        stream.flush();

        assertEquals(4, stream.length(), "Expected length of 4 after flush");
    }

    @Test
    void testWrite_byteArr_lengthSmallerThanBuffer_notWrittenImmediately() throws Exception {
        assumePureJavaTestType();

        stream.write(new byte[]{ 1, 2, 3, 4 }, 0, 3);
        assertEquals(0, stream.length(), "Partial array writes (smaller than internal buffer) are buffered");

        stream.flush();

        assertEquals(3, stream.length(), "Expected length of 3 after flush");
    }

    @Test
    void testWrite_byteArr_equalToBufferSize_writtenImmediately() throws Exception {
        byte[] data = DataGenerator.createRandomBytes(((FBBlob) stream.getBlob()).getBufferLength());

        stream.write(data);

        assertEquals(data.length, stream.length(),
                "Byte array writes equal to internal buffer size are written immediately");
    }

    @Test
    void testWrite_byteArr_twoHalfBufferSize_writtenOnSecondWrite() throws Exception {
        byte[] data = DataGenerator.createRandomBytes(((FBBlob) stream.getBlob()).getBufferLength());
        int halfLength = data.length / 2;

        stream.write(data, 0, halfLength);

        assertEquals(0, stream.length(), "Expected no write after first half");

        stream.write(data, halfLength, data.length - halfLength);

        assertEquals(data.length, stream.length(), "Expected full write after second half");
    }

    @Test
    void testWrite_byteArr_halfAndRemainderPlus1OfBufferSize_writtenOnSecondWrite() throws Exception {
        assumePureJavaTestType();

        byte[] data = DataGenerator.createRandomBytes(((FBBlob) stream.getBlob()).getBufferLength());
        int halfLength = data.length / 2;

        stream.write(data, 0, halfLength);

        assertEquals(0, stream.length(), "Expected no write after first half");

        // Writing one more byte than the available internal buffer to trigger "len > buf.length - count" condition
        stream.write(data, 0, data.length - (halfLength - 1));

        assertEquals(data.length + 1, stream.length(), "Expected full write after second write");
    }

    @Test
    void testWrite_byteArr_largerThanBufferSize_writtenImmediately() throws Exception {
        assumePureJavaTestType();

        byte[] data = DataGenerator.createRandomBytes((int) (((FBBlob) stream.getBlob()).getBufferLength() * 1.5));

        stream.write(data);

        assertEquals(data.length, stream.length(), "Byte array writes larger than internal buffer size are written immediately");
    }

    @Test
    void testWrite_byteArr_zeroLength_doesNothing() throws Exception {
        stream.write(new byte[] { 1, 2, 3, 4, 5 }, 0, 0);

        assertEquals(0, stream.length(), "Expected blob with no length");

        stream.flush();

        assertEquals(0, stream.length(), "Expected blob with no length after flush");
    }

    @Test
    void testWrite_byteArr_closed_throwsIOE() throws Exception {
        stream.close();

        assertStreamClosedIOException(() -> stream.write(new byte[] { 1, 2, 3, 4, 5 }));
    }

    @Test
    void testWrite_byteArrNull_throwsNPE() {
        assertThrows(NullPointerException.class, () -> stream.write(null, 0, 1));
    }

    @Test
    void testWrite_byteArr_offsetNegative_throwsIOBE() {
        assertThrows(IndexOutOfBoundsException.class, () -> stream.write(new byte[] { 1, 2, 3, 4, 5}, -1, 2));
    }

    @Test
    void testWrite_byteArr_LengthNegative_throwsIOBE() {
        assertThrows(IndexOutOfBoundsException.class, () -> stream.write(new byte[] { 1, 2, 3, 4, 5}, 0, -1));
    }

    @Test
    void testWrite_byteArr_offsetBeyondEnd_throwsIOBE() {
        assertThrows(IndexOutOfBoundsException.class, () -> stream.write(new byte[] { 1, 2, 3, 4, 5}, 5, 1));
    }

    @Test
    void testWrite_byteArr_lengthBeyondEnd_throwsIOBE() {
        assertThrows(IndexOutOfBoundsException.class, () -> stream.write(new byte[] { 1, 2, 3, 4, 5}, 0, 6));
    }

    @Test
    void testWrite_byteArr_offsetAndLengthBeyondEnd_throwsIOBE() {
        assertThrows(IndexOutOfBoundsException.class, () -> stream.write(new byte[] { 1, 2, 3, 4, 5}, 4, 2));
    }

    @Test
    void testWrite_byte_notWrittenImmediately() throws Exception {
        assumePureJavaTestType();

        stream.write(1);

        assertEquals(0, stream.length(), "Single byte writes aren't flushed immediately");

        stream.flush();

        assertEquals(1, stream.length(), "Expected length of 1 after flush");
    }

    @Test
    void testWrite_byte_fillsBuffer_writtenImmediately() throws Exception {
        byte[] data = DataGenerator.createRandomBytes(((FBBlob) stream.getBlob()).getBufferLength() - 1);
        stream.write(data);

        assertEquals(0, stream.length(), "Write less than internal buffer not written immediately");

        stream.write(1);

        assertEquals(data.length + 1, stream.length(), "Expected flush after filling internal buffer");
    }

    @Test
    void testWrite_byte_closed_throwsIOE() throws Exception {
        stream.close();

        assertStreamClosedIOException(() -> stream.write(1));
    }

    private void assertStreamClosedIOException(Executable executable) {
        IOException exception = assertThrows(IOException.class, executable);
        assertThat(exception, message(equalTo("Output stream is already closed.")));
    }

    private void assumePureJavaTestType() {
        assumeThat("Test only works with pure java implementations", FBTestProperties.GDS_TYPE, isPureJavaType());
    }
}
