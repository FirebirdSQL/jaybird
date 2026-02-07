// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire.crypt;

import org.jspecify.annotations.Nullable;

/**
 * Interface for the encryption/decryption session config for wire protocol encryption for a specific plugin.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public interface CryptSessionConfig extends AutoCloseable {

    /**
     * @return Encryption identifier
     */
    EncryptionIdentifier encryptionIdentifier();

    /**
     * @return Encryption key
     */
    byte[] encryptKey();

    /**
     * @return Decryption key
     */
    byte[] decryptKey();

    /**
     * @return Plugin-specific data (can be {@code null})
     */
    byte @Nullable [] specificData();

    /**
     * Clears (e.g. zeroes out) the keys and specific data
     */
    @Override
    void close();

    /**
     * Creates a crypt session config for a type symmetric plugin.
     *
     * @param encryptionIdentifier
     *         Encryption identifier of type Symmetric
     * @param sessionKey
     *         Session key (non-{@code null})
     * @param specificData
     *         Plugin specific data (can be {@code null})
     * @return Crypt session config
     */
    static CryptSessionConfig symmetric(
            EncryptionIdentifier encryptionIdentifier, byte[] sessionKey, byte @Nullable [] specificData) {
        if (encryptionIdentifier.isTypeSymmetric()) {
            return new CryptSessionConfigImpl(encryptionIdentifier, sessionKey, sessionKey, specificData);
        } else {
            throw new IllegalArgumentException(
                    "Wrong type for encryption identifier. Expected 'Symmetric/*', was '" + encryptionIdentifier + "'");
        }
    }

}
