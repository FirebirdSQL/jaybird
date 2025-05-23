// SPDX-FileCopyrightText: Copyright 2017-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.jdbc.SQLStateConstants;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Helper methods for byte arrays.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class ByteArrayHelper {

    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();
    private static final byte[] EMPTY = new byte[0];

    private ByteArrayHelper() {
        // no instances
    }

    /**
     * Converts the provided byte array to a hexadecimal string.
     *
     * @param bytes
     *         byte array (not {@code null}
     * @return String with the content of the byte array in hexadecimal.
     */
    public static String toHexString(byte[] bytes) {
        return HEX_FORMAT.formatHex(bytes);
    }

    /**
     * Converts the provided hexadecimal string to a byte array.
     *
     * @param hexString
     *         Hexadecimal string
     * @return byte array
     * @since 4.0
     */
    public static byte[] fromHexString(String hexString) {
        return HEX_FORMAT.parseHex(hexString);
    }

    /**
     * Decodes a base64 encoded string to a byte array.
     *
     * @param base64
     *         Base64 encoded data
     * @return byte array after decoding
     */
    public static byte[] fromBase64String(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Decodes a base64url encoded string to a byte array.
     * <p>
     * Base64url applies the &quot;URL and Filename safe&quot; Base 64 Alphabet.
     * </p>
     *
     * @param base64url
     *         Base64url encoded data
     * @return byte array after decoding
     * @since 5
     */
    public static byte[] fromBase64urlString(String base64url) {
        return Base64.getUrlDecoder().decode(base64url);
    }

    /**
     * Returns the index of the first occurrence of {@code b} in {@code array}.
     *
     * @param array
     *         Array to search
     * @param b
     *         byte to find
     * @return the index of the first occurrence of {@code b}, or {@code -1} if {@code b} is not in the array
     * @since 5
     */
    public static int indexOf(byte[] array, byte b) {
        for (int idx = 0; idx < array.length; idx++) {
            if (array[idx] == b) return idx;
        }
        return -1;
    }

    /**
     * @return an empty array (length == 0)
     */
    public static byte[] emptyByteArray() {
        return EMPTY;
    }

    /**
     * Validates requested offset ({@code off}) and length ({@code len}) against the array ({@code b}).
     *
     * @param b
     *         array
     * @param off
     *         position in array
     * @param len
     *         length from {@code off}
     * @throws SQLException
     *         if {@code off < 0}, {@code len < 0}, or if {@code off + len > b.length}
     * @since 7
     */
    public static void validateBufferLength(byte[] b, int off, int len) throws SQLException {
        try {
            Objects.checkFromIndexSize(off, len, b.length);
        } catch (IndexOutOfBoundsException e) {
            throw new SQLNonTransientException(e.toString(), SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH);
        }
    }

}
