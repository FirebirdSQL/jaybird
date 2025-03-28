/*
 SPDX-FileCopyrightText: Copyright 1996 Aki Yoshida
 SPDX-FileCopyrightText: Copyright 2001 Iris Van den Broeke
 SPDX-FileCopyrightText: Copyright 2001 Daniel Deville
 SPDX-FileCopyrightText: Copyright 2004 Greg Wilkins
 SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
 SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
 SPDX-License-Identifier: UnixCrypt
*/
package org.firebirdsql.gds.ng.wire.auth.legacy;

/**
 * Implements the one way password hash used by the legacy authentication of Firebird.
 * <p>
 * NOTE: This class was modified to only be applicable specifically to the hash used by Firebird, it is not generally
 * usable.
 * </p>
 * <p>
 * Original class name: UnixCrypt
 * </p>
 *
 * @author Greg Wilkins (gregw)
 * @version UnixCrypt.java,v 1.5 2004/10/11 00:28:41 gregwilkins Exp
 */
@SuppressWarnings("java:S117")
public final class LegacyHash {

    //@formatter:off
    private static final byte[] Rotates = { // PC1 rotation schedule
        1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};

    private static final byte[] ITOA64 = {		/* 0..63 => ascii-64 */
        (byte)'.',(byte) '/',(byte) '0',(byte) '1',(byte) '2',(byte) '3',(byte) '4',(byte) '5',
        (byte)'6',(byte) '7',(byte) '8',(byte) '9',(byte) 'A',(byte) 'B',(byte) 'C',(byte) 'D',
        (byte)'E',(byte) 'F',(byte) 'G',(byte) 'H',(byte) 'I',(byte) 'J',(byte) 'K',(byte) 'L', 
        (byte)'M',(byte) 'N',(byte) 'O',(byte) 'P',(byte) 'Q',(byte) 'R',(byte) 'S',(byte) 'T', 
        (byte)'U',(byte) 'V',(byte) 'W',(byte) 'X',(byte) 'Y',(byte) 'Z',(byte) 'a',(byte) 'b', 
        (byte)'c',(byte) 'd',(byte) 'e',(byte) 'f',(byte) 'g',(byte) 'h',(byte) 'i',(byte) 'j', 
        (byte)'k',(byte) 'l',(byte) 'm',(byte) 'n',(byte) 'o',(byte) 'p',(byte) 'q',(byte) 'r', 
        (byte)'s',(byte) 't',(byte) 'u',(byte) 'v',(byte) 'w',(byte) 'x',(byte) 'y',(byte) 'z'};
    //@formatter:on

    /* =====  Tables that are initialized at run time  ==================== */

    /* Initial key schedule permutation */
    private static final long[][] PC1ROT = new long[16][16];

    /* Subsequent key schedule rotation permutations */
    private static final long[][][] PC2ROT = new long[2][16][16];

    /* Table that combines the S, P, and E operations.  */
    private static final long[][] SPE = new long[8][64];

    /* compressed/interleaved => final permutation table */
    private static final long[][] CF6464 = new long[16][16];

    /* ==================================== */

    // Salt derived frm the string 9z (Firebird specific)
    private static final int FB_SALT = 754712576;

    private static final int ITERATIONS = 25;

