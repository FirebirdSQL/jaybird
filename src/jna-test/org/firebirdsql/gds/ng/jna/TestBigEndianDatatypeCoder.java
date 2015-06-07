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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.encodings.EncodingFactory;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test numeric conversion in big endian
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestBigEndianDatatypeCoder {

    private final BigEndianDatatypeCoder datatypeCoder =
            new BigEndianDatatypeCoder(EncodingFactory.getDefaultInstance());

    @Test
    public void encodeShort() {
        short testValue = 0b0110_1011_1010_1001;
        byte[] result = datatypeCoder.encodeShort(testValue);

        assertArrayEquals(new byte[] { 0b0110_1011, (byte) 0b1010_1001 }, result);
    }

    @Test
    public void decodeShort() {
        byte[] testValue = { 0b0110_1001, 0b0011_1100 };
        short result = datatypeCoder.decodeShort(testValue);

        assertEquals(0b0110_1001_0011_1100, result);
    }
}
