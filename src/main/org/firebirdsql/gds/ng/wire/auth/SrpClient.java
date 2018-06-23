/*
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// https://github.com/nakagami/pyfirebirdsql/blob/master/firebirdsql/srp.py

package org.firebirdsql.gds.ng.wire.auth;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.util.ByteArrayHelper;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * SRP client handshake implementation.
 *
 * @author <a href="mailto:nakagami@gmail.com">Hajime Nakagami</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class SrpClient {
    
    private static final int SRP_KEY_SIZE = 128;
    private static final int SRP_SALT_SIZE = 32;
    private static final int EXPECTED_AUTH_DATA_LENGTH = (SRP_SALT_SIZE + SRP_KEY_SIZE + 2) * 2;

    private static final BigInteger N = new BigInteger("E67D2E994B2F900C3F41F08F5BB2627ED0D49EE1FE767A52EFCD565CD6E768812C3E1E9CE8F0A8BEA6CB13CD29DDEBF7A96D4A93B55D488DF099A15C89DCB0640738EB2CBDD9A8F7BAB561AB1B0DC1C6CDABF303264A08D1BCA932D1F1EE428B619D970F342ABA9A65793B8B2F041AE5364350C16F735F56ECBCA87BD57B29E7", 16);
    private static final BigInteger g = new BigInteger("2");
    private static final BigInteger k = new BigInteger("1277432915985975349439481660349303019122249719989");

    private static final SecureRandom random = new SecureRandom();
    private static final byte SEPARATOR_BYTE = (byte) ':';

    private final MessageDigest sha1Md;
    private final String clientProofHashAlgorithm;
    private final BigInteger publicKey;   /* A */
    private final BigInteger privateKey;  /* a */
    private byte[] sessionKey;      /* K */

    public SrpClient(String clientProofHashAlgorithm) {
        this.clientProofHashAlgorithm = clientProofHashAlgorithm;
        privateKey = getSecret();
        publicKey = g.modPow(privateKey, N);
        try {
            sha1Md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            // Should not happen
            throw new RuntimeException("SHA-1 MessageDigest not available", e);
        }
    }

    private static BigInteger fromBigByteArray(byte[] b) {
        return new BigInteger(1, b);
    }

    private static byte[] toBigByteArray(BigInteger n) {
        byte[] b = n.toByteArray();
        if (b[0] != 0) {
            return b;
        }
        int i = 1;
        while (b[i] == 0) {
            i++;
        }
        return Arrays.copyOfRange(b, i, b.length);
    }

    private static String padHexBinary(String hexString) {
        if (hexString.length() % 2 != 0) {
            return '0' + hexString;
        }
        return hexString;
    }

    private byte[] sha1(byte[] bytes) {
        try {
            return sha1Md.digest(bytes);
        } finally {
            sha1Md.reset();
        }
    }

    private byte[] sha1(byte[] bytes1, byte[] bytes2) {
        try {
            sha1Md.update(bytes1);
            return sha1Md.digest(bytes2);
        } finally {
            sha1Md.reset();
        }
    }

    private static byte[] pad(BigInteger n) {
        final byte[] bn = toBigByteArray(n);
        if (bn.length > SRP_KEY_SIZE) {
            return Arrays.copyOfRange(bn, bn.length - SRP_KEY_SIZE, bn.length);
        }
        return bn;
    }

    private BigInteger getScramble(BigInteger x, BigInteger y) {
        return fromBigByteArray(sha1(pad(x), pad(y)));
    }

    private static BigInteger getSecret() {
        return new BigInteger(SRP_KEY_SIZE, random);
    }

    static byte[] getSalt() {
        byte[] b = new byte[SRP_SALT_SIZE];
        random.nextBytes(b);
        return b;
    }

    private BigInteger getUserHash(String user, String password, byte[] salt) {
        final byte[] hash1;
        try {
            sha1Md.update(user.toUpperCase().getBytes(StandardCharsets.UTF_8));
            sha1Md.update(SEPARATOR_BYTE);
            hash1 = sha1Md.digest(password.getBytes(StandardCharsets.UTF_8));
        } finally {
            sha1Md.reset();
        }
        final byte[] hash2 = sha1(salt, hash1);
        return fromBigByteArray(hash2);
    }

    KeyPair serverSeed(String user, String password, byte[] salt) {
        final BigInteger v = g.modPow(getUserHash(user, password, salt), N);
        final BigInteger b = getSecret();
        final BigInteger gb = g.modPow(b, N);
        final BigInteger kv = k.multiply(v).mod(N);
        final BigInteger B = kv.add(gb).mod(N);
        return new KeyPair(B, b);
    }

    byte[] getServerSessionKey(String user, String password, byte[] salt, BigInteger A, BigInteger B,
            BigInteger b) {
        final BigInteger u = getScramble(A, B);
        final BigInteger v = g.modPow(getUserHash(user, password, salt), N);
        final BigInteger vu = v.modPow(u, N);
        final BigInteger Avu = A.multiply(vu).mod(N);
        final BigInteger sessionSecret = Avu.modPow(b, N);
        return sha1(toBigByteArray(sessionSecret));
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    private byte[] getClientSessionKey(String user, String password, byte[] salt, BigInteger serverPublicKey) {
        final BigInteger u = getScramble(publicKey, serverPublicKey);
        final BigInteger x = getUserHash(user, password, salt);
        final BigInteger gx = g.modPow(x, N);
        final BigInteger kgx = k.multiply(gx).mod(N);
        final BigInteger diff = serverPublicKey.subtract(kgx).mod(N);
        final BigInteger ux = u.multiply(x).mod(N);
        final BigInteger aux = privateKey.add(ux).mod(N);
        final BigInteger sessionSecret = diff.modPow(aux, N);
        return sha1(toBigByteArray(sessionSecret));
    }

    String getPublicKeyHex() {
        return ByteArrayHelper.toHexString(pad(publicKey));
    }

    byte[] clientProof(String user, String password, byte[] salt, BigInteger serverPublicKey) throws SQLException {
        final byte[] K = getClientSessionKey(user, password, salt, serverPublicKey);
        final BigInteger n1 = fromBigByteArray(sha1(toBigByteArray(N)));
        final BigInteger n2 = fromBigByteArray(sha1(toBigByteArray(g)));
        final byte[] M = clientProofHash(
                toBigByteArray(n1.modPow(n2, N)),
                sha1(user.toUpperCase().getBytes(StandardCharsets.UTF_8)),
                salt,
                toBigByteArray(publicKey),
                toBigByteArray(serverPublicKey),
                K);

        sessionKey = K;
        return M;
    }

    private byte[] clientProofHash(byte[]... ba) throws SQLException {
        try {
            MessageDigest md = MessageDigest.getInstance(clientProofHashAlgorithm);
            for (byte[] b : ba) {
                md.update(b);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_hashAlgorithmNotAvailable)
                    .messageParameter(clientProofHashAlgorithm)
                    .cause(e)
                    .toFlatSQLException();
        }
    }

    byte[] clientProof(String user, String password, byte[] authData) throws SQLException {
        if (authData == null || authData.length == 0) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_auth_data).toFlatSQLException();
        }
        if (authData.length > EXPECTED_AUTH_DATA_LENGTH) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_auth_datalength)
                    .messageParameter(authData.length)
                    .messageParameter(EXPECTED_AUTH_DATA_LENGTH)
                    .messageParameter("data")
                    .toFlatSQLException();
        }

        final int saltLength = VaxEncoding.iscVaxInteger2(authData, 0);
        if (saltLength > SRP_SALT_SIZE * 2) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_auth_datalength)
                    .messageParameter(saltLength)
                    .messageParameter(SRP_SALT_SIZE * 2)
                    .messageParameter("salt")
                    .toFlatSQLException();
        }
        final byte[] salt = Arrays.copyOfRange(authData, 2, saltLength + 2);

        final int keyLength = VaxEncoding.iscVaxInteger2(authData, saltLength + 2);
        final int serverKeyStart = saltLength + 4;
        if (authData.length - serverKeyStart != keyLength) {
            throw new FbExceptionBuilder().exception(ISCConstants.isc_auth_datalength)
                    .messageParameter(keyLength)
                    .messageParameter(authData.length - serverKeyStart)
                    .messageParameter("key")
                    .toFlatSQLException();
        }
        final String hexServerPublicKey = new String(authData, serverKeyStart, authData.length - serverKeyStart,
                StandardCharsets.US_ASCII);
        final BigInteger serverPublicKey = new BigInteger(padHexBinary(hexServerPublicKey), 16);

        return clientProof(user.toUpperCase(), password, salt, serverPublicKey);
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    static class KeyPair {
        private BigInteger pub, secret;

        private KeyPair(BigInteger pub, BigInteger secret) {
            this.pub = pub;
            this.secret = secret;
        }

        BigInteger getPublicKey() {
            return pub;
        }

        BigInteger getPrivateKey() {
            return secret;
        }
    }
    
}
