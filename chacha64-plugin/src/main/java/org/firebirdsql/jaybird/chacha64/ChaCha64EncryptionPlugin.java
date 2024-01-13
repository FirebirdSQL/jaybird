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
package org.firebirdsql.jaybird.chacha64;

import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.crypt.CryptSessionConfig;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionInitInfo;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.sql.SQLException;
import java.util.Arrays;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptAlgorithmNotAvailable;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptInvalidKey;

/**
 * ChaCha64 (ChaCha with 64-bit counter) encryption plugin (introduced in Firebird 4.0.1).
 *
 * @author Mark Rotteveel
 * @since 6
 */
public class ChaCha64EncryptionPlugin implements EncryptionPlugin {

    private static final String CHA_CHA_CIPHER_NAME = "ChaCha";

    private final CryptSessionConfig cryptSessionConfig;
    private final Provider provider;

    ChaCha64EncryptionPlugin(CryptSessionConfig cryptSessionConfig, Provider provider) {
        this.cryptSessionConfig = cryptSessionConfig;
        this.provider = provider;
    }

    @Override
    public EncryptionIdentifier encryptionIdentifier() {
        return ChaCha64EncryptionPluginSpi.CHA_CHA_64_ID;
    }

    @Override
    public EncryptionInitInfo initializeEncryption() {
        try (var iv = new ChaCha64IV()) {
            return EncryptionInitInfo.success(
                    encryptionIdentifier(), createEncryptionCipher(iv), createDecryptionCipher(iv));
        } catch (SQLException e) {
            return EncryptionInitInfo.failure(encryptionIdentifier(), e);
        }
    }

    private Cipher createEncryptionCipher(ChaCha64IV iv) throws SQLException {
        return createCipher(Cipher.ENCRYPT_MODE, iv, toChaChaKey(cryptSessionConfig.encryptKey()));
    }

    private Cipher createDecryptionCipher(ChaCha64IV iv) throws SQLException {
        return createCipher(Cipher.DECRYPT_MODE, iv, toChaChaKey(cryptSessionConfig.decryptKey()));
    }

    private byte[] toChaChaKey(byte[] key) throws SQLException {
        if (key.length < 16) {
            throw FbExceptionBuilder.forNonTransientException(jb_cryptInvalidKey)
                    .messageParameter(encryptionIdentifier())
                    .messageParameter("Key too short")
                    .toSQLException();
        }
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return md.digest(key);
        } catch (NoSuchAlgorithmException e) {
            throw FbExceptionBuilder.forNonTransientException(jb_cryptAlgorithmNotAvailable)
                    .messageParameter(encryptionIdentifier())
                    .cause(e)
                    .toSQLException();
        }
    }

    @SuppressWarnings("java:S5542")
    private Cipher createCipher(int mode, ChaCha64IV iv, byte[] key) throws SQLException {
        try {
            var chaChaCipher = Cipher.getInstance(CHA_CHA_CIPHER_NAME, provider);
            chaChaCipher.init(mode, new SecretKeySpec(key, CHA_CHA_CIPHER_NAME), iv.toParameterSpec());
            return chaChaCipher;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw FbExceptionBuilder.forNonTransientException(jb_cryptAlgorithmNotAvailable)
                    .messageParameter(encryptionIdentifier())
                    .cause(e)
                    .toSQLException();
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw FbExceptionBuilder.forNonTransientException(jb_cryptInvalidKey)
                    .messageParameter(encryptionIdentifier())
                    .cause(e)
                    .toSQLException();
        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    private class ChaCha64IV implements AutoCloseable {

        private final byte[] nonce;

        ChaCha64IV() throws SQLException {
            byte[] iv = cryptSessionConfig.specificData();
            if (iv == null || iv.length != 8) {
                throw FbExceptionBuilder.forNonTransientException(jb_cryptInvalidKey)
                        .messageParameter(encryptionIdentifier())
                        .messageParameter("Wrong IV length, needs 8 bytes")
                        .toSQLException();
            }

            nonce = iv.clone();
        }

        @SuppressWarnings("java:S3329")
        IvParameterSpec toParameterSpec() {
            return new IvParameterSpec(nonce);
        }

        @Override
        public void close() {
            Arrays.fill(nonce, (byte) 0);
        }
    }

}
