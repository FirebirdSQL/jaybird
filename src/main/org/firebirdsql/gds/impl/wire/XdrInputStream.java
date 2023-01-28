/*
 * Firebird Open Source JDBC Driver
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
 *
 */
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.util.InternalApi;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
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
public final class XdrInputStream extends FilterInputStream implements EncryptedStreamSupport {

    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private boolean compressed;
    private boolean encrypted;

    /**
     * Create a new instance of {@code XdrInputStream}.
     *
     * @param in
     *         The underlying {@code InputStream} to read from
     */
    public XdrInputStream(InputStream in) {
        super(new BufferedInputStream(in, DEFAULT_BUFFER_SIZE));
    }

    /**
     * Skips the padding after a buffer of the specified length. The number of bytes to skip is calculated as
     * {@code (4 - length) & 3}.
     *
     * @param length
     *         Length of the previously read buffer
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     * @see XdrOutputStream#writePadding(int, int)
     */
    public void skipPadding(int length) throws IOException {
        skipNBytes((4 - length) & 3);
    }

    /**
     * Reads an {@code int} length from the stream, reads and returns the buffer of that length and skips the padding.
     *
     * @return The buffer that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public byte[] readBuffer() throws IOException {
        return readBuffer(readInt());
    }

    /**
     * Reads and returns the buffer of {@code len} and skips the padding.
     *
     * @return The buffer that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public byte[] readBuffer(int len) throws IOException {
        byte[] buffer = readRawBuffer(len);
        skipPadding(len);
        return buffer;
    }

    /**
     * Read in a raw array of bytes.
     *
     * @param len
     *         The number of bytes to read
     * @return The byte buffer that was read
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
     * @return The {@code String} that was read
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
     * @return The {@code long} that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public long readLong() throws IOException {
        byte[] buf = this.readBuffer;
        readFully(buf, 0, 8);
        return (buf[0] & 0xFFL) << 56 |
               (buf[1] & 0xFFL) << 48 |
               (buf[2] & 0xFFL) << 40 |
               (buf[3] & 0xFFL) << 32 |
               (buf[4] & 0xFFL) << 24 |
               (buf[5] & 0xFF) << 16 |
               (buf[6] & 0xFF) << 8 |
               buf[7] & 0xFF;
    }

    /**
     * Read in an {@code int}.
     *
     * @return The {@code int} that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public int readInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ch1 << 24 | ch2 << 16 | ch3 << 8 | ch4;
    }

    /**
     * Read in a {@code short}.
     *
     * @return The {@code short} that was read
     * @throws IOException
     *         if an error occurs while reading from the underlying input stream
     */
    public int readShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return ch1 << 8 | ch2;
    }

    /**
     * Read a given amount of data from the underlying input stream. The data
     * that is read is stored in {@code b}, starting from offset
     * {@code off}.
     *
     * @param b
     *         The byte buffer to hold the data that is read
     * @param off
     *         The offset at which to start storing data in {@code b}
     * @param len
     *         The number of bytes to be read
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
        if (currentStream instanceof EncryptedStreamSupport) {
            ((EncryptedStreamSupport) currentStream).setCipher(cipher);
        } else {
            in = new CipherInputStream(currentStream, cipher);
        }
        encrypted = true;
    }
}
