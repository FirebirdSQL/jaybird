/*
 SPDX-FileCopyrightText: Copyright 2001 Boix i Oltra, S.L.
 SPDX-FileContributor: Alejandro Alberola (Boix i Oltra, S.L.)
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2002-2010 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
 SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
 SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * An {@code XdrOutputStream} writes data in XDR format to an underlying {@code java.io.OutputStream}.
 * <p>
 * This class is not thread-safe.
 * </p>
 *
 * @author Alejandro Alberola
 * @author David Jencks
 * @author Mark Rotteveel
 * @version 1.0
 */
public final class XdrOutputStream extends BufferedOutputStream implements EncryptedStreamSupport {

    private static final int BUF_SIZE = Math.max(1024, JaybirdSystemProperties.getWireOutputBufferSize(32767));

    public static final int SPACE_BYTE = 0x20;
    public static final int NULL_BYTE = 0x0;
    private static final int TEXT_PAD_LENGTH = BUF_SIZE;
    private static final byte[] TEXT_PAD = createPadding(TEXT_PAD_LENGTH, SPACE_BYTE);
    private static final int ZERO_PAD_LENGTH = 3;
    private static final byte[] ZERO_PADDING = new byte[ZERO_PAD_LENGTH];

    // TODO In a lot of cases the padding written in this class should be NULL_BYTE instead of SPACE_BYTE

    private boolean compressed;
    private boolean encrypted;

    /**
     * Create a new instance of {@code XdrOutputStream} with default buffer size.
     *
     * @param out
     *         underlying {@code OutputStream} to write to
     */
    public XdrOutputStream(OutputStream out) {
        this(out, BUF_SIZE);
    }

    /**
     * Create a new instance of {@code XdrOutputStream} with the specified buffer size.
     *
     * @param out
     *         underlying {@code OutputStream} to write to
     * @param size
     *         size of the buffer (implementation uses a minimum of 8 bytes)
     */
    public XdrOutputStream(OutputStream out, int size) {
        super(out, Math.max(size, 8));
    }

    /**
     * Writes the {@code 0x00} alignment for the specified length. This padding is calculated as {@code (4 - len) & 3}.
     *
     * @param len
     *         length of the previously written buffer to pad
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     */
    public void writeAlignment(int len) throws IOException {
        write(ZERO_PADDING, 0, alignSize(len));
    }

    /**
     * Writes zero padding of the specified length
     *
     * @param len
     *         length to write
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     * @see #writePadding(int, int)
     */
    public void writeZeroPadding(int len) throws IOException {
        byte[] padding = len <= ZERO_PAD_LENGTH ? ZERO_PADDING : new byte[len];
        write(padding, 0, len);
    }

    /**
     * Writes space ({@code 0x20}) padding of the specified length
     *
     * @param len
     *         length to write
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     * @see #writePadding(int, int)
     */
    public void writeSpacePadding(int len) throws IOException {
        byte[] padding = len <= TEXT_PAD_LENGTH ? TEXT_PAD : createPadding(len, SPACE_BYTE);
        write(padding, 0, len);
    }

    /**
     * Writes padding for the specified length of the specified padding byte.
     * <p>
     * Prefer using the more specific {@link #writeZeroPadding(int)} and {@link #writeZeroPadding(int)}.
     * </p>
     *
     * @param len
     *         length of padding to write
     * @param padByte
     *         padding byte to use
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     * @see #writeSpacePadding(int)
     * @see #writeZeroPadding(int)
     */
    public void writePadding(int len, int padByte) throws IOException {
        if (len == 0) return;
        switch (padByte) {
        case SPACE_BYTE -> writeSpacePadding(len);
        case NULL_BYTE -> writeZeroPadding(len);
        default -> {
            byte[] padding = createPadding(len, (byte) padByte);
            write(padding, 0, len);
        }
        }
    }

    /**
     * Creates a padding array.
     *
     * @param len
     *         length of array
     * @param padByte
     *         byte value for filling the array
     * @return array filled with {@code padByte}
     */
    private static byte[] createPadding(int len, int padByte) {
        byte[] padding = new byte[len];
        Arrays.fill(padding, (byte) padByte);
        return padding;
    }

    /**
     * Write byte buffer {@code buf} to the underlying output stream in XDR format.
     *
     * @param buf
     *         byte buffer to be written, a {@code null} is written as an empty buffer
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     */
    public void writeBuffer(byte @Nullable [] buf) throws IOException {
        int len = buf == null ? 0 : buf.length;
        writeInt(len);
        if (len > 0) {
            write(buf, 0, len);
            writeAlignment(len);
        }
    }

