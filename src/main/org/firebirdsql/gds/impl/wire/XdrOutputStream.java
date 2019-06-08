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

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
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
public final class XdrOutputStream extends BufferedOutputStream {

    private static final int BUF_SIZE = 32767;

    public static final int SPACE_BYTE = 0x20;
    public static final int NULL_BYTE = 0x0;
    private static final int TEXT_PAD_LENGTH = BUF_SIZE;
    private static final byte[] TEXT_PAD = createPadding(BUF_SIZE, SPACE_BYTE);
    private static final int ZERO_PAD_LENGTH = 3;
    private static final byte[] ZERO_PADDING = new byte[ZERO_PAD_LENGTH];

    // TODO In a lot of cases the padding written in this class should be NULL_BYTE instead of SPACE_BYTE

    /**
     * Create a new instance of <code>XdrOutputStream</code> with default buffer size.
     *
     * @param out
     *         The underlying <code>OutputStream</code> to write to
     */
    public XdrOutputStream(OutputStream out) {
        super(out, BUF_SIZE);
    }

    /**
     * Create a new instance of <code>XdrOutputStream</code> with the specified buffer size.
     *
     * @param out
     *         The underlying <code>OutputStream</code> to write to
     * @param bufferSize
     *         The size of the buffer
     */
    public XdrOutputStream(OutputStream out, int bufferSize) {
        super(out, bufferSize);
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
        write(ZERO_PADDING, 0, (4 - length) & 3);
    }

    /**
     * Writes zero padding of the specified length
     *
     * @param length Length to write
     * @throws IOException if an error occurs while writing to the underlying output stream
     * @see #writePadding(int, int)
     */
    public void writeZeroPadding(int length) throws IOException {
        byte[] padding = length <= ZERO_PAD_LENGTH ? ZERO_PADDING : new byte[length];
        write(padding, 0, length);
    }

    /**
     * Writes space ({@code 0x20}) padding of the specified length
     *
     * @param length Length to write
     * @throws IOException if an error occurs while writing to the underlying output stream
     * @see #writePadding(int, int)
     */
    public void writeSpacePadding(int length) throws IOException {
        byte[] padding = length <= TEXT_PAD_LENGTH ? TEXT_PAD : createPadding(length, SPACE_BYTE);
        write(padding, 0, length);
    }

    /**
     * Writes padding for the specified length of the specified padding byte.
     * <p>
     * Prefer using the more specific {@link #writeZeroPadding(int)} and {@link #writeZeroPadding(int)}.
     * </p>
     *
     * @param length
     *         Length of padding to write
     * @param padByte
     *         Padding byte to use
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     * @see #writeSpacePadding(int)
     * @see #writeZeroPadding(int)
     */
    public void writePadding(int length, int padByte) throws IOException {
        if (padByte == SPACE_BYTE) {
            writeSpacePadding(length);
        } else if (padByte == NULL_BYTE) {
            writeZeroPadding(length);
        } else {
            byte[] padding = createPadding(length, (byte) padByte);
            write(padding, 0, length);
        }
    }

    /**
     * Creates a padding array.
     *
     * @param length Length of array
     * @param padByte Byte value for filling the array
     * @return Array filled with {@code padByte}
     */
    private static byte[] createPadding(int length, int padByte) {
        byte[] padding = new byte[length];
        Arrays.fill(padding, (byte) padByte);
        return padding;
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

    private final byte[] writeBuffer = new byte[8];

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
        write(buffer, 0, 8);
    }

    /**
     * Write an <code>int</code> value to the underlying stream in XDR format.
     *
     * @param v The <code>int</code> value to be written
     * @throws IOException if an error occurs while writing to the
     *         underlying output stream
     */
    public void writeInt(int v) throws IOException {
        write((v >>> 24) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>> 8) & 0xFF);
        write(v & 0xFF);
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
        write(b, offset, len);
        // TODO We shouldn't always pad with spaces
        writeSpacePadding(pad);
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
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
    }

    /**
     * Close this stream and the underlying output stream.
     *
     * @throws IOException if an error occurs while closing the
     *         underlying stream
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            // release old, possibly large buffer
            buf = new byte[1];
        }
    }

    public void setCipher(Cipher cipher) throws IOException {
        OutputStream currentOut = out;
        if (currentOut instanceof CipherOutputStream) {
            throw new IOException("Output stream already encrypted");
        }
        flush();
        out = new CipherOutputStream(currentOut, cipher);
    }

    /**
     * Writes directly to the {@code OutputStream} of the underlying socket.
     *
     * @param data
     *         Data to write
     * @throws IOException
     *         For errors writing to the socket.
     */
    public void writeDirect(byte[] data) throws IOException {
        out.write(data);
        out.flush();
    }
}
