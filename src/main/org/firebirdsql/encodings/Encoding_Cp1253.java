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
 * Revision 1.4  2004/10/08 22:39:11  rrokytskyy
 * added code to solve the issue when database has encoding NONE and there is no chance to control regional settings of the host OS
 * added possibility to translate characters if there are some encoding issues
 *
 * Revision 1.3  2003/06/04 12:38:23  brodsom
 * Remove unused vars and imports
 *
 * Revision 1.2  2003/01/23 01:37:05  brodsom
 * Encodings patch
 *
 */

package org.firebirdsql.encodings;

public class Encoding_Cp1253 extends Encoding_OneByte{

    private static char[] defaultByteToChar = new char[256];
        private static byte[] defaultCharToByte = new byte[256*256];;

        static{
            Initialize("Cp1253", defaultByteToChar, defaultCharToByte);
        }

        private char[] byteToChar;
        private byte[] charToByte;
    
        public Encoding_Cp1253() {
            byteToChar = defaultByteToChar;
            charToByte = defaultCharToByte;
        }
    
        public Encoding_Cp1253(char[] charMapping) {
            byteToChar = new char[256];
            charToByte = new byte[256 * 256];
            Initialize("Cp1253", byteToChar, charToByte, charMapping);
        }

    public int encodeToCharset(char[] in, int off, int len, byte[] out){
        return super.encodeToCharset(charToByte, in, off, len, out);
    }
    public int decodeFromCharset(byte[] in, int off, int len, char[] out){
        return super.decodeFromCharset(byteToChar, in, off, len, out);
    }
}