// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.crypt;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * Crypt session config data for a specific plugin.
 *
 * @author Mark Rotteveel
 * @since 5
 */
@SuppressWarnings("java:S6218")
record CryptSessionConfigImpl(
        EncryptionIdentifier encryptionIdentifier, byte[] encryptKey, byte[] decryptKey, byte @Nullable [] specificData)
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
