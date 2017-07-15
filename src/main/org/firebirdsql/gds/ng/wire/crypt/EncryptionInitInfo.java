/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import javax.crypto.Cipher;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * The initial initialization information of an encryption plugin.
 * <p>
 * Communicates success or failure, and contains the ciphers for encryption and decryption.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.1
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
