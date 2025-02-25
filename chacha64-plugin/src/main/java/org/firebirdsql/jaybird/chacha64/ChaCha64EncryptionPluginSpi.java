// SPDX-CopyrightText: Copyright 2023-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.chacha64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.wire.crypt.CryptConnectionInfo;
import org.firebirdsql.gds.ng.wire.crypt.CryptSessionConfig;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPluginSpi;

import java.security.Provider;
import java.security.Security;

import static java.util.Objects.requireNonNullElseGet;

/**
 * ChaCha64 (ChaCha with 64-bit counter) encryption plugin provider.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class ChaCha64EncryptionPluginSpi implements EncryptionPluginSpi {

    static final EncryptionIdentifier CHA_CHA_64_ID = new EncryptionIdentifier("Symmetric", "ChaCha64");
    // Use registered Bouncy Castle if possible, otherwise use our own unregistered instance
    private final Provider provider = requireNonNullElseGet(
            Security.getProvider("BC"), ChaCha64EncryptionPluginSpi::createProvider);

    @Override
    public EncryptionIdentifier encryptionIdentifier() {
        return CHA_CHA_64_ID;
    }

    @Override
    public EncryptionPlugin createEncryptionPlugin(CryptSessionConfig cryptSessionConfig) {
        return new ChaCha64EncryptionPlugin(cryptSessionConfig, provider);
    }

    @Override
    public boolean isSupported(CryptConnectionInfo cryptConnectionInfo) {
        return cryptConnectionInfo.protocolVersion() >= WireProtocolConstants.PROTOCOL_VERSION16;
    }

    private static Provider createProvider() {
        return new BouncyCastleProvider();
    }

}
