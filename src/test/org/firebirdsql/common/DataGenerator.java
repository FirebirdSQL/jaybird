/*
 * $Id$
 *
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
package org.firebirdsql.common;

import java.util.Random;

/**
 * Helper class for generating (random) test data.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class DataGenerator {

    private static final Random rnd = new Random();

    private DataGenerator() {
        // No instance
    }

    /**
     * Creates a byte array with random bytes with the specified length.
     *
     * @param length Requested length
     * @return Byte array of length filled with random bytes
     */
    public static byte[] createRandomBytes(int length) {
        byte[] randomBytes = new byte[length];
        rnd.nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Generates a random number between <code>lowerBound</code> (inclusive) and <code>upperBound</code> (exclusive).
     *
     * @return Generated number.
     */
    public static int generateRandom(int lowerBound, int upperBound) {
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("lowerBound must be smaller than upperBound");
        }
        return rnd.nextInt(upperBound - lowerBound) + lowerBound;
    }
}
