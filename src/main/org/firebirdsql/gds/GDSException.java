/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds;

import java.io.Serial;
import java.sql.SQLException;

/**
 * A GDS-specific exception
 * <p>
 * NOTE: This class is only retained for some compatibility with older implementations of
 * {@link org.firebirdsql.gds.impl.GDSFactoryPlugin}
 * which previously declared {@code throws GDSException} for some of its {@code getDatabasePath} methods.
 * </p>
 *
 * @author Mark Rotteveel
 * @deprecated Use a normal SQLException or subclass, this class may be removed in Jaybird 7 or later
 */
@Deprecated
public class GDSException extends SQLException {

    @Serial
    private static final long serialVersionUID = 8356102488501011522L;

    /**
     * Create a new instance.
     *
     * @param fbErrorCode
     *         Firebird error code, one of the constants declared in {@link ISCConstants}
     */
    public GDSException(int fbErrorCode) {
        super(GDSExceptionHelper.getMessage(fbErrorCode).toString(), null, fbErrorCode);
    }

    /**
     * Create a new instance.
     *
     * @param fbErrorCode
     *         Firebird error code, one of the constants declared in {@link ISCConstants}
     * @param cause
     *         Cause of this exception
     */
    public GDSException(int fbErrorCode, Throwable cause) {
        this(fbErrorCode);
        initCause(cause);
    }

    /**
     * Create a new instance with only a simple message.
     *
     * @param message
     *         Message for the new exception
     */
    public GDSException(String message) {
        super(message);
    }

}
