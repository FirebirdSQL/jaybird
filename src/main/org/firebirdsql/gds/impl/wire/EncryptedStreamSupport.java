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
package org.firebirdsql.gds.impl.wire;

import org.firebirdsql.util.InternalApi;

import javax.crypto.Cipher;
import java.io.IOException;

/**
 * Interface to support enabling encryption on a stream.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
