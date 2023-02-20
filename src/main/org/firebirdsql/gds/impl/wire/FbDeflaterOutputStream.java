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
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * {@code DeflaterOutputStream} with some modifications to simplify usage for Jaybird.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
class FbDeflaterOutputStream extends DeflaterOutputStream implements EncryptedStreamSupport {

    private static final Logger log = LoggerFactory.getLogger(FbDeflaterOutputStream.class);
    private static final int BUF_SIZE = Math.max(512, JaybirdSystemProperties.getWireDeflateBufferSize(8192));

    private boolean encrypted;

    /**
     * Creates a {@code DeflaterOutputStream} with {@code syncFlush = true}.
     *
     * @param out
     *         Output stream
     */
    public FbDeflaterOutputStream(OutputStream out) {
        super(out, new Deflater(), BUF_SIZE, true);
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            buf = new byte[1];
            if (log.isTraceEnabled()) {
                log.tracef("FbDeflaterOutputStream: Uncompressed bytes: %d to compressed bytes: %d",
                        def.getBytesRead(), def.getBytesWritten());
            }
            def.end();
        }
    }

    @Override
    public void setCipher(Cipher cipher) throws IOException {
        if (encrypted) {
            throw new IOException("Output stream already encrypted");
        }
        out = new CipherOutputStream(out, cipher);
        encrypted = true;
    }
}
