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

/* added by Blas Rodriguez Somoza:
 *
 * CVS modification log:
 * $Log$
 */

package org.firebirdsql.encodings;

public abstract class Encoding_OneByte implements Encoding{

    protected static void Initialize(String encoding, char[] byteToChar
    , byte[] charToByte){
        byte[] val = new byte[1];
        char[] charArray = null;
        for (int i=0; i< 256; i++){
            val[0] = (byte) i;
            try {
                charArray = new String(val, 0,1, encoding).toCharArray();
                byteToChar[i] = charArray[0];
                charToByte[byteToChar[i]] = (byte) i;
            }
            catch (java.io.UnsupportedEncodingException uee){
                uee.printStackTrace();
            }
        }
    }

    byte[] bufferB = new byte[128];
    char[] bufferC = new char[128];

    // encode
    public byte[] encodeToCharset(String str){
        if (bufferB.length < str.length()) 
            bufferB = new byte[str.length()];
        int length = encodeToCharset(str.toCharArray(), 0, str.length(), bufferB);
        byte[] result = new byte[length];
        System.arraycopy(bufferB, 0, result, 0, length);
        return result;
    }

    public abstract int encodeToCharset(char[] in, int off, int len, byte[] out);

    public int encodeToCharset(byte[] charToByte, char[] in, int off, int len, byte[] out){
        for (int i = off; i< off+len; i++)
            out[i] = charToByte[in[i]];
        return len;
    }

    // decode from charset
    public String decodeFromCharset(byte[] in){
        if (bufferC.length < in.length)
            bufferC = new char[in.length];
        int length = decodeFromCharset(in, 0, in.length, bufferC);
        return new String(bufferC, 0, length);
    }

    public abstract int decodeFromCharset(byte[] in, int off, int len, char[] out);

    public int decodeFromCharset(char[] byteToChar, byte[] in, int off, int len, char[] out){
        for (int i = off; i< off+len; i++)
            out[i] = byteToChar[(int) in[i] & 0xFF];
        return len;
    }
}
