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
 */
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * An <code>XdrOutputStream</code> writes data in XDR format to an
 * underlying <code>java.io.OutputStream</code>.
 * <p>
 * This class is not thread-safe.
 * </p>
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class XdrOutputStream {

    private static final int BUF_SIZE = 32767;

    private static final Logger log = LoggerFactory.getLogger(XdrOutputStream.class, false);
    private static final byte[] textPad = new byte[BUF_SIZE];
    private static final byte[] ZERO_PADDING = new byte[3];
    public static final int SPACE_BYTE = 0x20;
    public static final int NULL_BYTE = 0x0;

    private final OutputStream out;

    // TODO In a lot of cases the padding written in this class should be NULL_BYTE instead of SPACE_BYTE

    /**
     * Create a new instance of <code>XdrOutputStream</code>.
     *
     * @param out The underlying <code>OutputStream</code> to write to
     */
    public XdrOutputStream(OutputStream out) {
        this.out = new BufferedOutputStream(out, BUF_SIZE);
        // fill the padding with blanks
        Arrays.fill(textPad, (byte) SPACE_BYTE);
    }

    /**
     * Writes the <code>0x00</code> alignment for the specified length. This padding is calculated as
     * <code>(4 - length) & 3</code>.
     *
     * @param length
     *         The length of the previously written buffer to pad
     * @throws IOException
     *         if an error occurs while writing to the
     *         underlying output stream
     */
    public void writeAlignment(int length) throws IOException {
        out.write(ZERO_PADDING, 0, (4 - length) & 3);
    }

    /**
     * Writes padding for the specified length of the specified padding byte.
     *
     * @param length
     *         Length of padding to write
     * @param padByte
     *         Padding byte to use
     * @throws IOException
     *         if an error occurs while writing to the
     *         underlying output stream
     */
    public void writePadding(int length, int padByte) throws IOException {
        final byte[] padding;
        if (padByte == SPACE_BYTE && length <= textPad.length) {
            padding = textPad;
        } else if (padByte == NULL_BYTE && length <= ZERO_PADDING.length) {
            padding = ZERO_PADDING;
        } else {
            padding = new byte[length];
            if (padByte != NULL_BYTE) {
                Arrays.fill(padding, (byte) padByte);
            }
        }
        out.write(padding, 0, length);
    }

    /**
     * Write a <code>byte</code> buffer to the underlying output stream in
     * XDR format.
     *
     * @param buffer The <code>byte</code> buffer to be written
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void writeBuffer(byte[] buffer) throws IOException {
        if (buffer == null)
            writeInt(0);
        else {
            int len = buffer.length;
            writeInt(len);
            write(buffer, len, (4 - len) & 3);
        }
    }

    /**
     * Write a blob buffer to the underlying output stream in XDR format.
     *
     * @param buffer A <code>byte</code> array containing the blob
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void writeBlobBuffer(byte[] buffer) throws IOException {
        int len = buffer.length; // 2 for short for buffer length
        if (log != null && log.isDebugEnabled()) log.debug("writeBlobBuffer len: " + len);
        if (len > Short.MAX_VALUE) {
            throw new IOException(""); //Need a value???
        }
        writeInt(len + 2);
        writeInt(len + 2); //bizarre but true! three copies of the length
        write(len & 0xff);
        write((len >> 8) & 0xff);
        // TODO 4 - len + 2 or 4 - (len + 2) ?
        write(buffer, len, ((4 - len + 2) & 3));
    }

    /**
     * Write content of the specified string using the specified encoding.
     */
    public void writeString(String s, Encoding encoding) throws IOException {
        if (encoding != null) {
            byte[] buffer = encoding.encodeToCharset(s);
            writeBuffer(buffer);
        } else {
            // TODO Remove this option (always require encoding)
            writeBuffer(s.getBytes());
        }
    }

    /**
     * Write an <code>Xdrable</code> to this output stream.
     *
     * @param type Type of the <code>Xdrable</code> to be written,
     *        e.g. {@link ISCConstants#isc_tpb_version3}
     * @param item The object to be written
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void writeTyped(int type, Xdrable item) throws IOException {
        int size;
        if (item == null) {
            writeInt(1);
            write(type); //e.g. gds.isc_tpb_version3
            size = 1;
        } else {
            size = item.getLength() + 1;
            writeInt(size);
            write(type); //e.g. gds.isc_tpb_version3
            item.write(this);
        }
        writeAlignment(size);
    }

    private final byte writeBuffer[] = new byte[8];

    /**
     * Write a <code>long</code> value to the underlying stream in XDR format.
     *
     * @param v The <code>long</code> value to be written
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void writeLong(long v) throws IOException {
        final byte[] buffer = writeBuffer;
        buffer[0] = (byte) (v >>> 56);
        buffer[1] = (byte) (v >>> 48);
        buffer[2] = (byte) (v >>> 40);
        buffer[3] = (byte) (v >>> 32);
        buffer[4] = (byte) (v >>> 24);
        buffer[5] = (byte) (v >>> 16);
        buffer[6] = (byte) (v >>> 8);
        buffer[7] = (byte) v;
        out.write(buffer, 0, 8);
    }

    /**
     * Write an <code>int</code> value to the underlying stream in XDR format.
     *
     * @param v The <code>int</code> value to be written
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void writeInt(int v) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write(v & 0xFF);
    }

    /**
     * Write a <code>byte</code> buffer to the underlying output stream
     * in XDR format
     *
     * @param b The <code>byte</code> buffer to be written
     * @param len The number of bytes to write
     * @param pad The number of (blank) padding bytes to write
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void write(byte[] b, int len, int pad) throws IOException {
        out.write(b, 0, len);
        // TODO We shouldn't always pad with spaces
        writePadding(pad, SPACE_BYTE);
    }

    /**
     * Write a single <code>byte</code> to the underlying output stream in
     * XDR format.
     *
     * @param b The value to be written, will be truncated to a byte
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * Write an array of <code>byte</code>s to the underlying output stream
     * in XDR format.
     *
     * @param b The <code>byte</code> array to be written
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void write(byte[] b) throws IOException {
        out.write(b, 0, b.length);
    }

    /**
     * Flush all buffered data to the underlying output stream.
     *
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Close this stream and the underlying output stream.
     *
     * @throws IOException if an error occurs while closing the
     *         underlying stream
     */
    public void close() throws IOException {
        out.close();
    }
}
