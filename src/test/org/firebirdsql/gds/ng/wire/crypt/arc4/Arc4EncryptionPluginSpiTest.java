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
package org.firebirdsql.gds.ng.wire.crypt.arc4;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Arc4EncryptionPluginSpi}.
 */
class Arc4EncryptionPluginSpiTest {

    @Test
    void encryptionIdentifier() {
        assertEquals(new EncryptionIdentifier("Symmetric", "Arc4"), new Arc4EncryptionPluginSpi().encryptionIdentifier());
    }

    @ParameterizedTest
    // NOTE: Implementation always reports true, also for PROTOCOL_VERSION10 - 12, which don't support wire encryption,
    // but we don't check those versions
    @ValueSource(ints = { PROTOCOL_VERSION13, PROTOCOL_VERSION15, PROTOCOL_VERSION16, PROTOCOL_VERSION18 })
    void isSupported_true(int protocolVersion) {
        assertTrue(new Arc4EncryptionPluginSpi().isSupported(new ConnectionInfoImpl(protocolVersion)));
    }

    private record ConnectionInfoImpl(int protocolVersion) implements CryptConnectionInfo {
    }

}