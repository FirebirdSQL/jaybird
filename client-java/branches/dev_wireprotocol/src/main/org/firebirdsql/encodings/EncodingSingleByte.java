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
 * Implementation of {@link Encoding} for single byte character sets.
 */
public final class EncodingSingleByte implements Encoding {

    private final char[] byteToChar;
    private final byte[] charToByte;
    private final Charset charset;

    public EncodingSingleByte(final Charset charset) {
        this(charset, CharacterTranslator.IDENTITY_TRANSLATOR);
    }

    EncodingSingleByte(final Charset charset, final CharacterTranslator translator) {
        byteToChar = new char[256];
        charToByte = new byte[256 * 256];
        this.charset = charset;

        byte[] val = new byte[1];
        for (int i = 0; i < 256; i++) {
            val[0] = (byte) i;
            char ch = new String(val, 0, 1, charset).charAt(0);
            byteToChar[i] = translator.getMapping(ch);
            charToByte[byteToChar[i]] = (byte) i;
        }
    }

    @Override
    public byte[] encodeToCharset(final String str) {
        final int length = str.length();
        final byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = charToByte[str.charAt(i)];
        }
        return result;
    }

    @Override
    public String decodeFromCharset(final byte[] in) {
        final int length = in.length;
        final char[] bufferC = new char[length];
        for (int i = 0; i < length; i++) {
            bufferC[i] = byteToChar[in[i] & 0xFF];
        }
        return new String(bufferC);
    }

    @Override
    public Encoding withTranslation(final CharacterTranslator translator) {
        return new EncodingSingleByte(charset, translator);
    }
}
