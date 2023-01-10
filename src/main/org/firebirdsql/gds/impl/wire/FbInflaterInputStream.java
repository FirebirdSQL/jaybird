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

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * {@code InflaterOutputStream} with some modifications to simplify usage for Jaybird.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
class FbInflaterInputStream extends InflaterInputStream implements EncryptedStreamSupport {

    private static final Logger log = LoggerFactory.getLogger(FbInflaterInputStream.class);

    private boolean encrypted;

    /**
     * Creates a {@code DeflaterOutputStream} with {@code syncFlush = true}.
     *
     * @param in Input stream
     */
    public FbInflaterInputStream(InputStream in) {
        super(in, new Inflater());
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (log.isTraceEnabled()) {
                log.tracef("FbInflaterInputStream: Compressed bytes: %d to uncompressed bytes: %d",
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
        in = new CipherInputStream(in, cipher);
        encrypted = true;
    }
}
