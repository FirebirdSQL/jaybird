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

/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 *
 */

package org.firebirdsql.jgds;

import java.io.*;

/**
 * Describe class <code>XdrInputStream</code> here.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class XdrInputStream extends DataInputStream {

    private byte[] pad = new byte[4];

    public XdrInputStream(InputStream in) {
        super(in);
    }
	 
    public final byte[] readOpaque(int len) throws IOException {
        byte[] buffer = new byte[len];
        readFully(buffer);
        readFully(pad,0,((4 - len) & 3));
        return buffer;
    }

    public final byte[] readBuffer() throws IOException {
        int len = readInt();
        byte[] buffer = new byte[len];
        readFully(buffer);
        readFully(pad,0,((4 - len) & 3));
        return buffer;
    }


    public final String readString() throws IOException {
        int len = readInt();
        byte[] buffer = new byte[len];
        readFully(buffer);
        readFully(pad,0,((4 - len) & 3));
        return new String(buffer);				
    }
    public final byte[] readIntBytes() throws IOException {
        byte[] buffer = new byte[4];
		  readFully(buffer);
		  return buffer;
	 }

    public final byte[] readLongBytes() throws IOException {
        byte[] buffer = new byte[8];
		  readFully(buffer);
		  return buffer;
	 }

    public final byte[] readFloatBytes() throws IOException {
        byte[] buffer = new byte[4];
		  readFully(buffer);
		  return buffer;
	 }

    public final byte[] readDoubleBytes() throws IOException {
        byte[] buffer = new byte[8];
		  readFully(buffer);
		  return buffer;
	 }	 
}
