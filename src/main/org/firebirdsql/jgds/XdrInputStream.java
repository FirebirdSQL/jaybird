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
public final class XdrInputStream {

    private final static byte[] pad = new byte[8];
    private InputStream in = null;
    // Buffer
    private static int defaultBufferSize = 16384;
    private byte buf[];
    private int count;
    private int pos;

    public XdrInputStream(InputStream in) {
        buf = new byte[defaultBufferSize];
        this.in = in;
    }

    public byte[] readBuffer() throws IOException {
        int len = readInt();
        byte[] buffer = new byte[len];
        readFully(buffer,0,len);
        readFully(pad,0,(4 - len) & 3);
        return buffer;
    }

    public String readString() throws IOException {
        int len = readInt();
        byte[] buffer = new byte[len];
        readFully(buffer,0,len);
        readFully(pad,0,(4 - len) & 3);
        return new String(buffer);
    }

    //
    // Read SQL data
    //
    //Now returns results in Object[] and in xsqlda.data
    //Nulls are represented by null values in Object array
    public void readSQLData(int[] ioLength, isc_stmt_handle_impl stmt) throws IOException {
        // This only works if not (port->port_flags & PORT_symmetric)		 
        int numCols = ioLength.length;
        byte[][] row = new byte[numCols][];
        byte[] buffer;
        for (int i = 0; i < numCols; i++) {
            int len = ioLength[i];
            if (len == 0){
                len = readInt();
                buffer = new byte[len];
                readFully(buffer,0,len);
                readFully(pad,0,(4 - len) & 3);
            }
            else if (len < 0){
                buffer = new byte[-len];
                readFully(buffer,0,-len);
            }
            else {
                // len is incremented to avoid value 0 so it must be decremented					
                len --;
                buffer = new byte[len];
                readFully(buffer,0,len);
                readFully(pad,0,(4 - len) & 3);
            }
            if (readInt()==-1)
                buffer = null;
            row[i] = buffer;
        }
        if (stmt != null) 
            stmt.addRow(row);
    }

    //
    // Substitute DataInputStream
    //
    public final long readLong() throws IOException {
        return (read() << 56) | (read() << 48) | (read() << 40) | (read() << 32) 
        | (read() << 24) | (read() << 16) | (read() << 8) | (read() << 0);
    }

    public int readInt() throws IOException {
        return (read() << 24) | (read() << 16) | (read() << 8) | (read() << 0);
    }

    public final void readFully(byte b[], int off, int len) throws IOException {

        if (len <= count-pos){
            System.arraycopy(buf, pos, b, off, len);
            pos += len;
        }
        else {
            int n = 0;
            while (n < len) {
                if (count <= pos){
                   pos = count = 0;
                   int readn = in.read(buf, 0, defaultBufferSize);
                   if (readn > 0)
                       count = readn;
                   else 
                       throw new EOFException();
                }
                int lenN = len-n;
                int avail = count - pos;
                int cnt = (avail < lenN) ? avail : lenN;
                System.arraycopy(buf, pos, b, off+n, cnt);
                pos += cnt;
                n += cnt;
            }
        }
    }

    //
    // Buffering classes (those interface with the real InputStream
    //

    public int read() throws IOException {
        if (pos >= count){
            pos = count = 0;
            int readn = in.read(buf, 0, defaultBufferSize);
            if (readn > 0)
                count = readn;
        }
        return buf[pos++] & 0xff;
    }

    public void close() throws IOException {
        if (in == null)
            return;
        in.close();
        in = null;
        buf = null;
    }
}