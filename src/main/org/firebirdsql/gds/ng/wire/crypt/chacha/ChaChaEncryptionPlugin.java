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
import org.firebirdsql.util.SQLExceptionChainBuilder;

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
 * ChaCha (ChaCha-20) encryption plugin (introduced in Firebird 4.0)
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class ChaChaEncryptionPlugin implements EncryptionPlugin {

    private static final String CHA_CHA_20_CIPHER_NAME = "ChaCha20";

    private final CryptSessionConfig cryptSessionConfig;

    ChaChaEncryptionPlugin(CryptSessionConfig cryptSessionConfig) {
        this.cryptSessionConfig = cryptSessionConfig;
    }

    @Override
    public EncryptionIdentifier getEncryptionIdentifier() {
        return ChaChaEncryptionPluginSpi.CHA_CHA_ID;
    }

    @Override
    public EncryptionInitInfo initializeEncryption() {
        SQLExceptionChainBuilder<SQLException> chainBuilder = new SQLExceptionChainBuilder<>();
        Cipher encryptionCipher = null;
        Cipher decryptionCipher = null;
        try (ChaChaIV iv = new ChaChaIV()) {
            encryptionCipher = createEncryptionCipher(iv, chainBuilder);
            decryptionCipher = createDecryptionCipher(iv, chainBuilder);
        } catch (SQLException e) {
            chainBuilder.append(e);
        }
        if (chainBuilder.hasException()) {
            return EncryptionInitInfo.failure(getEncryptionIdentifier(), chainBuilder.getException());
        }
        return EncryptionInitInfo.success(getEncryptionIdentifier(), encryptionCipher, decryptionCipher);
    }

    private Cipher createEncryptionCipher(ChaChaIV iv, SQLExceptionChainBuilder<SQLException> chainBuilder) {
        try {
            return createCipher(Cipher.ENCRYPT_MODE, iv, toChaChaKey(cryptSessionConfig.getEncryptKey()));
        } catch (SQLException e) {
            chainBuilder.append(e);
            return null;
        }
    }

    private Cipher createDecryptionCipher(ChaChaIV iv, SQLExceptionChainBuilder<SQLException> chainBuilder) {
        try {
            return createCipher(Cipher.DECRYPT_MODE, iv, toChaChaKey(cryptSessionConfig.getEncryptKey()));
        } catch (SQLException e) {
            chainBuilder.append(e);
            return null;
        }
    }

    private byte[] toChaChaKey(byte[] key) throws SQLException {
        if (key.length < 16) {
            throw new FbExceptionBuilder().nonTransientException(jb_cryptInvalidKey)
                    .messageParameter(getEncryptionIdentifier().toString())
                    .messageParameter("Key too short")
                    .toSQLException();
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(key);
        } catch (NoSuchAlgorithmException e) {
            throw new FbExceptionBuilder().nonTransientException(jb_cryptAlgorithmNotAvailable)
                    .messageParameter(getEncryptionIdentifier().toString())
                    .cause(e)
                    .toSQLException();
        }
    }

    private Cipher createCipher(int mode, ChaChaIV iv, byte[] key) throws SQLException {
        try {
            Cipher chaChaCipher = Cipher.getInstance(CHA_CHA_20_CIPHER_NAME);
            SecretKeySpec chaChaKey = new SecretKeySpec(key, CHA_CHA_20_CIPHER_NAME);
            chaChaCipher.init(mode, chaChaKey, iv.toParameterSpec());
            return chaChaCipher;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new FbExceptionBuilder().nonTransientException(jb_cryptAlgorithmNotAvailable)
                    .messageParameter(getEncryptionIdentifier().toString()).cause(e).toSQLException();
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new FbExceptionBuilder().nonTransientException(jb_cryptInvalidKey)
                    .messageParameter(getEncryptionIdentifier().toString()).cause(e).toSQLException();
        }
    }

    private class ChaChaIV implements AutoCloseable {

        private byte[] nonce;
        private int counter;

        ChaChaIV() throws SQLException {
            byte[] iv = cryptSessionConfig.getSpecificData();
            if (iv == null || !(iv.length == 12 || iv.length == 16)) {
                throw new FbExceptionBuilder().nonTransientException(jb_cryptInvalidKey)
                        .messageParameter(getEncryptionIdentifier().toString())
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
            nonce = null;
            counter = -1;
        }
    }

}
