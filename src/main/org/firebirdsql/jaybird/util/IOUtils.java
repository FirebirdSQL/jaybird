// SPDX-FileCopyrightText: Copyright 2016-2023 Mark Rotteveel
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

    private IOUtils() {
        // No instances
    }

    public static byte[] toBytes(final InputStream in, final int length) throws IOException {
        if (length == -1) {
            return toBytes(in);
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buff = new byte[Math.min(4096, length)];
        int counter;
        int toRead = length;
        while (toRead > 0 && (counter = in.read(buff, 0, Math.min(toRead, buff.length))) != -1) {
            out.write(buff, 0, counter);
            toRead -= counter;
        }
        return out.toByteArray();
    }

    public static byte[] toBytes(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buff = new byte[4096];
        int counter;
        while ((counter = in.read(buff, 0, buff.length)) != -1) {
            out.write(buff, 0, counter);
        }
        return out.toByteArray();
    }

    public static String toString(final Reader in, final int length) throws IOException {
        if (length == -1) {
            return toString(in);
        }
        final StringWriter out = new StringWriter();
        final char[] buff = new char[Math.min(4096, length)];
        int counter;
        int toRead = length;
        while (toRead > 0 && (counter = in.read(buff, 0, Math.min(toRead, buff.length))) != -1) {
            out.write(buff, 0, counter);
            toRead -= counter;
        }
        return out.toString();
    }

    public static String toString(final Reader in) throws IOException {
        final StringWriter out = new StringWriter();
        final char[] buff = new char[4096];
        int counter;
        while ((counter = in.read(buff, 0, buff.length)) != -1) {
            out.write(buff, 0, counter);
        }
        return out.toString();
    }
}
