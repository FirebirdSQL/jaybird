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
package org.firebirdsql.gds;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper methods for decoding Vax style (little endian) integers as used by Firebird from byte arrays.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class VaxEncoding {

    private VaxEncoding() {
        // No instances
    }

    /**
     * Reads Vax style integers from the supplied buffer, starting at {@code startPosition} and reading for
     * {@code length} bytes.
     * <p>
     * This method is useful for lengths up to 4 bytes (ie normal Java integers ({@code int}). For larger lengths it
     * will return {@code 0}. Use {@link #iscVaxLong(byte[], int, int)} for reading values with length up to 8 bytes.
     * For decoding 2 byte integers, use {@link #iscVaxInteger2(byte[], int)}.
     * </p>
     *
     * @param buffer
     *         The byte array from which the integer is to be retrieved
     * @param startPosition
     *         The offset starting position from which to start retrieving byte values
     * @return The integer value retrieved from the bytes
     * @see #iscVaxLong(byte[], int, int)
     * @see #iscVaxInteger2(byte[], int)
     */
    public static int iscVaxInteger(final byte[] buffer, final int startPosition, int length) {
        if (length > 4) {
            return 0;
        }
        int value = 0;
        int shift = 0;

        int index = startPosition;
        while (--length >= 0) {
            value += (buffer[index++] & 0xff) << shift;
            shift += 8;
        }
        return value;
    }

    /**
     * Encodes an integer using vax encoding into the output stream, length prefix is included.
     *
     * @param stream Output stream to write
     * @param value Value to encode
     * @throws IOException
     */
    public static void encodeVaxInteger(OutputStream stream, int value) throws IOException {
        stream.write(4);
        stream.write(value);
        stream.write(value >> 8);
        stream.write(value >> 16);
        stream.write(value >> 24);
    }

    /**
     * Reads Vax style integers from the supplied buffer, starting at {@code startPosition} and reading for
     * {@code length} bytes.
     * <p>
     * This method is useful for lengths up to 8 bytes (ie normal Java longs ({@code long}). For larger lengths it will
     * return {@code 0}.
     * </p>
     *
     * @param buffer
     *         The byte array from which the integer is to be retrieved
     * @param startPosition
     *         The offset starting position from which to start retrieving byte values
     * @return The integer value retrieved from the bytes
     * @see #iscVaxLong(byte[], int, int)
     * @see #iscVaxInteger2(byte[], int)
     */
    public static long iscVaxLong(final byte[] buffer, final int startPosition, int length) {
        if (length > 8) {
            return 0;
        }
        long value = 0;
        int shift = 0;

        int index = startPosition;
        while (--length >= 0) {
            value += (buffer[index++] & 0xffL) << shift;
            shift += 8;
        }
        return value;
    }

    /**
     * Encodes a long using vax encoding into the output stream, length prefix is included.
     *
     * @param stream Output stream to write
     * @param value Value to encode
     * @throws IOException
     */
    public static void encodeVaxLong(OutputStream stream, long value) throws IOException {
        stream.write(8);
        stream.write((int) value);
        stream.write((int) (value >> 8));
        stream.write((int) (value >> 16));
        stream.write((int) (value >> 24));
        stream.write((int) (value >> 32));
        stream.write((int) (value >> 40));
        stream.write((int) (value >> 48));
        stream.write((int) (value >> 56));
    }

    /**
     * Variant of {@link #iscVaxInteger(byte[], int, int)} specifically for two-byte integers.
     *
     * @param buffer
     *         The byte array from which the integer is to be retrieved
     * @param startPosition
     *         The offset starting position from which to start retrieving byte values
     * @return The integer value retrieved from the bytes
     * @see #iscVaxInteger(byte[], int, int)
     * @see #iscVaxLong(byte[], int, int)
     */
    public static int iscVaxInteger2(final byte[] buffer, final int startPosition) {
        return (buffer[startPosition] & 0xff) | ((buffer[startPosition + 1] & 0xff) << 8);
    }

}
