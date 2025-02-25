// SPDX-FileCopyrightText: Copyright 2019 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.util.InternalApi;

import javax.crypto.Cipher;
import java.io.IOException;

/**
 * Interface to support enabling encryption on a stream.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
@InternalApi
interface EncryptedStreamSupport {

    /**
     * Wraps the underlying stream for encryption using the provided {@code cipher}.
     * <p>
     * An implementation wrapping a stream that also implements {@code EncryptedStreamSupport} may delegate this call to
     * that wrapped stream.
     * </p>
     *
     * @param cipher
     *         Cipher for the stream
     * @throws IOException
     *         If the underlying stream is already wrapped
     */
    void setCipher(Cipher cipher) throws IOException;
}
