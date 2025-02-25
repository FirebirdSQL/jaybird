// SPDX-FileCopyrightText: Copyright 2017-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.crypt.arc4;

import org.firebirdsql.gds.ng.wire.crypt.CryptConnectionInfo;
import org.firebirdsql.gds.ng.wire.crypt.CryptSessionConfig;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPluginSpi;

/**
 * ARC4 encryption plugin provider.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public final class Arc4EncryptionPluginSpi implements EncryptionPluginSpi {

    static final EncryptionIdentifier ARC4_ID = new EncryptionIdentifier("Symmetric", "Arc4");

    @Override
    public EncryptionIdentifier encryptionIdentifier() {
        return ARC4_ID;
    }

    @Override
    public EncryptionPlugin createEncryptionPlugin(CryptSessionConfig cryptSessionConfig) {
        return new Arc4EncryptionPlugin(cryptSessionConfig);
    }

    @Override
    public boolean isSupported(CryptConnectionInfo cryptConnectionInfo) {
        // TODO Maybe check if ARC4 requirements are allowed by the security config?
        return true;
    }

}
