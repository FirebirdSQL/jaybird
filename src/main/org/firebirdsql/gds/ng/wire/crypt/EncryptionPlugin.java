// SPDX-FileCopyrightText: Copyright 2017-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire.crypt;

/**
 * Plugin for Firebird wire encryption.
 * <p>
 * NOTE: This plugin is currently only internal to Jaybird, consider the API as unstable.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public interface EncryptionPlugin {

    /**
     * @return Encryption identifier
     */
    EncryptionIdentifier encryptionIdentifier();

    /**
     * Initializes the encryption for incoming and outgoing communication.
     *
     * @return Object with the result of initialization
     */
    EncryptionInitInfo initializeEncryption();

}
