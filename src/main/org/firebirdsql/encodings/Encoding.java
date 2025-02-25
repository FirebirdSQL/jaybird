// SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
// SPDX-FileCopyrightText: Copyright 2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2013-2017 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.encodings;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Encoding translates between a Java string and a byte array for a specific (Firebird) encoding.
 * <p>
 * Encoding implementations need to be thread-safe.
 * </p>
 */
public interface Encoding {

    /**
     * Encodes the supplied String to bytes in this encoding.
     *
     * @param in
     *         String to encode
     * @return Byte array with encoded string
     */
    byte[] encodeToCharset(String in);

    /**
     * Decodes the supplied byte array to a String.
     *
     * @param in
     *         byte array to decode
     * @return String after decoding the byte array
     */
    String decodeFromCharset(byte[] in);

    /**
     * Decodes a part of the supplied byte array to a String.
     *
     * @param in
     *         byte array to decode
     * @param offset
     *         Offset into the byte array
     * @param length
     *         Length in bytes to decode
     * @return String after decoding the byte array
     */
    String decodeFromCharset(byte[] in, int offset, int length);

    /**
     * @return The name of the Java character set.
     */
    String getCharsetName();

    /**
     * Creates a reader wrapping an input stream.
     *
     * @param inputStream
     *         Input stream
     * @return Reader applying this encoding when reading
     */
    Reader createReader(InputStream inputStream);

    /**
     * Creates a writer wrapping an input stream.
     *
     * @param outputStream
     *         Input stream
     * @return Writer applying this encoding when writing
     */
    Writer createWriter(OutputStream outputStream);
}
