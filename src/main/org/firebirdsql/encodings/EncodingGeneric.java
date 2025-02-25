// SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
// SPDX-FileCopyrightText: Copyright 2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2020 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.encodings;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Implementation of {@link Encoding} which uses the default functionality of {@link java.nio.charset.Charset} and
 * {@link java.lang.String}.
 */
final class EncodingGeneric implements Encoding {

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
