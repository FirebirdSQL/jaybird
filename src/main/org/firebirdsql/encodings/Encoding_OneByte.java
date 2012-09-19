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
 * Revision 1.6  2006/06/21 05:39:38  rrokytskyy
 * disabled caching
 *
 * Revision 1.5  2006/06/20 06:34:00  rrokytskyy
 * added encoding caching that can be enabled via jaybird.encoding.cache property
 *
 * Revision 1.4  2004/10/08 22:39:10  rrokytskyy
 * added code to solve the issue when database has encoding NONE and there is no chance to control regional settings of the host OS
 * added possibility to translate characters if there are some encoding issues
 *
 * Revision 1.3  2003/06/05 22:36:07  brodsom
 * Substitute package and inline imports
 *
 * Revision 1.2  2003/01/23 01:37:05  brodsom
 * Encodings patch
 *
 */

package org.firebirdsql.encodings;

import java.io.UnsupportedEncodingException;

public abstract class Encoding_OneByte implements Encoding{
    
    protected static void Initialize(String encoding, char[] byteToChar,
            byte[] charToByte) {
        Initialize(encoding, byteToChar, charToByte, EncodingFactory.DEFAULT_MAPPING);            
    }
    
    protected static void Initialize(String encoding, char[] byteToChar,
            byte[] charToByte, char[] charMapping) {
        byte[] val = new byte[1];
        char[] charArray = null;
        for (int i=0; i< 256; i++){
            val[0] = (byte) i;
            try {
                charArray = new String(val, 0,1, encoding).toCharArray();
                char ch = charArray[0];
                byteToChar[i] = charMapping[ch];
                charToByte[byteToChar[i]] = (byte) i;
            }
            catch (UnsupportedEncodingException uee){
                uee.printStackTrace();
            }
        }
    }

    // encode
    public byte[] encodeToCharset(String str){
        byte[] result = new byte[str.length()];
        encodeToCharset(str.toCharArray(), 0, str.length(), result);
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
        char[] bufferC = new char[in.length];
        int length = decodeFromCharset(in, 0, in.length, bufferC);
        return new String(bufferC, 0, length);
    }

    public abstract int decodeFromCharset(byte[] in, int off, int len, char[] out);

    public int decodeFromCharset(char[] byteToChar, byte[] in, int off, int len, char[] out){
        for (int i = off; i< off+len; i++)
            out[i] = byteToChar[in[i] & 0xFF];
        return len;
    }
}
