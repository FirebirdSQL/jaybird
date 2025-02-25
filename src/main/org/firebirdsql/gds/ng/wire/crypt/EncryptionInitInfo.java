// SPDX-FileCopyrightText: Copyright 2017 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire.crypt;

import javax.crypto.Cipher;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * The initial initialization information of an encryption plugin.
 * <p>
 * Communicates success or failure, and contains the ciphers for encryption and decryption.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public final class EncryptionInitInfo {

    private static final SQLException NO_EXCEPTION = null;
    private static final Cipher NO_CIPHER = null;

    private final EncryptionIdentifier encryptionIdentifier;
    private final SQLException exception;
    private final InitResult initResult;
    private final Cipher encryptionCipher;
    private final Cipher decryptionCipher;

    private EncryptionInitInfo(EncryptionIdentifier encryptionIdentifier, InitResult initResult,
            Cipher encryptionCipher, Cipher decryptionCipher, SQLException exception) {
        this.encryptionIdentifier = encryptionIdentifier;
        this.exception = exception;
        this.initResult = initResult;
        this.encryptionCipher = encryptionCipher;
        this.decryptionCipher = decryptionCipher;
    }

    public static EncryptionInitInfo success(
            EncryptionIdentifier encryptionIdentifier, Cipher encryptionCipher,
            Cipher decryptionCipher) {
        return new EncryptionInitInfo(encryptionIdentifier, InitResult.SUCCESS,
                requireNonNull(encryptionCipher, "encryptionCipher"),
                requireNonNull(decryptionCipher, "decryptionCipher"),
                NO_EXCEPTION);
    }

    public static EncryptionInitInfo failure(
            EncryptionIdentifier encryptionIdentifier, SQLException exception) {
        return new EncryptionInitInfo(encryptionIdentifier, InitResult.FAILURE, NO_CIPHER, NO_CIPHER,
                requireNonNull(exception, "exception"));
    }

    public EncryptionIdentifier getEncryptionIdentifier() {
        return encryptionIdentifier;
    }

    public InitResult getInitResult() {
        return initResult;
    }

    public boolean isSuccess() {
        return initResult == InitResult.SUCCESS;
    }

    public Cipher getEncryptionCipher() {
        if (initResult != InitResult.SUCCESS) {
            throw new IllegalStateException("Getting cipher only allowed when initResult is SUCCESS, was " + initResult);
        }
        return encryptionCipher;
    }

    public Cipher getDecryptionCipher() {
        if (initResult != InitResult.SUCCESS) {
            throw new IllegalStateException("Getting cipher only allowed when initResult is SUCCESS, was " + initResult);
        }
        return decryptionCipher;
    }

    /**
     * @return {@code null} on SUCCESS, otherwise exception with the cause of failure (multiple exceptions may be chained!)
     */
    public SQLException getException() {
        return exception;
    }

    public enum InitResult {
        SUCCESS,
        FAILURE
    }
}
