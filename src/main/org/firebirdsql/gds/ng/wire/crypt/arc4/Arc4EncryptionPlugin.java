// SPDX-FileCopyrightText: Copyright 2017-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.crypt.arc4;

import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.crypt.CryptSessionConfig;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionInitInfo;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import static org.firebirdsql.gds.JaybirdErrorCodes.*;

/**
 * ARC4 encryption plugin (the wire encryption provided out-of-the-box in Firebird 3).
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public final class Arc4EncryptionPlugin implements EncryptionPlugin {

    private static final String ARCFOUR_CIPHER_NAME = "ARCFOUR";
    
    private final CryptSessionConfig cryptSessionConfig;

    Arc4EncryptionPlugin(CryptSessionConfig cryptSessionConfig) {
        this.cryptSessionConfig = cryptSessionConfig;
    }

    @Override
    public EncryptionIdentifier encryptionIdentifier() {
        return Arc4EncryptionPluginSpi.ARC4_ID;
    }

    @Override
    public EncryptionInitInfo initializeEncryption() {
        try {
            return EncryptionInitInfo.success(
                    encryptionIdentifier(), createEncryptionCipher(), createDecryptionCipher());
        } catch (SQLException e) {
            return EncryptionInitInfo.failure(encryptionIdentifier(), e);
        }
    }

    private Cipher createEncryptionCipher() throws SQLException {
        return createCipher(Cipher.ENCRYPT_MODE, cryptSessionConfig.encryptKey());
    }

    private Cipher createDecryptionCipher() throws SQLException {
        return createCipher(Cipher.DECRYPT_MODE, cryptSessionConfig.decryptKey());
    }

    @SuppressWarnings({ "java:S5542", "java:S5547" })
    private Cipher createCipher(int mode, byte[] key) throws SQLException {
        try {
            var rc4Cipher = Cipher.getInstance(ARCFOUR_CIPHER_NAME);
            rc4Cipher.init(mode, new SecretKeySpec(key, ARCFOUR_CIPHER_NAME));
            return rc4Cipher;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw FbExceptionBuilder.forNonTransientException(jb_cryptAlgorithmNotAvailable)
                    .messageParameter(encryptionIdentifier())
                    .cause(e)
                    .toSQLException();
        } catch (InvalidKeyException e) {
            throw FbExceptionBuilder.forNonTransientException(jb_cryptInvalidKey)
                    .messageParameter(encryptionIdentifier())
                    .cause(e)
                    .toSQLException();
        }
    }
}
