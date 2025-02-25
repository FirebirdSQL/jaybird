// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
