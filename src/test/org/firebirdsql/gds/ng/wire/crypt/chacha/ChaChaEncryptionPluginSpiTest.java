// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.crypt.chacha;

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
 * Tests for {@link ChaChaEncryptionPluginSpi}.
 */
class ChaChaEncryptionPluginSpiTest {

    @Test
    void encryptionIdentifier() {
        assertEquals(new EncryptionIdentifier("Symmetric", "ChaCha"), new ChaChaEncryptionPluginSpi().encryptionIdentifier());
    }

    @ParameterizedTest
    @ValueSource(ints = { PROTOCOL_VERSION16, PROTOCOL_VERSION18 })
    void isSupported_true(int protocolVersion) {
        assertTrue(new ChaChaEncryptionPluginSpi().isSupported(new ConnectionInfoImpl(protocolVersion)));
    }

    @ParameterizedTest
    // NOTE: Implementation also reports false for PROTOCOL_VERSION10 - 12, which don't support wire encryption,
    // but we don't check those versions
    @ValueSource(ints = { PROTOCOL_VERSION13, PROTOCOL_VERSION15 })
    void isSupported_false(int protocolVersion) {
        assertFalse(new ChaChaEncryptionPluginSpi().isSupported(new ConnectionInfoImpl(protocolVersion)));
    }

    private record ConnectionInfoImpl(int protocolVersion) implements CryptConnectionInfo {
    }

}