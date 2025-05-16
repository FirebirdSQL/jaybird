// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import org.jspecify.annotations.NullMarked;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.firebirdsql.gds.ISCConstants.isc_info_truncated;
import static org.firebirdsql.gds.VaxEncoding.encodeVaxInteger2WithoutLength;
import static org.firebirdsql.gds.VaxEncoding.encodeVaxIntegerWithoutLength;
import static org.firebirdsql.gds.VaxEncoding.encodeVaxLongWithoutLength;

/**
 * Class to help writing info response byte arrays.
 *
 * @author Mark Rotteveel
 */
@NullMarked
@SuppressWarnings("unused")
public final class InfoResponseWriter {

    private final ByteWriter response = new ByteWriter();

    public InfoResponseWriter addByte(int item, int val) throws IOException {
        writeItem(item);
        encodeVaxInteger2WithoutLength(response, 1);
        response.write(val);
        return this;
    }

    public InfoResponseWriter addShort(int item, int val) throws IOException {
        writeItem(item);
        encodeVaxInteger2WithoutLength(response, 2);
        encodeVaxInteger2WithoutLength(response, val);
        return this;
    }

    public InfoResponseWriter addInt(int item, int val) throws IOException {
        writeItem(item);
        encodeVaxInteger2WithoutLength(response, 4);
        encodeVaxIntegerWithoutLength(response, val);
        return this;
    }

    public InfoResponseWriter addLong(int item, long val) throws IOException {
        writeItem(item);
        encodeVaxInteger2WithoutLength(response, 8);
        encodeVaxLongWithoutLength(response, val);
        return this;
    }

    public InfoResponseWriter addBytes(int item, byte[] bytes) throws IOException {
        if (bytes.length > 0xFFFF) {
            throw new IllegalArgumentException("Maximum supported length is 65535, was: " + bytes.length);
        }
        writeItem(item);
        encodeVaxInteger2WithoutLength(response, bytes.length);
        response.write(bytes);
        return this;
    }

    public InfoResponseWriter addString(int item, String val, Charset charset) throws IOException {
        return addBytes(item, val.getBytes(charset));
    }

    /**
     * Creates an info response array ending in {@link org.firebirdsql.gds.ISCConstants#isc_info_end}.
     * <p>
     * After calling this method, the content of this writer will be all normal items written since creation or the last
     * call to {@link #reset()}. The {@link org.firebirdsql.gds.ISCConstants#isc_info_end} item is not retained.
     * </p>
     *
     * @return array with info items written up to now, ending in {@link org.firebirdsql.gds.ISCConstants#isc_info_end}.
     * @see #toTruncatedArray()
     */
    public byte[] toArray() {
        return toArray(isc_info_end);
    }

    /**
     * Creates a <i>truncated</i> info response array (ending in
     * {@link org.firebirdsql.gds.ISCConstants#isc_info_truncated}).
     * <p>
     * After calling this method, the content of this writer will be all normal items written since creation or the last
     * call to {@link #reset()}. The {@link org.firebirdsql.gds.ISCConstants#isc_info_truncated} item is not retained.
     * </p>
     *
     * @return array with info items written up to now, ending in
     * {@link org.firebirdsql.gds.ISCConstants#isc_info_truncated}.
     * @see #toArray()
     */
    public byte[] toTruncatedArray() {
        return toArray(isc_info_truncated);
    }

    private byte[] toArray(int endMarker) {
        response.write(endMarker);
        try {
            return response.toByteArray();
        } finally {
            response.rewind();
        }
    }

    /**
     * Clears all items written up to now.
     */
    public void reset() {
        response.reset();
    }

    private void writeItem(int item) {
        switch (item) {
        case isc_info_end, isc_info_truncated ->
                throw new IllegalArgumentException(
                        "Item %d has special meaning in an info response and should not be used for normal items"
                                .formatted(item));
        }
        response.write(item);
    }

    private static final class ByteWriter extends ByteArrayOutputStream {

        /**
         * Undo the last written byte (decrements the internal {@code count} by 1).
         */
        synchronized void rewind() {
            if (count > 0) {
                count--;
            }
        }

    }

}
