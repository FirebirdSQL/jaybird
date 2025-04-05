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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.common.InfoResponseWriter;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.jaybird.fb.constants.BpbItems;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.firebirdsql.common.DataGenerator.createIndexBytes;
import static org.firebirdsql.common.DataGenerator.createRandomBytes;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.firebirdsql.gds.ISCConstants.isc_bad_segstr_handle;
import static org.firebirdsql.gds.ISCConstants.isc_info_blob_max_segment;
import static org.firebirdsql.gds.ISCConstants.isc_info_blob_num_segments;
import static org.firebirdsql.gds.ISCConstants.isc_info_blob_total_length;
import static org.firebirdsql.gds.ISCConstants.isc_info_blob_type;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.firebirdsql.gds.ISCConstants.isc_no_segstr_close;
import static org.firebirdsql.gds.ISCConstants.isc_segstr_no_write;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link InlineBlob}.
 *
 * @author Mark Rotteveel
 */
@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
class InlineBlobTest {

    @Mock
    private FbWireDatabase database;

    @ParameterizedTest
    @ValueSource(longs = { -1, 1000 })
    void getBlobId(final long blobId) {
        InlineBlob blob = createInlineBlob(blobId, 5, new byte[0], new byte[0]);

        assertEquals(blobId, blob.getBlobId(), "blobId");
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 1000 })
    void getTransactionHandle(final int transactionHandle) {
        InlineBlob blob = createInlineBlob(1, transactionHandle, new byte[0], new byte[0]);

        assertEquals(transactionHandle, blob.getTransactionHandle(), "transactionHandle");
    }

    @Test
    void getHandle_greaterThan65535() {
        InlineBlob blob1 = createInlineBlob(1, 5, new byte[0], new byte[0]);
        InlineBlob blob2 = createInlineBlob(1, 5, new byte[0], new byte[0]);

        assertThat("blob1 handle", blob1.getHandle(), greaterThan(65535));
        assertThat("blob2 handle", blob2.getHandle(), greaterThan(65535));
        assertNotEquals(blob1.getHandle(), blob2.getHandle(), "blob1 handle should not equal blob2 handle");
    }

    @Test
    void handle_afterIntegerOverflow() throws Exception {
        Field localHandleIdGenerator = InlineBlob.class.getDeclaredField("localHandleIdGenerator");
        localHandleIdGenerator.setAccessible(true);
        AtomicInteger generator = (AtomicInteger) localHandleIdGenerator.get(null);
        // Set generator so next value overflows
        generator.set(Integer.MAX_VALUE);

        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);

        assertEquals(65536, blob.getHandle(), "Unexpected handle after integer overflow of handle generator");
    }

    @Test
    void getDatabase() {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);

        assertSame(database, blob.getDatabase(), "database");
    }

    @Test
    void isOpen_beforeOpen_afterOpen_afterClose() throws Exception {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);

        assertFalse(blob.isOpen(), "isOpen before open()");
        blob.open();
        assertTrue(blob.isOpen(), "isOpen after open()");
        blob.close();
        assertFalse(blob.isOpen(), "isOpen after close()");
    }

    @Test
    void doubleOpen_notAllowed() throws Exception {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);
        blob.open();

        SQLException e = assertThrows(SQLException.class, blob::open, "double open not allowed");
        assertThat(e, errorCodeEquals(isc_no_segstr_close));
        assertTrue(blob.isOpen(), "blob should still be open after double open");
    }

    @Test
    void reopenAfterClose_allowed() throws Exception {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);
        blob.open();
        blob.close();

        assertDoesNotThrow(blob::open);
        assertTrue(blob.isOpen(), "blob should be open after reopen");
    }

    @Test
    void isEof_nonEmpty_beforeOpen_afterOpen_afterClose() throws Exception {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[1]);

        assertTrue(blob.isEof(), "eof before open()");
        blob.open();
        assertFalse(blob.isEof(), "eof after open()");
        blob.close();
        assertTrue(blob.isEof(), "eof after close()");
    }

    @Test
    void isEof_emptyBlob_reportsTrueAfterOpen() throws Exception {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);
        blob.open();

        assertTrue(blob.isEof(), "eof");
    }

    @Test
    void cancel_closesBlob() throws Exception {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);
        blob.open();

        blob.cancel();
        assertFalse(blob.isOpen(), "isOpen after cancel()");
    }

    @Test
    void isOutput_false() {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);

        assertFalse(blob.isOutput(), "output");
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -500 })
    void getSegment_invalidSizes(final int segmentSize) throws Exception {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);
        blob.open();

        SQLException e = assertThrows(SQLException.class, () -> blob.getSegment(segmentSize),
                "should not allow invalid sizes");
        assertThat(e, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_blobGetSegmentNegative),
                fbMessageStartsWith(JaybirdErrorCodes.jb_blobGetSegmentNegative, String.valueOf(segmentSize))));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 10 })
    void getSegment_allData(final int increasedSegmentSize) throws Exception {
        final int length = 100;
        final byte[] data = createIndexBytes(length);
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], data);
        blob.open();

        assertFalse(blob.isEof(), "eof before getSegment");
        byte[] segment = blob.getSegment(length + increasedSegmentSize);
        assertNotSame(data, segment, "returned segment should not be the same array as data");
        assertArrayEquals(data, segment, "returned segment should have same content as data");
        assertTrue(blob.isEof(), "eof after getSegment");
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, value = {
            "blobLength, sizeRequested",
            "100,        100",
            "100,        23",
            "111,        1",
            "0,          10"})
    void getSegment(final int blobLength, final int sizeRequested) throws Exception {
        final byte[] data = createIndexBytes(blobLength);
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], data);
        blob.open();

        ByteArrayOutputStream out = new ByteArrayOutputStream(blobLength);
        while (!blob.isEof()) {
            byte[] segment = blob.getSegment(sizeRequested);
            out.write(segment);
        }
        byte[] bytesRead = out.toByteArray();
        assertArrayEquals(data, bytesRead, "bytesRead should have same content as data");
    }

    @Test
    void getSegment_whenClosed() {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);

        SQLException e = assertThrows(SQLException.class, () -> blob.getSegment(5),
                "getSegment not allowed when closed");
        assertThat(e, errorCodeEquals(isc_bad_segstr_handle));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, value = {
            "blobLength, maxGetLength",
            "100,        100",
            "100,        23",
            "111,        1",
            "0,          10"})
    void get(final int blobLength, final int maxGetLength) throws Exception {
        final byte[] data = createIndexBytes(blobLength);
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], data);
        blob.open();

        final byte[] bytesRead = new byte[blobLength];
        int remaining = blobLength;
        int offset = 0;
        while (remaining > 0) {
            int read = blob.get(bytesRead, offset, Math.min(maxGetLength, remaining));
            remaining -= read;
            offset += read;
        }
        assertArrayEquals(data, bytesRead, "bytesRead should have same content as data");
    }

    @Test
    void get_whenClosed() {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);

        SQLException e = assertThrows(SQLException.class, () -> blob.get(new byte[5], 0, 5),
                "get not allowed when closed");
        assertThat(e, errorCodeEquals(isc_bad_segstr_handle));
    }

    @Test
    void putSegment_notSupported() throws SQLException {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);
        blob.open();

        SQLNonTransientException e = assertThrows(SQLNonTransientException.class, () -> blob.putSegment(new byte[10]),
                "putSegment not supported");
        assertThat(e, errorCodeEquals(isc_segstr_no_write));
    }

    @Test
    void put_notSupported() throws SQLException {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);
        blob.open();

        SQLNonTransientException e = assertThrows(SQLNonTransientException.class, () -> blob.put(new byte[10], 0, 10),
                "put not supported");
        assertThat(e, errorCodeEquals(isc_segstr_no_write));
    }

    // TODO Behaviour of seek must still be verified against actual server behaviour; maybe in a separate test

    @Test
    void seek_ABSOLUTE() throws Exception {
        final int blobLength = 100;
        final byte[] data = createIndexBytes(blobLength);
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], data);
        blob.open();

        blob.seek(5, FbBlob.SeekMode.ABSOLUTE);
        byte[] bytesRead = blob.getSegment(5);
        assertArrayEquals(Arrays.copyOfRange(data, 5, 10), bytesRead, "Unexpected bytes read");

        blob.seek(-5, FbBlob.SeekMode.ABSOLUTE);
        assertTrue(blob.isOpen(), "blob should still be open after seeking before start of blob");
        bytesRead = blob.getSegment(5);
        assertArrayEquals(Arrays.copyOfRange(data, 0, 5), bytesRead, "Unexpected bytes read");

        blob.seek(0, FbBlob.SeekMode.ABSOLUTE);
        bytesRead = blob.getSegment(5);
        assertArrayEquals(Arrays.copyOfRange(data, 0, 5), bytesRead, "Unexpected bytes read");

        blob.seek(blobLength + 5, FbBlob.SeekMode.ABSOLUTE);
        assertTrue(blob.isOpen(), "blob should still be open after seeking beyond end of blob");
        assertTrue(blob.isEof(), "Expected end of blob");
        bytesRead = blob.getSegment(5);
        assertArrayEquals(new byte[0], bytesRead, "Unexpected bytes read");
    }

    @Test
    void seek_ABSOLUTE_FROM_END() throws Exception {
        final int blobLength = 100;
        final byte[] data = createIndexBytes(blobLength);
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], data);
        blob.open();

        blob.seek(-5, FbBlob.SeekMode.ABSOLUTE_FROM_END);
        byte[] bytesRead = blob.getSegment(5);
        assertArrayEquals(Arrays.copyOfRange(data, blobLength - 5, blobLength), bytesRead, "Unexpected bytes read");

        blob.seek(5, FbBlob.SeekMode.ABSOLUTE_FROM_END);
        assertTrue(blob.isOpen(), "blob should still be open after seeking beyond end of blob");
        assertTrue(blob.isEof(), "Expected end of blob");
        bytesRead = blob.getSegment(5);
        assertArrayEquals(new byte[0], bytesRead, "Unexpected bytes read");

        blob.seek(-blobLength, FbBlob.SeekMode.ABSOLUTE_FROM_END);
        bytesRead = blob.getSegment(5);
        assertArrayEquals(Arrays.copyOfRange(data, 0, 5), bytesRead, "Unexpected bytes read");

        blob.seek(-blobLength - 5, FbBlob.SeekMode.ABSOLUTE_FROM_END);
        assertTrue(blob.isOpen(), "blob should still be open after seeking before start of blob");
        bytesRead = blob.getSegment(5);
        assertArrayEquals(Arrays.copyOfRange(data, 0, 5), bytesRead, "Unexpected bytes read");
    }

    @Test
    void seek_RELATIVE() throws Exception {
        final int blobLength = 100;
        final byte[] data = createIndexBytes(blobLength);
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], data);
        blob.open();

        blob.seek(-1, FbBlob.SeekMode.RELATIVE);
        assertTrue(blob.isOpen(), "blob should still be open after seeking before start of blob");
        byte[] bytesRead = blob.getSegment(5);
        assertArrayEquals(Arrays.copyOfRange(data, 0, 5), bytesRead, "Unexpected bytes read");

        blob.seek(15, FbBlob.SeekMode.RELATIVE);
        bytesRead = blob.getSegment(5);
        assertArrayEquals(Arrays.copyOfRange(data, 20, 25), bytesRead, "Unexpected bytes read");

        // Positioning beyond end, positions at end
        blob.seek(blobLength, FbBlob.SeekMode.RELATIVE);
        assertTrue(blob.isEof(), "Expected end of blob");
        blob.seek(-10, FbBlob.SeekMode.RELATIVE);
        bytesRead = blob.getSegment(5);
        assertArrayEquals(Arrays.copyOfRange(data, blobLength - 10, blobLength -5), bytesRead, "Unexpected bytes read");
    }

    @Test
    void seek_whenClosed() {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[0]);

        SQLException e = assertThrows(SQLException.class, () -> blob.seek(5, FbBlob.SeekMode.ABSOLUTE),
                "seek not allowed when closed");
        assertThat(e, errorCodeEquals(isc_bad_segstr_handle));
    }

    @Test
    void getBlobInfo_emptyBlobInfo() throws Exception {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[1]);
        blob.open();

        assertArrayEquals(new byte[] { isc_info_end },
                blob.getBlobInfo(new byte[] { isc_info_blob_total_length, isc_info_end }, 10),
                "expected array with isc_info_end if input array was empty");
    }

    @Test
    void getBlobInfo_returnsInput_ignoringRequestedItems() throws Exception {
        final byte[] blobInfo = createBlobInfo(2, 3, 4, BpbItems.TypeValues.isc_bpb_type_stream);
        InlineBlob blob = createInlineBlob(1, 5, blobInfo, new byte[0]);
        blob.open();

        byte[] result = blob.getBlobInfo(getKnownItems(), 100);
        assertNotSame(blobInfo, result, "returned blob info should not be the same array as input");
        assertArrayEquals(blobInfo, result, "returned blob info should have same content as input");
    }

    @Test
    void getBlobInfo_whenClosed() {
        InlineBlob blob = createInlineBlob(1, 5, new byte[0], new byte[1]);

        SQLException e = assertThrows(SQLException.class,
                () -> blob.getBlobInfo(new byte[] { isc_info_blob_total_length, isc_info_end }, 10),
                "getBlobInfo not allowed when closed");
        assertThat(e, errorCodeEquals(isc_bad_segstr_handle));
    }

    @Test
    void copy_returnsInitiallyClosedBlobWithSameData() throws Exception {
        final byte[] info = createBlobInfo(3, 10, 15, BpbItems.TypeValues.isc_bpb_type_segmented);
        final byte[] data = createRandomBytes(15);
        InlineBlob blob = createInlineBlob(1, 5, info, data);
        blob.open();
        blob.getSegment(5);

        InlineBlob copy = blob.copy();
        assertNotSame(blob, copy, "blob and copy should not be the same object");
        assertFalse(copy.isOpen(), "copy should be closed");
        copy.open();
        assertArrayEquals(info, copy.getBlobInfo(getKnownItems(), 100), "info of copy");
        assertArrayEquals(data, copy.getSegment(15), "data of copy");
    }

    private InlineBlob createInlineBlob(long blobId, int transactionHandle, byte[] blobInfo, byte[] data) {
        return new InlineBlob(database, blobId, transactionHandle, blobInfo, data);
    }

    /**
     * @return known blob info items in the same order as used by {@link #createBlobInfo(int, int, int, int)}.
     */
    private byte[] getKnownItems() {
        return new byte[] {
                isc_info_blob_num_segments, isc_info_blob_max_segment, isc_info_blob_total_length, isc_info_blob_type,
                isc_info_end };
    }

    private byte[] createBlobInfo(int numSegments, int maxSegment, int totalLength, int type) throws IOException {
        return new InfoResponseWriter()
                .addInt(isc_info_blob_num_segments, numSegments)
                .addInt(isc_info_blob_max_segment, maxSegment)
                .addInt(isc_info_blob_total_length, totalLength)
                .addByte(isc_info_blob_type, type)
                .toArray();
    }

}