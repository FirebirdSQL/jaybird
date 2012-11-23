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
final class Encoding_OneByte implements Encoding {

    private final char[] byteToChar;
    private final byte[] charToByte;

    Encoding_OneByte(Charset charset) {
        this(charset, EncodingFactory.DEFAULT_MAPPING);
    }

    Encoding_OneByte(Charset charset, final char[] charMapping) {
        byteToChar = new char[256];
        charToByte = new byte[256 * 256];
        
        byte[] val = new byte[1];
        for (int i = 0; i < 256; i++) {
            val[0] = (byte) i;
            char ch = new String(val, 0, 1, charset).charAt(0);
            byteToChar[i] = charMapping[ch];
            charToByte[byteToChar[i]] = (byte) i;
        }
    }

    // encode
    public byte[] encodeToCharset(String str) {
        int length = str.length();
        byte[] result = new byte[length];
        char[] in = str.toCharArray();
        for (int i = 0; i < in.length; i++) {
            result[i] = charToByte[in[i]];
        }
        return result;
    }

    // decode from charset
    public String decodeFromCharset(byte[] in) {
        char[] bufferC = new char[in.length];
        for (int i = 0; i < bufferC.length; i++) {
            bufferC[i] = byteToChar[in[i] & 0xFF];
        }
        return new String(bufferC);
    }
}
