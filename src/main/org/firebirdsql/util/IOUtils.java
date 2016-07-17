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
package org.firebirdsql.util;

import java.io.*;

/**
 * Utility methods for stream and byte array conversions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
        while (toRead > 0  && (counter = in.read(buff, 0, Math.min(toRead, buff.length))) != -1) {
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
