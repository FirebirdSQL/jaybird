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

/**
 * Interface for the encryption/decryption session config for wire protocol encryption for a specific plugin.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public interface CryptSessionConfig extends AutoCloseable {

    /**
     * @return Encryption identifier
     */
    EncryptionIdentifier getEncryptionIdentifier();

    /**
     * @return Encryption key
     */
    byte[] getEncryptKey();

    /**
     * @return Decryption key
     */
    byte[] getDecryptKey();

    /**
     * @return Plugin-specific data (can be {@code null})
     */
    byte[] getSpecificData();

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
            EncryptionIdentifier encryptionIdentifier, byte[] sessionKey, byte[] specificData) {
        if (encryptionIdentifier.isTypeSymmetric()) {
            return new CryptSessionConfigImpl(encryptionIdentifier, sessionKey, sessionKey, specificData);
        } else {
            throw new IllegalArgumentException(
                    "Wrong type for encryption identifier. Expected 'Symmetric/*', was '" + encryptionIdentifier + "'");
        }
    }

}
