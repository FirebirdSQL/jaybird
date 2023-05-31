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
package org.firebirdsql.gds.ng.wire.crypt.chacha;

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
}