    /**
     * Write byte buffer {@code buf} from offset {@code off} for {@code len} bytes to the underlying output stream
     * in XDR format.
     *
     * @param buf
     *         byte buffer to be written
     * @param off
     *         offset to start
     * @param len
     *         length to write
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     * @throws IndexOutOfBoundsException
     *         If {@code off} is negative, {@code len} is negative, or {@code len} is greater than
     *         {@code buff.length - off}
     */
    public void writeBuffer(byte[] buf, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, buf.length);
        writeInt(len);
        if (len > 0) {
            write(buf, off, len);
            writeAlignment(len);
        }
    }

    /**
     * Writes at most {@code len} bytes from {@code buf}, padding with {@code paddingByte} up to {@code len} if
     * the buffer is shorter or {@code null}, followed by the buffer alignment padding.
     * <p>
     * Contrary to {@link #writeBuffer(byte[])} and {@link #writeBuffer(byte[], int, int)}, it is not prefixed with
     * length; the length is expected to either have been written already, or follow from the protocol or some other
     * specification (e.g. the parameter BLR).
     * </p>
     *
     * @param buf
     *         bytes to be written
     * @param len
     *         length to write, if {@code buf} is shorter or {@code null}, the difference is written as {@code padByte}
     * @param padByte
     *         padding byte to use (generally either space (0x20) or NUL (0x00))
     * @since 7
     */
    public void writePaddedBuffer(byte @Nullable [] buf, int len, int padByte) throws IOException {
        int buflen = buf != null ? Math.min(buf.length, len) : 0;
        if (buflen > 0) {
            write(buf, 0, buflen);
        }
        writePadding(len - buflen, padByte);
        writeAlignment(len);
    }

    /**
     * Write content of the specified string using the specified encoding.
     */
    public void writeString(String s, Encoding encoding) throws IOException {
        byte[] buffer = encoding.encodeToCharset(s);
        writeBuffer(buffer);
    }

    /**
     * Write an {@code Xdrable} to this output stream.
     *
     * @param type
     *         type of the {@code Xdrable} to be written, e.g. {@link ISCConstants#isc_tpb_version3}
     * @param item
     *         object to be written ({@code null} is written as an empty item)
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     */
    public void writeTyped(int type, @Nullable Xdrable item) throws IOException {
        int size = 1 + (item != null ? item.getLength() : 0);
        writeInt(size);
        write(type); //e.g. isc_tpb_version3
        if (size > 1) {
            item.write(this);
        }
        writeAlignment(size);
    }

    public void writeTyped(ParameterBuffer parameterBuffer) throws IOException {
        writeTyped(parameterBuffer.getType(), parameterBuffer.toXdrable());
    }

    /**
     * Write a {@code long} value to the underlying stream in XDR format.
     *
     * @param v
     *         {@code long} value to be written
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     */
    public void writeLong(long v) throws IOException {
        byte[] buf = this.buf;
        if (buf.length - count < 8) {
            flushBuffer();
        }
        buf[count++] = (byte) (v >>> 56);
        buf[count++] = (byte) (v >>> 48);
        buf[count++] = (byte) (v >>> 40);
        buf[count++] = (byte) (v >>> 32);
        buf[count++] = (byte) (v >>> 24);
        buf[count++] = (byte) (v >>> 16);
        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) v;
    }

    /**
     * Write an {@code int} value to the underlying stream in XDR format.
     *
     * @param v
     *         {@code int} value to be written
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     */
    public void writeInt(int v) throws IOException {
        byte[] buf = this.buf;
        if (buf.length - count < 4) {
            flushBuffer();
        }
        buf[count++] = (byte) (v >>> 24);
        buf[count++] = (byte) (v >>> 16);
        buf[count++] = (byte) (v >>> 8);
        buf[count++] = (byte) v;
    }

    /**
     * Write byte buffer {@code b} to the underlying output stream in XDR format.
     *
     * @param b
     *         byte buffer to be written
     * @param off
     *         start offset in the buffer
     * @param len
     *         number of bytes to write
     * @param pad
     *         number of (blank) padding bytes to write
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     * @deprecated if you really need this, use {@link #write(byte[], int, int)}, and {@link #writeSpacePadding(int)} or
     * {@link #writeZeroPadding(int)}, will be removed in Jaybird 8 or later
     */
    @Deprecated(since = "7", forRemoval = true)
    public void write(byte[] b, int off, int len, int pad) throws IOException {
        write(b, off, len);
        // TODO We shouldn't always pad with spaces
        writeSpacePadding(pad);
    }

    /**
     * Writes {@code len} bytes from the specified byte array starting at offset {@code off} to this output stream as
     * defined by {@link java.io.OutputStream#write(byte[], int, int)}.
     * <p>
     * <b>Important</b>: do not confuse this method with {@link #write(byte[], int, int, int)} which originally had
     * the signature of this method.
     * </p>
     *
     * @param b
     *         data
     * @param off
     *         start offset in the data
     * @param len
     *         number of bytes to write
     * @throws IOException
     *         if an error occurs while writing to the underlying output stream
     */
    @Override
    @SuppressWarnings({ "java:S1185", "java:S3551" })
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
    }

    /**
     * Close this stream and the underlying output stream.
     *
     * @throws IOException
     *         if an error occurs while closing the underlying stream
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            // release old, possibly large buffer
            buf = new byte[8];
        }
    }

    /**
     * Wraps the underlying stream for zlib compression.
     *
     * @throws IOException
     *         If the underlying stream is already set up for compression
     */
    @InternalApi
    public void enableCompression() throws IOException {
        if (compressed) {
            throw new IOException("Output stream already compressed");
        }
        out = new FbDeflaterOutputStream(out);
        compressed = true;
    }

    @Override
    public void setCipher(Cipher cipher) throws IOException {
        if (encrypted) {
            throw new IOException("Output stream already encrypted");
        }
        flush();
        OutputStream currentStream = out;
        if (currentStream instanceof EncryptedStreamSupport encryptedStreamSupport) {
            encryptedStreamSupport.setCipher(cipher);
        } else {
            out = new CipherOutputStream(currentStream, cipher);
        }
        encrypted = true;
    }

    @SuppressWarnings("java:S2177")
    private void flushBuffer() throws IOException {
        if (count > 0) {
            out.write(buf, 0, count);
            count = 0;
        }
    }

    private static int alignSize(int len) {
        return (4 - len) & 3;
    }

}
