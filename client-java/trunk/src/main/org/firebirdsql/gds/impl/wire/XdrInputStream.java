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

package org.firebirdsql.gds.impl.wire;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;



/**
 * <code>XdrInputStream</code> is an input stream for reading in data that
 * is in the XDR format. An <code>XdrInputStream</code> instance is wrapped 
 * around an underlying <code>java.io.InputStream</code>.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class XdrInputStream {

    protected final static byte[] pad = new byte[8];
    protected InputStream in = null;
    // Buffer
    protected static int defaultBufferSize = 16384;
    protected byte buf[];
    protected int count;
    protected int pos;

    protected XdrInputStream() {
        // empty, for subclasses only
    }
    
    /**
     * Create a new instance of <code>XdrInputStream</code>.
     *
     * @param in The underlying <code>InputStream</code> to read from
     */
    public XdrInputStream(InputStream in) {
        buf = new byte[defaultBufferSize];
        this.in = in;
    }

    /**
     * Read in a byte buffer.
     *
     * @return The buffer that was read
     * @throws IOException if an error occurs while reading from the 
     *         underlying input stream
     */
    public byte[] readBuffer() throws IOException {
        int len = readInt();
        byte[] buffer = new byte[len];
        readFully(buffer,0,len);
        readFully(pad,0,(4 - len) & 3);
        return buffer;
    }

    /**
     * Read in a raw array of bytes.
     *
     * @param len The number of bytes to read
     * @return The byte buffer that was read
     * @throws IOException if an error occurs while reading from the 
     *         underlying input stream
     */
    public byte [] readRawBuffer(int len) throws IOException {
        byte [] buffer = new byte[len];
        readFully(buffer, 0, len);
        return buffer;
    }
    
    /**
     * Read in a <code>String</code>.
     *
     * @return The <code>String</code> that was read
     * @throws IOException if an error occurs while reading from the 
     *         underlying input stream
     */
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

    /**
     * Read a row of SQL data and store it in the results set of a statement.
     *
     * @param ioLength array containing the lengths of each column in the
     *        data row that is to be read
     * @param stmt The statement where the row is to be stored
     * @throws IOException if an error occurs while reading from the 
     *         underlying input stream
     */
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


    /**
     * Read in a <code>long</code>.
     *
     * @return The <code>long</code> that was read
     * @throws IOException if an error occurs while reading from the 
     *         underlying input stream
     */
    public long readLong() throws IOException {
        return (read() << 56) | (read() << 48) | (read() << 40) | (read() << 32) 
        | (read() << 24) | (read() << 16) | (read() << 8) | (read() << 0);
    }

    /**
     * Read in an <code>int</code>.
     *
     * @return The <code>int</code> that was read
     * @throws IOException if an error occurs while reading from the 
     *         underlying input stream
     */
    public int readInt() throws IOException {
        return (read() << 24) | (read() << 16) | (read() << 8) | (read() << 0);
    }
    
    /**
     * Read in a <code>short</code>.
     *
     * @return The <code>short</code> that was read
     * @throws IOException if an error occurs while reading from the 
     *         underlying input stream
     */
    public int readShort() throws IOException {
        return (read() << 8) | (read() << 0);
    }

    /**
     * Read a given amount of data from the underlying input stream. The data
     * that is read is stored in <code>b</code>, starting from offset
     * <code>off</code>.
     *
     * @param b The byte buffer to hold the data that is read
     * @param off The offset at which to start storing data in <code>b</code>
     * @param len The number of bytes to be read
     * @throws IOException if an error occurs while reading from the 
     *         underlying input stream
     */
    public void readFully(byte b[], int off, int len) throws IOException {

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

    /**
     * Read in the next byte of data from the underlying input stream.
     *
     * @return The value that was read
     * @throws IOException if an error occurs while reading from the 
     *         underlying input stream
     */
    public int read() throws IOException {
        if (pos >= count){
            pos = count = 0;
            int readn = in.read(buf, 0, defaultBufferSize);
            if (readn > 0)
                count = readn;
        }
        return buf[pos++] & 0xff;
    }

    /**
     * Close this input stream and the underlying input stream.
     *
     * @throws IOException if an error occurs while closing the underlying
     *         input stream
     */
    public void close() throws IOException {
        if (in == null)
            return;
        in.close();
        in = null;
        buf = null;
    }
}
