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
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.gds.JaybirdSystemProperties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Modified version of {@link javax.crypto.CipherInputStream} with a larger buffer size.
 */
final class FbCipherInputStream extends FilterInputStream {

    private static final int BUF_SIZE = Math.max(512, JaybirdSystemProperties.getWireDecryptBufferSize(8192));

    private final Cipher cipher;
    private byte[] inBuf;
    private byte[] outBuf = null;
    private int outPos = 0;
    private int outLim = 0;
    private boolean eof = false;
    private boolean closed = false;

    public FbCipherInputStream(InputStream is, Cipher c) {
        super(is);
        cipher = c;
        inBuf = new byte[BUF_SIZE];
    }

    private void ensureCapacity(int inLen) {
        int minLen = cipher.getOutputSize(inLen);
        if (outBuf == null || outBuf.length < minLen) {
            outBuf = new byte[minLen];
        }
        outPos = 0;
        outLim = 0;
    }

    private int getMoreData() throws IOException {
        if (eof) return -1;
        int readin = in.read(inBuf);

        if (readin == -1) {
            eof = true;
            ensureCapacity(0);
            try {
                outLim = cipher.doFinal(outBuf, 0);
            } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
                throw new IOException(e);
            }
            return outLim == 0 ? -1 : outLim;
        }
        ensureCapacity(readin);
        try {
            outLim = cipher.update(inBuf, 0, readin, outBuf, outPos);
        } catch (ShortBufferException e) {
            throw new IOException(e);
        }
        return outLim;
    }

    @Override
    public int read() throws IOException {
        if (outPos >= outLim) {
            int i;
            do {
                i = getMoreData();
            } while (i == 0);
            if (i == -1) return -1;
        }
        return (int) outBuf[outPos++] & 0xFF;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (outPos >= outLim) {
            int i;
            do {
                i = getMoreData();
            } while (i == 0);
            if (i == -1) return -1;
        }
        if (len <= 0) return 0;
        int available = Math.min(outLim - outPos, len);
        if (b != null) {
            System.arraycopy(outBuf, outPos, b, off, available);
        }
        outPos += available;
        return available;
    }

    @Override
    public long skip(long n) {
        n = Math.min(outLim - outPos, n);
        if (n < 0) {
            return 0;
        }
        outPos += n;
        return n;
    }

    @Override
    public int available() {
        return outLim - outPos;
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        try {
            in.close();

            if (!eof) {
                ensureCapacity(0);
                try {
                    cipher.doFinal(outBuf, 0);
                } catch (BadPaddingException | IllegalBlockSizeException | ShortBufferException ignored) {
                }
            }
        } finally {
            outBuf = null;
            inBuf = new byte[1];
        }
    }

    @Override
    public boolean markSupported() {
        return false;
    }

}
