// SPDX-FileCopyrightText: Copyright 2021-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.crypt.chacha;

import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.ng.wire.crypt.CryptConnectionInfo;
import org.firebirdsql.gds.ng.wire.crypt.CryptSessionConfig;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPluginSpi;

/**
 * ChaCha (ChaCha-20) encryption plugin provider.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class ChaChaEncryptionPluginSpi implements EncryptionPluginSpi {

    static final EncryptionIdentifier CHA_CHA_ID = new EncryptionIdentifier("Symmetric", "ChaCha");

    @Override
    public EncryptionIdentifier encryptionIdentifier() {
        return CHA_CHA_ID;
    }

    @Override
    public EncryptionPlugin createEncryptionPlugin(CryptSessionConfig cryptSessionConfig) {
        return new ChaChaEncryptionPlugin(cryptSessionConfig);
    }

    @Override
    public boolean isSupported(CryptConnectionInfo cryptConnectionInfo) {
        return cryptConnectionInfo.protocolVersion() >= WireProtocolConstants.PROTOCOL_VERSION16;
    }

}
