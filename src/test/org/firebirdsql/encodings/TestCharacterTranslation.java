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
package org.firebirdsql.encodings;

import org.junit.Test;

import static org.junit.Assert.*;

// TODO Needs a better name as it no longer tests character translation
public class TestCharacterTranslation {

    private static final IEncodingFactory factory = EncodingFactory.getPlatformDefault();

    private byte[] testBytes = new byte[] {
            0x61, 0x62, 0x63
    };

    private char[] originalChars = new char[] {
            '\u0061', '\u0062', '\u0063'
    };

    @Test
    public void testOriginal() {
        Encoding encoding = factory.getEncodingForFirebirdName("ISO8859_1");

        String testStr = encoding.decodeFromCharset(testBytes);
        String checkStr = new String(originalChars);

        assertTrue("Strings should be equal", testStr.equals(checkStr));
    }

}
