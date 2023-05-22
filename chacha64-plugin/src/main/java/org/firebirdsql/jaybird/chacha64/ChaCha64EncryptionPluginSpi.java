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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
public class ChaCha64EncryptionPluginSpi implements EncryptionPluginSpi {

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

    private static Provider createProvider() {
        // TODO Maybe create a reduced provider which only provides Bouncy Castle's ChaCha implementation?
        return new BouncyCastleProvider();
    }

}
