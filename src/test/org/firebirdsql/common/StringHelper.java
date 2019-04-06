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
package org.firebirdsql.common;

import java.math.BigInteger;

/**
 * Helper class for converting (test) output to string.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class StringHelper {

    private StringHelper() {
    }
    
    /**
     * Converts a byte array to hexadecimal output.
     * 
     * @param bytes Byte array
     * @return hexadecimal form of bytes
     */
    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    /**
     * Null-safe trim.
     *
     * @param stringToTrim String to trim
     * @return result of {@code stringToTrim.trim()} (or {@code null} if {@code stringToTrim} was null
     */
    public static String trim(String stringToTrim) {
        return stringToTrim == null ? null : stringToTrim.trim();
    }
}
