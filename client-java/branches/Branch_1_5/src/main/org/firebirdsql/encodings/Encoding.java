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
 * Revision 1.3  2004/10/08 22:39:10  rrokytskyy
 * added code to solve the issue when database has encoding NONE and there is no chance to control regional settings of the host OS
 * added possibility to translate characters if there are some encoding issues
 *
 * Revision 1.2  2003/01/23 01:41:19  brodsom
 * Encodings patch
 *
 */

package org.firebirdsql.encodings;

public interface Encoding{

    // encode
    public abstract byte[] encodeToCharset(String in);

    // decode
    public abstract String decodeFromCharset(byte[] in);
}
