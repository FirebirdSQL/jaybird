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

import org.firebirdsql.common.DataGenerator;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.impl.oo.OOGDSFactoryPlugin;
import org.firebirdsql.gds.impl.wire.WireGDSFactoryPlugin;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlobOutputStream}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBBlobOutputStream extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private Connection conn;
    private FBBlobOutputStream stream;

    private void initDefault() throws Exception {
        conn = getConnectionViaDriverManager();
        FBBlob blob = (FBBlob) conn.createBlob();
        stream = (FBBlobOutputStream) blob.setBinaryStream(1);
    }

    @After
    public void tearDown() throws Exception {
        if (conn != null) conn.close();
    }

    @Test
    public void testWrite_byteArr_lengthEqualToBuffer_notWrittenImmediately() throws Exception {
        assumePureJavaTestType();

        initDefault();
        stream.write(new byte[]{ 1, 2, 3, 4 });

        assertEquals("Complete array writes (smaller than internal buffer) are buffered", 0, stream.length());

        stream.flush();

        assertEquals("Expected length of 4 after flush", 4, stream.length());
    }

    @Test
    public void testWrite_byteArr_lengthSmallerThanBuffer_notWrittenImmediately() throws Exception {
        assumePureJavaTestType();

        initDefault();
        stream.write(new byte[]{ 1, 2, 3, 4 }, 0, 3);
        assertEquals("Partial array writes (smaller than internal buffer) are buffered", 0, stream.length());

        stream.flush();

        assertEquals("Expected length of 3 after flush", 3, stream.length());
    }

    @Test
    public void testWrite_byteArr_equalToBufferSize_writtenImmediately() throws Exception {
        initDefault();
        byte[] data = DataGenerator.createRandomBytes(((FBBlob) stream.getBlob()).getBufferLength());

        stream.write(data);

        assertEquals("Byte array writes equal to internal buffer size are written immediately", data.length, stream.length());
    }

    @Test
    public void testWrite_byteArr_twoHalfBufferSize_writtenOnSecondWrite() throws Exception {
        initDefault();
        byte[] data = DataGenerator.createRandomBytes(((FBBlob) stream.getBlob()).getBufferLength());
        int halfLength = data.length / 2;

        stream.write(data, 0, halfLength);

        assertEquals("Expected no write after first half", 0, stream.length());

        stream.write(data, halfLength, data.length - halfLength);

        assertEquals("Expected full write after second half", data.length, stream.length());
    }

    @Test
    public void testWrite_byteArr_halfAndRemainderPlus1OfBufferSize_writtenOnSecondWrite() throws Exception {
        assumePureJavaTestType();

        initDefault();
        byte[] data = DataGenerator.createRandomBytes(((FBBlob) stream.getBlob()).getBufferLength());
        int halfLength = data.length / 2;

        stream.write(data, 0, halfLength);

        assertEquals("Expected no write after first half", 0, stream.length());

        // Writing one more byte than the available internal buffer to trigger "len > buf.length - count" condition
        stream.write(data, 0, data.length - (halfLength - 1));

        assertEquals("Expected full write after second write", data.length + 1, stream.length());
    }

    @Test
    public void testWrite_byteArr_largerThanBufferSize_writtenImmediately() throws Exception {
        assumePureJavaTestType();

        initDefault();
        byte[] data = DataGenerator.createRandomBytes((int) (((FBBlob) stream.getBlob()).getBufferLength() * 1.5));

        stream.write(data);

        assertEquals("Byte array writes larger than internal buffer size are written immediately", data.length, stream.length());
    }

    @Test
    public void testWrite_byteArr_zeroLength_doesNothing() throws Exception {
        initDefault();
        stream.write(new byte[] { 1, 2, 3, 4, 5 }, 0, 0);

        assertEquals("Expected blob with no length", 0, stream.length());

        stream.flush();

        assertEquals("Expected blob with no length after flush", 0, stream.length());
    }

    @Test
    public void testWrite_byteArr_closed_throwsIOE() throws Exception {
        initDefault();
        stream.close();

        expectStreamClosedIOException();

        stream.write(new byte[] { 1, 2, 3, 4, 5 });
    }

    @Test
    public void testWrite_byteArrNull_throwsNPE() throws Exception {
        initDefault();

        expectedException.expect(NullPointerException.class);

        stream.write(null, 0, 1);
    }

    @Test
    public void testWrite_byteArr_offsetNegative_throwsIOBE() throws Exception {
        initDefault();

        expectedException.expect(IndexOutOfBoundsException.class);

        stream.write(new byte[] { 1, 2, 3, 4, 5}, -1, 2);
    }

    @Test
    public void testWrite_byteArr_LengthNegative_throwsIOBE() throws Exception {
        initDefault();

        expectedException.expect(IndexOutOfBoundsException.class);

        stream.write(new byte[] { 1, 2, 3, 4, 5}, 0, -1);
    }

    @Test
    public void testWrite_byteArr_offsetBeyondEnd_throwsIOBE() throws Exception {
        initDefault();

        expectedException.expect(IndexOutOfBoundsException.class);

        stream.write(new byte[] { 1, 2, 3, 4, 5}, 5, 1);
    }

    @Test
    public void testWrite_byteArr_lengthBeyondEnd_throwsIOBE() throws Exception {
        initDefault();

        expectedException.expect(IndexOutOfBoundsException.class);

        stream.write(new byte[] { 1, 2, 3, 4, 5}, 0, 6);
    }

    @Test
    public void testWrite_byteArr_offsetAndLengthBeyondEnd_throwsIOBE() throws Exception {
        initDefault();

        expectedException.expect(IndexOutOfBoundsException.class);

        stream.write(new byte[] { 1, 2, 3, 4, 5}, 4, 2);
    }

    @Test
    public void testWrite_byte_notWrittenImmediately() throws Exception {
        assumePureJavaTestType();

        initDefault();
        stream.write(1);

        assertEquals("Single byte writes aren't flushed immediately", 0, stream.length());

        stream.flush();

        assertEquals("Expected length of 1 after flush", 1, stream.length());
    }

    @Test
    public void testWrite_byte_fillsBuffer_writtenImmediately() throws Exception {
        initDefault();
        byte[] data = DataGenerator.createRandomBytes(((FBBlob) stream.getBlob()).getBufferLength() - 1);
        stream.write(data);

        assertEquals("Write less than internal buffer not written immediately", 0, stream.length());

        stream.write(1);

        assertEquals("Expected flush after filling internal buffer", data.length + 1, stream.length());
    }

    @Test
    public void testWrite_byte_closed_throwsIOE() throws Exception {
        initDefault();
        stream.close();

        expectStreamClosedIOException();

        stream.write(1);
    }

    private void expectStreamClosedIOException() {
        expectedException.expect(allOf(
                isA(IOException.class),
                message(equalTo("Output stream is already closed."))
        ));
    }

    private void assumePureJavaTestType() {
        assumeThat("Test only works with pure java implementations", FBTestProperties.GDS_TYPE, isIn(Arrays.asList(
                WireGDSFactoryPlugin.PURE_JAVA_TYPE_NAME,
                OOGDSFactoryPlugin.TYPE_NAME)));
    }
}
