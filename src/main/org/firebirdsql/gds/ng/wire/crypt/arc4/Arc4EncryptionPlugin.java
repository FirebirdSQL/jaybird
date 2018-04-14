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
package org.firebirdsql.gds.ng.wire.crypt.arc4;

import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionInitInfo;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptAlgorithmNotAvailable;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptInvalidKey;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptNoCryptKeyAvailable;

/**
 * ARC4 encryption plugin (the wire encryption provided out-of-the-box in Firebird 3).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0.4
 */
public final class Arc4EncryptionPlugin implements EncryptionPlugin {

    private final ClientAuthBlock clientAuthBlock;

    public Arc4EncryptionPlugin(ClientAuthBlock clientAuthBlock) {
        this.clientAuthBlock = clientAuthBlock;
    }

    @Override
    public EncryptionIdentifier getEncryptionIdentifier() {
        return Arc4EncryptionPluginSpi.ARC4_ID;
    }

    @Override
    public EncryptionInitInfo initializeEncryption() {
        SQLExceptionChainBuilder<SQLException> chainBuilder = new SQLExceptionChainBuilder<>();
        byte[] key = getKey(chainBuilder);
        Cipher encryptionCipher = createEncryptionCipher(key, chainBuilder);
        Cipher decryptionCipher = createDecryptionCipher(key, chainBuilder);

        if (chainBuilder.hasException()) {
            return EncryptionInitInfo.failure(getEncryptionIdentifier(), chainBuilder.getException());
        }
        return EncryptionInitInfo.success(getEncryptionIdentifier(), encryptionCipher, decryptionCipher);
    }

    private byte[] getKey(SQLExceptionChainBuilder<SQLException> chainBuilder) {
        byte[] key = null;
        try {
            key = getKey(clientAuthBlock);
        } catch (SQLException e) {
            chainBuilder.append(e);
        }
        return key;
    }

    private Cipher createEncryptionCipher(byte[] key, SQLExceptionChainBuilder<SQLException> chainBuilder) {
        return createCipher(Cipher.ENCRYPT_MODE, key, chainBuilder);
    }

    private Cipher createDecryptionCipher(byte[] key, SQLExceptionChainBuilder<SQLException> chainBuilder) {
        return createCipher(Cipher.DECRYPT_MODE, key, chainBuilder);
    }

    private byte[] getKey(ClientAuthBlock clientAuthBlock) throws SQLException {
        if (!clientAuthBlock.supportsEncryption()) {
            throw new FbExceptionBuilder().nonTransientException(jb_cryptNoCryptKeyAvailable)
                    .messageParameter(getEncryptionIdentifier().toString())
                    .toFlatSQLException();
        }
        return clientAuthBlock.getSessionKey();
    }

    private Cipher createCipher(int mode, byte[] key, SQLExceptionChainBuilder<SQLException> chainBuilder) {
        try {
            return createCipher(mode, key);
        } catch (SQLException e) {
            chainBuilder.append(e);
            return null;
        }
    }

    private Cipher createCipher(int mode, byte[] key) throws SQLException {
        try {
            Cipher rc4Cipher = Cipher.getInstance("ARCFOUR");
            SecretKeySpec rc4Key = new SecretKeySpec(key, "ARCFOUR");
            rc4Cipher.init(mode, rc4Key);
            return rc4Cipher;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new FbExceptionBuilder().nonTransientException(jb_cryptAlgorithmNotAvailable)
                    .messageParameter(getEncryptionIdentifier().toString())
                    .cause(e).toFlatSQLException();
        } catch (InvalidKeyException e) {
            throw new FbExceptionBuilder().nonTransientException(jb_cryptInvalidKey)
                    .messageParameter(getEncryptionIdentifier().toString())
                    .cause(e).toFlatSQLException();
        }
    }
}
