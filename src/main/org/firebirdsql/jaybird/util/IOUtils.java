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
