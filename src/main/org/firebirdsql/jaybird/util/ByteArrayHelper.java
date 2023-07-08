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
package org.firebirdsql.jaybird.util;

import java.util.Base64;
import java.util.HexFormat;

/**
 * Helper methods for byte arrays.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class ByteArrayHelper {

    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

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

}
