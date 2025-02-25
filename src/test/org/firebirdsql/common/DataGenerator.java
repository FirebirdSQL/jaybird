// SPDX-FileCopyrightText: Copyright 2014-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper class for generating (random) test data.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class DataGenerator {

    private DataGenerator() {
        // No instance
    }

    /**
     * Creates a byte array with random bytes with the specified length.
     *
     * @param length
     *         Requested length
     * @return Byte array of length filled with random bytes
     */
    public static byte[] createRandomBytes(int length) {
        byte[] randomBytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Creates a byte array with random bytes in the range 0 - 127 with the specified length.
     *
     * @param length
     *         Requested length
     * @return Byte array of length filled with random bytes in the range 0 - 127
     */
    public static byte[] createRandomAsciiBytes(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) random.nextInt(128);
        }
        return bytes;
    }

    /**
     * Generates a random number between {@code lowerBound} (inclusive) and {@code upperBound} (exclusive).
     *
     * @return Generated number.
     */
    public static int generateRandom(int lowerBound, int upperBound) {
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("lowerBound must be smaller than upperBound");
        }
        return ThreadLocalRandom.current().nextInt(upperBound - lowerBound) + lowerBound;
    }
}
