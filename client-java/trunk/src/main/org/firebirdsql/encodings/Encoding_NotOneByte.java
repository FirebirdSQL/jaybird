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
 * Revision 1.2  2003/01/23 01:40:50  brodsom
 * Encodings patch
 *
 */
package org.firebirdsql.encodings;

import java.io.UnsupportedEncodingException;

public class Encoding_NotOneByte implements Encoding{

    String encoding = null;

    public Encoding_NotOneByte(String encoding){
        this.encoding = encoding;
    }
    // encode
    public byte[] encodeToCharset(String in){
        byte[] result = null;
        try {
            result = in.getBytes(encoding);
        }
        catch (UnsupportedEncodingException uee){
        }            
        return result;
    }
    public int encodeToCharset(char[] in, int off, int len, byte[] out){
        byte[] by = null;
        try {
            by = new String(in).getBytes(encoding);
            System.arraycopy(by, 0, out, 0, by.length);
        }
        catch (UnsupportedEncodingException uee){
        }
        return by.length;
    }
    // decode
    public String decodeFromCharset(byte[] in){
        String result = null;
        try {
            result = new String(in,encoding);
        }
        catch (UnsupportedEncodingException uee){
        }
        return result;
    }
    public int decodeFromCharset(byte[] in, int off, int len, char[] out){
        String str = null; 
        try {
            str = new String(in, encoding);
            str.getChars(0, str.length(), out, 0);
        }
        catch (UnsupportedEncodingException uee){
        }
        return str.length();
    }
}
