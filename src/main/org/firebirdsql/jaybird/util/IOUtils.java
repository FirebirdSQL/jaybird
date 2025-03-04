// SPDX-FileCopyrightText: Copyright 2016-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import java.io.*;

/**
 * Utility methods for stream and byte array conversions.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class IOUtils {

    private static final int TRANSFER_BUFFER_SIZE = 8192;

    private IOUtils() {
        // No instances
    }

    /**
     * Reads {@code length} bytes from {@code in} to a byte array, or until EOF.
     *
     * @param in
     *         input stream
     * @param length
     *         number of bytes to read (or {@code -1} to read until EOF)
     * @return byte array; length is the actual number of bytes read when EOF was reached before {@code length}
     * @throws IOException
     *         for exceptions reading from {@code in}
     */
    public static byte[] toBytes(final InputStream in, final int length) throws IOException {
        if (length == -1) {
            return in.readAllBytes();
        }
        return in.readNBytes(length);
    }

    /**
     * Reads {@code length} characters from {@code in} to a string, or until EOF.
     *
     * @param in
     *         input stream
     * @param length
     *         number of characters to read (or {@code -1} to read until EOF)
     * @return string; length is the actual number of characters read when EOF was reached before {@code length}
     * @throws IOException
     *         for exceptions reading from {@code in}
     */
    public static String toString(final Reader in, final int length) throws IOException {
        var out = new StringWriter();
        if (length == -1) {
            in.transferTo(out);
        } else {
            var buff = new char[Math.min(TRANSFER_BUFFER_SIZE, length)];
            int counter;
            int toRead = length;
            while (toRead > 0 && (counter = in.read(buff, 0, Math.min(toRead, buff.length))) != -1) {
                out.write(buff, 0, counter);
                toRead -= counter;
            }
        }
        return out.toString();
    }

}
