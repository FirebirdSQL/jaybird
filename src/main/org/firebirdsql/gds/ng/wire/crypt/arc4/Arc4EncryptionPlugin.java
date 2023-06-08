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
