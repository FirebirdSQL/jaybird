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
 * Revision 1.1  2003/01/26 00:49:52  brodsom
 * New character sets support
 *
 * Revision 1.2  2003/01/23 01:37:05  brodsom
 * Encodings patch
 *
 */
package org.firebirdsql.encodings;

public class Encoding_ISO8859_8 extends Encoding_OneByte{

    private static char[] byteToChar = new char[256];
    private static byte[] charToByte = new byte[256*256];;

    static{
        Initialize("ISO8859_8", byteToChar, charToByte);
    }

    public int encodeToCharset(char[] in, int off, int len, byte[] out){
        return super.encodeToCharset(charToByte, in, off, len, out);
    }
    public int decodeFromCharset(byte[] in, int off, int len, char[] out){
        return super.decodeFromCharset(byteToChar, in, off, len, out);
    }
}