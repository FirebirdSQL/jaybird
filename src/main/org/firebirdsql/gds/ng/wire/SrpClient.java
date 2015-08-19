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

package org.firebirdsql.gds.ng.wire;
import java.util.Arrays;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

/**
 * @author <a href="mailto:nakagami@gmail.com">Hajime Nakagami</a>
 */



public final class SrpClient {
    private static final int SRP_KEY_SIZE = 128;
    private static final int SRP_SALT_SIZE = 32;
    private static final BigInteger N = new BigInteger("E67D2E994B2F900C3F41F08F5BB2627ED0D49EE1FE767A52EFCD565CD6E768812C3E1E9CE8F0A8BEA6CB13CD29DDEBF7A96D4A93B55D488DF099A15C89DCB0640738EB2CBDD9A8F7BAB561AB1B0DC1C6CDABF303264A08D1BCA932D1F1EE428B619D970F342ABA9A65793B8B2F041AE5364350C16F735F56ECBCA87BD57B29E7", 16);
    private static final BigInteger g = new BigInteger("2");

    private static final BigInteger k = new BigInteger("1277432915985975349439481660349303019122249719989");

    private BigInteger publicKey;   /* A */
    private BigInteger privateKey;  /* a */
    private byte[] sessionKey;      /* K */


    private static class KeyPair {
        private BigInteger pub, secret;
        KeyPair(BigInteger pub, BigInteger secret) {
            this.pub = pub;
            this.secret = secret;
        }
        private BigInteger getPublicKey() {
            return pub;
        }
        private BigInteger getPrivateKey() {
            return secret;
        }
    }

    private static BigInteger fromBigByteArray(byte[] b) {
        return new BigInteger(DatatypeConverter.printHexBinary(b), 16);
    }

    private static byte[] toBigByteArray(BigInteger n) {
        byte[] b = n.toByteArray();
        int i = 0;
        while (b[i] == 0)
            i++;
        return Arrays.copyOfRange(b, i, b.length);
    }

    private static byte[] parseHexBinary(String hexString) {

        if (hexString.length() % 2 != 0) {
            hexString = '0' + hexString;
        }

        return DatatypeConverter.parseHexBinary(hexString);
    }

    private static byte[] sha1(byte[]... ba) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (byte[] b : ba) {
                md.update(b);
            }
            return md.digest();
        }
        catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] pad(BigInteger n) {
        final byte[] bn = n.toByteArray();
        final int start = bn.length > SRP_KEY_SIZE ? bn.length - SRP_KEY_SIZE : 0;
        return Arrays.copyOfRange(bn, start, SRP_KEY_SIZE+start);
    }

    private static BigInteger getScramble(BigInteger x, BigInteger y) {
        return fromBigByteArray(sha1(pad(x), pad(y)));
    }

    private static BigInteger getSecret() {
        byte[] b = new byte[SRP_KEY_SIZE/8];
        SecureRandom random = new SecureRandom();
        random.nextBytes(b);
        return fromBigByteArray(b);
    }

    private static byte[] getSalt() {
        byte[] b = new byte[SRP_SALT_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(b);
        return b;
    }


    private static BigInteger getUserHash(String user, String password, byte[] salt) {
        final byte[] hash1 = sha1(user.getBytes(), ":".getBytes(), password.getBytes());
        final byte[] hash2 = sha1(salt, hash1);
        return fromBigByteArray(hash2);
    }

    private static KeyPair serverSeed(String user, String password, byte[] salt) {
        final BigInteger v = g.modPow(getUserHash(user, password, salt), N);
        final BigInteger b = getSecret();
        final BigInteger gb = g.modPow(b, N);
        final BigInteger kv = k.multiply(v).mod(N);
        final BigInteger B = kv.add(gb).mod(N);
        return new KeyPair(B, b);
    }

    private static byte[] getServerSessionKey(String user, String password, byte[] salt, BigInteger A, BigInteger B, BigInteger b) {
        final BigInteger u = getScramble(A, B);
        final BigInteger v = g.modPow(getUserHash(user, password, salt), N);
        final BigInteger vu = v.modPow(u, N);
        final BigInteger Avu = A.multiply(vu).mod(N);
        final BigInteger sessionSecret = Avu.modPow(b, N);
        return sha1(toBigByteArray(sessionSecret));
    }

    public SrpClient() {
        privateKey = getSecret();
        publicKey = g.modPow(privateKey, N);
    }
    private BigInteger getPublicKey() {
        return publicKey;
    }
    private BigInteger getPrivateKey() {
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

    public String getPublicKeyHex() {
        return DatatypeConverter.printHexBinary(pad(publicKey));
    }

    public byte[] clientProof(String user, String password, byte[] salt, BigInteger serverPublicKey) {
        final byte[] K = getClientSessionKey(user, password, salt, serverPublicKey);
        final BigInteger n1 = fromBigByteArray(sha1(toBigByteArray(N)));
        final BigInteger n2 = fromBigByteArray(sha1(toBigByteArray(g)));
        final byte[] M = sha1(toBigByteArray(n1.modPow(n2, N)), toBigByteArray(fromBigByteArray(sha1(user.getBytes()))), salt, toBigByteArray(publicKey), toBigByteArray(serverPublicKey), K);


        sessionKey = K;
        return M;
    }

    public byte[] clientProof(String user, String password, byte[] authData) {
        int length = (int)authData[0] + (int)authData[1]*256;

        byte[] salt = Arrays.copyOfRange(authData, 2, length +2);

        final String hexServerPublicKey = new String(Arrays.copyOfRange(authData, length+4, authData.length));
        final BigInteger serverPublicKey = fromBigByteArray(parseHexBinary(hexServerPublicKey));

        return clientProof(user.toUpperCase(), password, salt, serverPublicKey);
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public static void main(String[] arg)
    {
        String user = "SYSDBA";
        String password = "masterkey";

        SrpClient srp = new SrpClient();
        byte[] salt = getSalt();
        KeyPair server_key_pair = serverSeed(user, password, salt);

        byte[] serverKey = getServerSessionKey(user, password, salt, srp.getPublicKey(), server_key_pair.getPublicKey(), server_key_pair.getPrivateKey());

        byte[] proof = srp.clientProof(user, password, salt, server_key_pair.getPublicKey());

        byte[] sessionKey = srp.getSessionKey();

        DatatypeConverter.printHexBinary(sessionKey).equals(DatatypeConverter.printHexBinary(serverKey));
    }

}
