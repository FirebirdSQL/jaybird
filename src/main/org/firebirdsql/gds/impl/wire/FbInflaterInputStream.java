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

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static java.lang.System.Logger.Level.TRACE;

/**
 * {@code InflaterOutputStream} with some modifications to simplify usage for Jaybird.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
class FbInflaterInputStream extends InflaterInputStream implements EncryptedStreamSupport {

    private static final System.Logger log = System.getLogger(FbInflaterInputStream.class.getName());
    private static final int BUF_SIZE = Math.max(512, JaybirdSystemProperties.getWireInflateBufferSize(8192));

    private boolean encrypted;

    /**
     * Creates a {@code InflaterInputStream}.
     *
     * @param in
     *         Input stream
     */
    public FbInflaterInputStream(InputStream in) {
        super(in, new Inflater(), BUF_SIZE);
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            buf = new byte[1];
            if (log.isLoggable(TRACE)) {
                log.log(TRACE, "FbInflaterInputStream: Compressed bytes: {0} to uncompressed bytes: {1}",
                        inf.getBytesRead(), inf.getBytesWritten());
            }
            inf.end();
        }
    }

    @Override
    public void setCipher(Cipher cipher) throws IOException {
        if (encrypted) {
            throw new IOException("Input stream already encrypted");
        }
        in = new FbCipherInputStream(in, cipher);
        encrypted = true;
    }
}
