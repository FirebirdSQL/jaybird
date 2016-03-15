/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
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
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

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
public final class XdrOutputStream extends OutputStream {

    private static final int BUF_SIZE = 32767;

    private static final Logger log = LoggerFactory.getLogger(XdrOutputStream.class);
    private static final byte[] TEXT_PAD;
    private static final byte[] ZERO_PADDING = new byte[3];
    public static final int SPACE_BYTE = 0x20;
    public static final int NULL_BYTE = 0x0;
    static {
        // fill the padding with blanks
        byte[] textPad = new byte[BUF_SIZE];
        Arrays.fill(textPad, (byte) SPACE_BYTE);
        TEXT_PAD = textPad;
    }

    private OutputStream out;

    // TODO In a lot of cases the padding written in this class should be NULL_BYTE instead of SPACE_BYTE

    /**
     * Create a new instance of <code>XdrOutputStream</code> with buffering.
     *
     * @param out
     *         The underlying <code>OutputStream</code> to write to
     */
    public XdrOutputStream(OutputStream out) {
        this(out, true);
    }

    /**
     * Create a new instance of <code>XdrOutputStream</code> with either a buffered or an unbuffered stream.
     *
     * @param out
     *         The underlying <code>OutputStream</code> to write to
     * @param buffered
     *         {@code true} Uses buffering (like {@link #XdrOutputStream(java.io.OutputStream)} or
     *         {@code false} writes directly to provided {@code OutputStream}.
     */
    public XdrOutputStream(OutputStream out, boolean buffered) {
        if (buffered) {
            this.out = new BufferedOutputStream(out, BUF_SIZE);
        } else {
            this.out = out;
        }
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
        if (padByte == SPACE_BYTE && length <= TEXT_PAD.length) {
            padding = TEXT_PAD;
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
            write(buffer, 0, len, (4 - len) & 3);
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
        // TODO This is probably wrong. It looks like op_batch_segments allows for writing multiple segments up to 2^64 (or 2^63 - 1?) in length (including all segment sizes)
        // writeLong(len + 2); // This works as well instead of 2 * writeInt(len + 2); below
        writeInt(len + 2);
        writeInt(len + 2); //bizarre but true! three copies of the length
        write(len & 0xff);
        write((len >> 8) & 0xff);
        // TODO 4 - len + 2 or 4 - (len + 2) ?
        write(buffer, 0, len, ((4 - len + 2) & 3));
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
            write(type); //e.g. isc_tpb_version3
            size = 1;
        } else {
            size = item.getLength() + 1;
            writeInt(size);
            write(type); //e.g. isc_tpb_version3
            item.write(this);
        }
        writeAlignment(size);
    }

    public void writeTyped(ParameterBuffer parameterBuffer) throws IOException {
        writeTyped(parameterBuffer.getType(), parameterBuffer.toXdrable());
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
     * in XDR format.
     *
     * @param b The <code>byte</code> buffer to be written
     * @param offset The start offset in the buffer
     * @param len The number of bytes to write
     * @param pad The number of (blank) padding bytes to write
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void write(byte[] b, int offset, int len, int pad) throws IOException {
        out.write(b, offset, len);
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
    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream as defined by {@link java.io.OutputStream#write(byte[], int, int)}.
     * <p>
     * <b>Important</b>: do not confuse this method with {@link #write(byte[], int, int, int)} which originally had
     * the signature of this method.
     * </p>
     *
     * @param b The data
     * @param off The start offset in the data
     * @param len The number of bytes to write
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
    }

    /**
     * Flush all buffered data to the underlying output stream.
     *
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Close this stream and the underlying output stream.
     *
     * @throws IOException if an error occurs while closing the
     *         underlying stream
     */
    @Override
    public void close() throws IOException {
        out.close();
    }

    public void setArc4Key(byte[] key) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException {
        if (out instanceof CipherOutputStream) {
            throw new IOException("Output stream already encrypted");
        }
        Cipher rc4 = Cipher.getInstance("ARCFOUR");
        SecretKeySpec rc4Key = new SecretKeySpec(key, "ARCFOUR");
        rc4.init(Cipher.ENCRYPT_MODE, rc4Key);
        out = new CipherOutputStream(out, rc4);
    }
}
