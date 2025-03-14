/*
 SPDX-FileCopyrightText: Copyright 2001 Boix i Oltra, S.L.
 SPDX-FileContributor: Alejandro Alberola (Boix i Oltra, S.L.)
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2003-20011 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
 SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.firebirdsql.util.InternalApi;

import javax.crypto.Cipher;
import java.io.*;

/**
 * An input stream for reading in data that is in the XDR format. An {@code XdrInputStream} instance is wrapped
 * around an underlying {@code java.io.InputStream}.
 * <p>
 * This class is not thread-safe.
 * </p>
 *
 * @author Alejandro Alberola
 * @author David Jencks
 * @author Mark Rotteveel
 */
@SuppressWarnings("java:S4929")
public final class XdrInputStream extends FilterInputStream implements EncryptedStreamSupport {

    private static final int MIN_BUF_SIZE = 1024;
    private static final int DEFAULT_BUF_SIZE = 16384;
    private static final int BUF_SIZE =
            Math.max(MIN_BUF_SIZE, JaybirdSystemProperties.getWireInputBufferSize(DEFAULT_BUF_SIZE));
    private boolean compressed;
    private boolean encrypted;

    /**
     * Create a new instance of {@code XdrInputStream}.
     *
     * @param in
     *         underlying {@code InputStream} to read from
     */
    public XdrInputStream(InputStream in) {
        this(in, BUF_SIZE);
    }

    /**
     * Create a new instance of {@code XdrInputStream}.
     *
     * @param in
     *         underlying {@code InputStream} to read from
     * @param size
     *         buffer size
     */
    public XdrInputStream(InputStream in, int size) {
        super(new BufferedInputStream(in, size));
    }

    /**
     * Skips the padding after a buffer of the specified length. The number of bytes to skip is calculated as
     * {@code (4 - len) & 3}.
     *
     * @param len
     *         length of the previously read buffer
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     * @see XdrOutputStream#writePadding(int, int)
     */
    public void skipPadding(int len) throws IOException {
        skipNBytes((4 - len) & 3);
    }

    /**
     * Reads an {@code int} length from the stream, reads and returns the buffer of that length and skips the padding.
     *
     * @return buffer that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public byte[] readBuffer() throws IOException {
        return readBuffer(readInt());
    }

    /**
     * Reads and returns the buffer of {@code len} and skips the padding.
     *
     * @param len
     *         length of the buffer
     * @return buffer that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public byte[] readBuffer(int len) throws IOException {
        if (len == 0) return ByteArrayHelper.emptyByteArray();
        byte[] buffer = readRawBuffer(len);
        skipPadding(len);
        return buffer;
    }

    /**
     * Skips a buffer, by reading an {@code int} length from the stream, and then skipping that length plus the padding.
     *
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public void skipBuffer() throws IOException {
        // buffer length
        int len = readInt();
        if (len > 0) {
            // skip buffer + padding
            skipNBytes(len + ((4 - len) & 3));
        }
    }

    /**
     * Read in a raw array of bytes.
     *
     * @param len
     *         number of bytes to read
     * @return byte buffer that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public byte[] readRawBuffer(int len) throws IOException {
        byte[] buffer = new byte[len];
        readFully(buffer, 0, len);
        return buffer;
    }

    /**
     * Read in a {@code String}.
     *
     * @param encoding
     *         encoding to use for reading the string
     * @return {@code String} that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public String readString(Encoding encoding) throws IOException {
        return encoding.decodeFromCharset(
                readBuffer());
    }

    private final byte[] readBuffer = new byte[8];

    /**
     * Read in a {@code long}.
     *
     * @return {@code long} that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public long readLong() throws IOException {
        readFully(readBuffer, 0, 8);
        return ((long) readBuffer[0]) << 56 |
               (readBuffer[1] & 0xFFL) << 48 |
               (readBuffer[2] & 0xFFL) << 40 |
               (readBuffer[3] & 0xFFL) << 32 |
               (readBuffer[4] & 0xFFL) << 24 |
               (readBuffer[5] & 0xFFL) << 16 |
               (readBuffer[6] & 0xFFL) << 8 |
               (readBuffer[7] & 0xFFL);
    }

    /**
     * Read in an {@code int}.
     *
     * @return {@code int} that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public int readInt() throws IOException {
        readFully(readBuffer, 0, 4);
        return readBuffer[0] << 24 |
               (readBuffer[1] & 0xFF) << 16 |
               (readBuffer[2] & 0xFF) << 8 |
               (readBuffer[3] & 0xFF);
    }

    /**
     * Read a given amount of data from the underlying input stream. The data
     * that is read is stored in {@code b}, starting from offset
     * {@code off}.
     *
     * @param b
     *         byte buffer to hold the data that is read
     * @param off
     *         offset at which to start storing data in {@code b}
     * @param len
     *         number of bytes to be read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (readNBytes(b, off, len) < len) {
            throw new EOFException();
        }
    }

    /**
     * Wraps the underlying stream for zlib decompression.
     *
     * @throws IOException
     *         if the underlying stream is already set up for decompression
     */
    @InternalApi
    public void enableDecompression() throws IOException {
        if (compressed) {
            throw new IOException("Input stream already compressed");
        }
        in = new FbInflaterInputStream(in);
        compressed = true;
    }

    @Override
    public void setCipher(Cipher cipher) throws IOException {
        if (encrypted) {
            throw new IOException("Input stream already encrypted");
        }
        InputStream currentStream = in;
        if (currentStream instanceof EncryptedStreamSupport encryptedStreamSupport) {
            encryptedStreamSupport.setCipher(cipher);
        } else {
            in = new FbCipherInputStream(currentStream, cipher);
        }
        encrypted = true;
    }
}
