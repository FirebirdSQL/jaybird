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
package org.firebirdsql.encodings;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Implementation of {@link Encoding} which uses the default functionality of {@link java.nio.charset.Charset} and
 * {@link java.lang.String}.
 * <p>
 * The main use for this class is for multi-byte character sets, but it also works for single byte character sets,
 * although {@link EncodingSingleByte} is more efficient for shorter strings.
 * </p>
 */
final class EncodingGeneric implements Encoding {

    // TODO Test claim that EncodingSingleByte is more efficient
    // Some testing seems to indicate that EncodingSingleByte is (slightly) faster for up to +/- 1500-2000 chars),
    // while EncodingGeneric is (slightly) faster with longer strings

    private final Charset charset;

    EncodingGeneric(final Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] encodeToCharset(final String in) {
        return in.getBytes(charset);
    }

    @Override
    public String decodeFromCharset(final byte[] in) {
        return new String(in, charset);
    }

    @Override
    public String decodeFromCharset(final byte[] in, final int offset, final int length) {
        return new String(in, offset, length, charset);
    }

    @Override
    public String getCharsetName() {
        return charset.name();
    }

    @Override
    public Reader createReader(InputStream inputStream) {
        return new InputStreamReader(inputStream, charset);
    }

    @Override
    public Writer createWriter(OutputStream outputStream) {
        return new OutputStreamWriter(outputStream, charset);
    }

}
