// SPDX-FileCopyrightText: Copyright 2017-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire.crypt;

import javax.crypto.Cipher;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * The initial initialisation information of an encryption plugin.
 * <p>
 * Communicates success or failure, and contains the ciphers for encryption and decryption.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public abstract sealed class EncryptionInitInfo {

    private final EncryptionIdentifier encryptionIdentifier;

    private EncryptionInitInfo(EncryptionIdentifier encryptionIdentifier) {
        this.encryptionIdentifier = encryptionIdentifier;
    }

    public static EncryptionInitInfo success(EncryptionIdentifier encryptionIdentifier, Cipher encryptionCipher,
            Cipher decryptionCipher) {
        return new Success(encryptionIdentifier, encryptionCipher, decryptionCipher);
    }

    public static EncryptionInitInfo failure(EncryptionIdentifier encryptionIdentifier, SQLException cause) {
        return new Failure(encryptionIdentifier, cause);
    }

    public EncryptionIdentifier getEncryptionIdentifier() {
        return encryptionIdentifier;
    }

    /**
     * Encryption information for successful encryption setup.
     *
     * @since 7
     */
    public static final class Success extends EncryptionInitInfo {

        private final Cipher encryptionCipher;
        private final Cipher decryptionCipher;

        private Success(EncryptionIdentifier encryptionIdentifier,
                Cipher encryptionCipher, Cipher decryptionCipher) {
            super(encryptionIdentifier);
            this.encryptionCipher = requireNonNull(encryptionCipher, "encryptionCipher");
            this.decryptionCipher = requireNonNull(decryptionCipher, "decryptionCipher");
        }

        public Cipher getEncryptionCipher() {
            return encryptionCipher;
        }

        public Cipher getDecryptionCipher() {
            return decryptionCipher;
        }
    }

    /**
     * Encryption information for failed encryption setup.
     *
     * @since 7
     */
    public static final class Failure extends EncryptionInitInfo {

        private final SQLException cause;

        private Failure(EncryptionIdentifier encryptionIdentifier, SQLException cause) {
            super(encryptionIdentifier);
            this.cause = requireNonNull(cause, "cause");
        }

        public SQLException getCause() {
            return cause;
        }

    }

}
