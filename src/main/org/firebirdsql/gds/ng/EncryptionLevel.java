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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public enum EncryptionLevel {
    REQUIRED(WireProtocolConstants.WIRE_CRYPT_REQUIRED),
    ENABLED(WireProtocolConstants.WIRE_CRYPT_ENABLED),
    DISABLED(WireProtocolConstants.WIRE_CRYPT_DISABLED),
    /**
     * Equivalent to {@link #ENABLED} for wire protocol, but for JNA connections
     * it uses the default of the {@code firebird.conf} used by the client library.
     */
    DEFAULT(WireProtocolConstants.WIRE_CRYPT_ENABLED);

    private final int wireProtocolCryptLevel;

    EncryptionLevel(int wireProtocolCryptLevel) {
        this.wireProtocolCryptLevel = wireProtocolCryptLevel;
    }

    /**
     * @return Encryption level value for the wire protocol.
     */
    public int getWireProtocolCryptLevel() {
        return wireProtocolCryptLevel;
    }
}
