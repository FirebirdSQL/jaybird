/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.encodings;

import java.nio.charset.Charset;

/**
 * Implementation of {@link Encoding} for multibyte charactersets.
 * <p>
 * It also works for single byte character sets, but {@link Encoding_OneByte} is more efficient.
 * </p>
 */
final class Encoding_NotOneByte implements Encoding {

    private final Charset charset;
    private final char[] charMapping;

    public Encoding_NotOneByte(Charset charset) {
        this(charset, null);
    }

    public Encoding_NotOneByte(Charset charset, char[] charMapping) {
        this.charset = charset;
        this.charMapping = charMapping;
    }

    // encode
    public byte[] encodeToCharset(String in) {
        if (charMapping != null) {
            in = new String(translate(in.toCharArray()));
        }
        return in.getBytes(charset);
    }

    // decode
    public String decodeFromCharset(byte[] in) {
        String result = new String(in, charset);
        if (charMapping != null) {
            return new String(translate(result.toCharArray()));
        }
        return result;
    }
    
    private char[] translate(char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            chars[i] = charMapping[chars[i]];
        }
        return chars;
    }
}
