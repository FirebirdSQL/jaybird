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

public class TestCharacterDecoding {

    @Test
    public void testOriginal() {
        IEncodingFactory factory = EncodingFactory.getPlatformDefault();
        Encoding encoding = factory.getEncodingForFirebirdName("ISO8859_1");
        String testStr = encoding.decodeFromCharset(new byte[] { 0x61, 0x62, 0x63 });
        assertEquals("Strings should be equal", "\u0061\u0062\u0063", testStr);
    }

}
