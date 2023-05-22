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
package org.firebirdsql.gds.ng.wire.crypt;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * Crypt session config data for a specific plugin.
 *
 * @author Mark Rotteveel
 * @since 5
 */
record CryptSessionConfigImpl(
        EncryptionIdentifier encryptionIdentifier, byte[] encryptKey, byte[] decryptKey, byte[] specificData)
        implements CryptSessionConfig {

    CryptSessionConfigImpl {
        requireNonNull(encryptionIdentifier, "encryptionIdentifier");
        encryptKey = requireNonNull(encryptKey, "encryptKey").clone();
        decryptKey = requireNonNull(decryptKey, "decryptKey").clone();
        specificData = specificData != null ? specificData.clone() : null;
    }

    @Override
    public void close() {
        Arrays.fill(encryptKey, (byte) 0);
        Arrays.fill(decryptKey, (byte) 0);
        if (specificData != null) Arrays.fill(specificData, (byte) 0);
    }

}
