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
package org.firebirdsql.common;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper class for generating (random) test data.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
