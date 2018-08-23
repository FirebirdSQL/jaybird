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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public enum WireCrypt {
    REQUIRED(WireProtocolConstants.WIRE_CRYPT_REQUIRED),
    ENABLED(WireProtocolConstants.WIRE_CRYPT_ENABLED),
    DISABLED(WireProtocolConstants.WIRE_CRYPT_DISABLED),
    /**
     * Equivalent to {@link #ENABLED} for wire protocol, but for JNA connections
     * it uses the default of the {@code firebird.conf} used by the client library.
     */
    DEFAULT(WireProtocolConstants.WIRE_CRYPT_ENABLED);

    private final int wireProtocolCryptLevel;

    WireCrypt(int wireProtocolCryptLevel) {
        this.wireProtocolCryptLevel = wireProtocolCryptLevel;
    }

    /**
     * @return Encryption level value for the wire protocol.
     */
    public int getWireProtocolCryptLevel() {
        return wireProtocolCryptLevel;
    }

    /**
     * Get the enum value for the provided name, case-insensitive.
     * <p>
     * Works like {@link #valueOf(String)}, except {@code null} will return {@link #DEFAULT} and values
     * are handled case-insensitively.
     * </p>
     *
     * @param name String name
     * @return Enum name for the name
     * @throws IllegalArgumentException if this enum type has no constant with the specified name
     */
    public static WireCrypt fromString(String name) throws IllegalArgumentException {
        if (name == null) {
            return DEFAULT;
        }
        String uppercaseValue = name.toUpperCase();
        return valueOf(uppercaseValue);
    }
}
