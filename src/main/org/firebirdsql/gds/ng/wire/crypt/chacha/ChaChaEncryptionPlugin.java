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
package org.firebirdsql.gds.ng.wire.crypt.chacha;

import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.crypt.CryptSessionConfig;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionInitInfo;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptAlgorithmNotAvailable;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptInvalidKey;

/**
 * ChaCha (ChaCha-20) encryption plugin (introduced in Firebird 4.0).
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class ChaChaEncryptionPlugin implements EncryptionPlugin {

    private static final String CHA_CHA_20_CIPHER_NAME = "ChaCha20";

    private final CryptSessionConfig cryptSessionConfig;

    ChaChaEncryptionPlugin(CryptSessionConfig cryptSessionConfig) {
        this.cryptSessionConfig = cryptSessionConfig;
    }

    @Override
    public EncryptionIdentifier encryptionIdentifier() {
        return ChaChaEncryptionPluginSpi.CHA_CHA_ID;
    }

    @Override
    public EncryptionInitInfo initializeEncryption() {
        try (var iv = new ChaChaIV()) {
            return EncryptionInitInfo.success(
                    encryptionIdentifier(), createEncryptionCipher(iv), createDecryptionCipher(iv));
        } catch (SQLException e) {
            return EncryptionInitInfo.failure(encryptionIdentifier(), e);
        }
    }

    private Cipher createEncryptionCipher(ChaChaIV iv) throws SQLException {
        return createCipher(Cipher.ENCRYPT_MODE, iv, toChaChaKey(cryptSessionConfig.encryptKey()));
    }

    private Cipher createDecryptionCipher(ChaChaIV iv) throws SQLException {
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

    private Cipher createCipher(int mode, ChaChaIV iv, byte[] key) throws SQLException {
        try {
            var chaChaCipher = Cipher.getInstance(CHA_CHA_20_CIPHER_NAME);
            chaChaCipher.init(mode, new SecretKeySpec(key, CHA_CHA_20_CIPHER_NAME), iv.toParameterSpec());
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

    private class ChaChaIV implements AutoCloseable {

        private final byte[] nonce;
        private int counter;

        ChaChaIV() throws SQLException {
            byte[] iv = cryptSessionConfig.specificData();
            if (iv == null || !(iv.length == 12 || iv.length == 16)) {
                throw FbExceptionBuilder.forNonTransientException(jb_cryptInvalidKey)
                        .messageParameter(encryptionIdentifier())
                        .messageParameter("Wrong IV length, needs 12 or 16 bytes")
                        .toSQLException();
            }

            nonce = Arrays.copyOf(iv, 12);
            if (iv.length == 16) {
                counter = (iv[12] << 24) + (iv[13] << 16) + (iv[14] << 8) + iv[15];
            }
        }

        ChaCha20ParameterSpec toParameterSpec() {
            return new ChaCha20ParameterSpec(nonce, counter);
        }

        @Override
        public void close() {
            Arrays.fill(nonce, (byte) 0);
            counter = -1;
        }
    }

}
