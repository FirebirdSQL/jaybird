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
package org.firebirdsql.jaybird.chacha64;

import org.firebirdsql.gds.ng.wire.crypt.CryptConnectionInfo;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.PROTOCOL_VERSION13;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.PROTOCOL_VERSION15;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.PROTOCOL_VERSION16;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.PROTOCOL_VERSION18;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mark Rotteveel
 */
class ChaCha64EncryptionPluginSpiTest {

    @Test
    void encryptionIdentifier() {
        assertEquals(new EncryptionIdentifier("Symmetric", "ChaCha64"), new ChaCha64EncryptionPluginSpi().encryptionIdentifier());
    }

    @ParameterizedTest
    @ValueSource(ints = { PROTOCOL_VERSION16, PROTOCOL_VERSION18 })
    void isSupported_true(int protocolVersion) {
        assertTrue(new ChaCha64EncryptionPluginSpi().isSupported(new ConnectionInfoImpl(protocolVersion)));
    }

    @ParameterizedTest
    // NOTE: Implementation also reports false for PROTOCOL_VERSION10 - 12, which don't support wire encryption,
    // but we don't check those versions
    @ValueSource(ints = { PROTOCOL_VERSION13, PROTOCOL_VERSION15 })
    void isSupported_false(int protocolVersion) {
        assertFalse(new ChaCha64EncryptionPluginSpi().isSupported(new ConnectionInfoImpl(protocolVersion)));
    }

    private record ConnectionInfoImpl(int protocolVersion) implements CryptConnectionInfo {
    }

}