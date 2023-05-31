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
package org.firebirdsql.jaybird.xca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FBXidTest {

    @Test
    void testToBytes() throws Exception {
        FBXid xid = new FBXid(123L, 23, new byte[] { 1, 2, 3, 4 }, new byte[] { 5, 6, 7, 8, 9 });

        byte[] xidBytes = xid.toBytes();
        assertArrayEquals(new byte[] { 1, 5, 0, 0, 0, 23, 6, 0, 0, 0, 4, 1, 2, 3, 4, 4, 0, 0, 0, 5, 5, 6, 7, 8, 9 },
                xidBytes, "unexpected result for xid.toBytes");

        FBXid newXid = new FBXid(xidBytes, 123L);

        assertEquals(xid, newXid, "expected identical xids after deserialization");
    }

}