    static {
        byte[] perm = new byte[64];
        byte[] temp = new byte[64];

        //@formatter:off
        final byte[] PC1 = { // permuted choice table 1
            57, 49, 41, 33, 25, 17,  9,
             1, 58, 50, 42, 34, 26, 18,
            10,  2, 59, 51, 43, 35, 27,
            19, 11,  3, 60, 52, 44, 36,

            63, 55, 47, 39, 31, 23, 15,
             7, 62, 54, 46, 38, 30, 22,
            14,  6, 61, 53, 45, 37, 29,
            21, 13,  5, 28, 20, 12,  4};

        final byte[] PC2 = { // permuted choice table 2
             9, 18,    14, 17, 11, 24,  1,  5,
            22, 25,     3, 28, 15,  6, 21, 10,
            35, 38,    23, 19, 12,  4, 26,  8,
            43, 54,    16,  7, 27, 20, 13,  2,

             0,  0,    41, 52, 31, 37, 47, 55,
             0,  0,    30, 40, 51, 45, 33, 48,
             0,  0,    44, 49, 39, 56, 34, 53,
             0,  0,    46, 42, 50, 36, 29, 32};
        //@formatter:on

        // PC1ROT - bit reverse, then PC1, then Rotate, then PC2
        for (int i = 0; i < 64; i++) {
            int k;
            if ((k = PC2[i]) == 0) continue;
            if ((k % 28) < 1) k -= 28;
            k = PC1[k];
            k--;
            k = (k | 0x07) - (k & 0x07);
            k++;
            perm[i] = (byte) k;
        }
        initPerm(PC1ROT, perm);

        // PC2ROT - PC2 inverse, then Rotate, then PC2
        for (int j = 0; j < 2; j++) {
            int k;
            for (int i = 0; i < 64; i++) perm[i] = temp[i] = 0;
            for (int i = 0; i < 64; i++) {
                if ((k = PC2[i]) == 0) continue;
                temp[k - 1] = (byte) (i + 1);
            }
            for (int i = 0; i < 64; i++) {
                if ((k = PC2[i]) == 0) continue;
                k += j;
                if ((k % 28) <= j) k -= 28;
                perm[i] = temp[k];
            }

            initPerm(PC2ROT[j], perm);
        }

        //@formatter:off
        // (mostly) Standard DES Tables from Tom Truscott
        final byte[] IP = { // initial permutation
            58, 50, 42, 34, 26, 18, 10,  2,
            60, 52, 44, 36, 28, 20, 12,  4,
            62, 54, 46, 38, 30, 22, 14,  6,
            64, 56, 48, 40, 32, 24, 16,  8,
            57, 49, 41, 33, 25, 17,  9,  1,
            59, 51, 43, 35, 27, 19, 11,  3,
            61, 53, 45, 37, 29, 21, 13,  5,
            63, 55, 47, 39, 31, 23, 15,  7};

        // The final permutation is the inverse of IP - no table is necessary
        final byte[] ExpandTr = { // expansion operation
            32,  1,  2,  3,  4,  5,
             4,  5,  6,  7,  8,  9,
             8,  9, 10, 11, 12, 13,
            12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29,
            28, 29, 30, 31, 32,  1};
            //@formatter:on

        // Bit reverse, initial permutation, expansion
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int k = (j < 2) ? 0 : IP[ExpandTr[i * 6 + j - 2] - 1];
                if (k > 32) k -= 32;
                else if (k > 0) k--;
                if (k > 0) {
                    k--;
                    k = (k | 0x07) - (k & 0x07);
                    k++;
                }
                perm[i * 8 + j] = (byte) k;
            }
        }

        //@formatter:off
        final byte[] CIFP = { // compressed/interleaved permutation
             1,  2,  3,  4,   17, 18, 19, 20,
             5,  6,  7,  8,   21, 22, 23, 24,
             9, 10, 11, 12,   25, 26, 27, 28,
            13, 14, 15, 16,   29, 30, 31, 32,

            33, 34, 35, 36,   49, 50, 51, 52,
            37, 38, 39, 40,   53, 54, 55, 56,
            41, 42, 43, 44,   57, 58, 59, 60,
            45, 46, 47, 48,   61, 62, 63, 64};
        //@formatter:on

        // Compression, final permutation, bit reverse
        for (int i = 0; i < 64; i++) {
            int k = IP[CIFP[i] - 1];
            k--;
            k = (k | 0x07) - (k & 0x07);
            k++;
            perm[k - 1] = (byte) (i + 1);
        }

        initPerm(CF6464, perm);

        //@formatter:off
        final byte[][] S = { // 48->32 bit substitution tables
            // S[1]
            {14,  4, 13,  1,  2, 15, 11,  8,  3, 10,  6, 12,  5,  9,  0,  7,
              0, 15,  7,  4, 14,  2, 13,  1, 10,  6, 12, 11,  9,  5,  3,  8,
              4,  1, 14,  8, 13,  6,  2, 11, 15, 12,  9,  7,  3, 10,  5,  0,
             15, 12,  8,  2,  4,  9,  1,  7,  5, 11,  3, 14, 10,  0,  6, 13},
            // S[2]
            {15,  1,  8, 14,  6, 11,  3,  4,  9,  7,  2, 13, 12,  0,  5, 10,
              3, 13,  4,  7, 15,  2,  8, 14, 12,  0,  1, 10,  6,  9, 11,  5,
              0, 14,  7, 11, 10,  4, 13,  1,  5,  8, 12,  6,  9,  3,  2, 15,
             13,  8, 10,  1,  3, 15,  4,  2, 11,  6,  7, 12,  0,  5, 14,  9},
            // S[3]
            {10,  0,  9, 14,  6,  3, 15,  5,  1, 13, 12,  7, 11,  4,  2,  8,
             13,  7,  0,  9,  3,  4,  6, 10,  2,  8,  5, 14, 12, 11, 15,  1,
             13,  6,  4,  9,  8, 15,  3,  0, 11,  1,  2, 12,  5, 10, 14,  7,
              1, 10, 13,  0,  6,  9,  8,  7,  4, 15, 14,  3, 11,  5,  2, 12},
            // S[4]
            { 7, 13, 14,  3,  0,  6,  9, 10,  1,  2,  8,  5, 11, 12,  4, 15,
             13,  8, 11,  5,  6, 15,  0,  3,  4,  7,  2, 12,  1, 10, 14,  9,
             10,  6,  9,  0, 12, 11,  7, 13, 15,  1,  3, 14,  5,  2,  8,  4,
              3, 15,  0,  6, 10,  1, 13,  8,  9,  4,  5, 11, 12,  7,  2, 14},
            // S[5]
            { 2, 12,  4,  1,  7, 10, 11,  6,  8,  5,  3, 15, 13,  0, 14,  9,
             14, 11,  2, 12,  4,  7, 13,  1,  5,  0, 15, 10,  3,  9,  8,  6,
              4,  2,  1, 11, 10, 13,  7,  8, 15,  9, 12,  5,  6,  3,  0, 14,
             11,  8, 12,  7,  1, 14,  2, 13,  6, 15,  0,  9, 10,  4,  5,  3},
            // S[6]
            {12,  1, 10, 15,  9,  2,  6,  8,  0, 13,  3,  4, 14,  7,  5, 11,
             10, 15,  4,  2,  7, 12,  9,  5,  6,  1, 13, 14,  0, 11,  3,  8,
              9, 14, 15,  5,  2,  8, 12,  3,  7,  0,  4, 10,  1, 13, 11,  6,
              4,  3,  2, 12,  9,  5, 15, 10, 11, 14,  1,  7,  6,  0,  8, 13},
            // S[7]
            { 4, 11,  2, 14, 15,  0,  8, 13,  3, 12,  9,  7,  5, 10,  6,  1,
             13,  0, 11,  7,  4,  9,  1, 10, 14,  3,  5, 12,  2, 15,  8,  6,
              1,  4, 11, 13, 12,  3,  7, 14, 10, 15,  6,  8,  0,  5,  9,  2,
              6, 11, 13,  8,  1,  4, 10,  7,  9,  5,  0, 15, 14,  2,  3, 12},
            // S[8]
            {13,  2,  8,  4,  6, 15, 11,  1, 10,  9,  3, 14,  5,  0, 12,  7,
              1, 15, 13,  8, 10,  3,  7,  4, 12,  5,  6, 11,  0, 14,  9,  2,
              7, 11,  4,  1,  9, 12, 14,  2,  0,  6, 10, 13, 15,  3,  5,  8,
              2,  1, 14,  7,  4, 10,  8, 13, 15, 12,  9,  0,  3,  5,  6, 11}};

        final byte[] P32Tr = { // 32-bit permutation function
            16,  7, 20, 21,
            29, 12, 28, 17,
             1, 15, 23, 26,
             5, 18, 31, 10,
             2,  8, 24, 14,
            32, 27,  3,  9,
            19, 13, 30,  6,
            22, 11,  4, 25};
        //@formatter:on

        // SPE table
        for (int i = 0; i < 48; i++)
            perm[i] = P32Tr[ExpandTr[i] - 1];
        for (int t = 0; t < 8; t++) {
            for (int j = 0; j < 64; j++) {
                int k = (j & 0x01) << 5 | (j >> 1 & 0x01) << 3 |
                        (j >> 2 & 0x01) << 2 | (j >> 3 & 0x01) << 1 |
                        (j >> 4 & 0x01) | (j >> 5 & 0x01) << 4;
                k = S[t][k];
                k = (k >> 3 & 0x01) | (k >> 2 & 0x01) << 1 |
                    (k >> 1 & 0x01) << 2 | (k & 0x01) << 3;
                for (int i = 0; i < 32; i++) temp[i] = 0;
                for (int i = 0; i < 4; i++) temp[4 * t + i] = (byte) ((k >> i) & 0x01);
                long kk = 0;
                for (int i = 24; --i >= 0; )
                    kk = kk << 1 | (long) temp[perm[i] - 1] << 32 | temp[perm[i + 24] - 1];

                SPE[t][j] = toSixBit(kk);
            }
        }
    }

    private LegacyHash() {
        // no instances
    }

    /**
     * Returns the transposed and split code of two 24-bit code into two 4-byte code, each having 6 bits.
     */
    private static long toSixBit(long num) {
        return num << 26 & 0xfc000000fc000000L | num << 12 & 0xfc000000fc0000L |
               num >> 2 & 0xfc000000fc00L | num >> 16 & 0xfc000000fcL;
    }

    /**
     * Returns the permutation of the given 64-bit code with the specified permutation table.
     */
    private static long perm6464(long c, long[][] p) {
        long out = 0L;
        for (int i = 8; --i >= 0; ) {
            int t = (int) (c & 0xff);
            c >>= 8;
            out |= p[i << 1][t & 0x0f];
            out |= p[(i << 1) + 1][t >> 4];
        }
        return out;
    }

    /**
     * Returns the key schedule for the given key.
     */
    private static long[] desSetKey(long keyword) {
        long K = perm6464(keyword, PC1ROT);
        long[] KS = new long[16];
        KS[0] = K & ~0x0303030300000000L;

        for (int i = 1; i < 16; i++) {
            KS[i] = K;
            K = perm6464(K, PC2ROT[Rotates[i] - 1]);

            KS[i] = K & ~0x0303030300000000L;
        }
        return KS;
    }

    /**
     * Returns the DES encrypted code of the given word with the specified environment.
     */
    private static long desCipher(long[] KS) {
        long L = 0;
        long R = 0;
        for (int iter = 0; iter < ITERATIONS; iter++) {
            for (int loopCount = 0; loopCount < 8; loopCount++) {
                long kp = KS[loopCount << 1];
                L ^= opSPE(opSALT(R) ^ R ^ kp);

                kp = KS[(loopCount << 1) + 1];
                R ^= opSPE(opSALT(L) ^ L ^ kp);
            }
            // swap L and R
            L ^= R;
            R ^= L;
            L ^= R;
        }
        L = (L >> 35 & 0x0f0f0f0fL | L << 1 & 0xf0f0f0f0L) << 32 |
            R >> 35 & 0x0f0f0f0fL | R << 1 & 0xf0f0f0f0L;

        L = perm6464(L, CF6464);

        return L;
    }

    // NOTE: name is essentially meaningless, just named so because it uses FB_SALT
    private static long opSALT(long R) {
        long k = ((R >> 32) ^ R) & FB_SALT;
        k |= k << 32;
        return k;
    }

    // NOTE: name is essentially meaningless, just named so because it uses SPE
    private static long opSPE(long B) {
        return SPE[0][(int) (B >> 58 & 0x3f)] ^ SPE[1][(int) (B >> 50 & 0x3f)] ^
                SPE[2][(int) (B >> 42 & 0x3f)] ^ SPE[3][(int) (B >> 34 & 0x3f)] ^
                SPE[4][(int) (B >> 26 & 0x3f)] ^ SPE[5][(int) (B >> 18 & 0x3f)] ^
                SPE[6][(int) (B >> 10 & 0x3f)] ^ SPE[7][(int) (B >> 2 & 0x3f)];
    }

    /**
     * Initializes the given permutation table with the mapping table.
     */
    private static void initPerm(long[][] perm, byte[] p) {
        for (int k = 0; k < 8 * 8; k++) {
            int l = p[k] - 1;
            if (l < 0) continue;
            int i = l >> 2;
            l = 1 << (l & 0x03);
            for (int j = 0; j < 16; j++) {
                int s = (k & 0x07) + (7 - (k >> 3) << 3);
                if ((j & l) != 0x00) perm[i][j] |= 1L << s;
            }
        }
    }

    /**
     * Encrypts String into crypt (Unix) code as used by Firebird.
     *
     * @param key
     *         the key to be encrypted
     * @return the encrypted String
     */
    public static byte[] fbCrypt(final String key) {
        if (key == null) {
            return new byte[] { '*' }; // will NOT match under ANY circumstances!
        }

        final int keyLen = key.length();
        long keyword = 0L;
        for (int i = 0; i < 8; i++) {
            keyword = keyword << 8 | ((i < keyLen) ? 2 * key.charAt(i) : 0);
        }

        long rsltblock = desCipher(desSetKey(keyword));

        final byte[] cryptResult = new byte[11];
        cryptResult[10] = ITOA64[(((int) rsltblock) << 2) & 0x3f];
        rsltblock >>= 4;
        for (int i = 10; --i >= 0; ) {
            cryptResult[i] = ITOA64[((int) rsltblock) & 0x3f];
            rsltblock >>= 6;
        }

        return cryptResult;
    }

}

