// SPDX-FileCopyrightText: Copyright 2017-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link FBRowId}.
 *
 * @author Mark Rotteveel
 */
class FBRowIdTest {

    @Test
    void constructFBRowId() {
        byte[] rowIdBytes = { 1, 2, 3, 4, 5, 6, 7, 8 };
        FBRowId fbRowId = new FBRowId(rowIdBytes);

        assertArrayEquals(rowIdBytes, fbRowId.getBytes());
    }

    @Test
    void constructFBRowIdWithNullArrayThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FBRowId(null));
    }

    @Test
    void toStringPrintsArrayAsHex() {
        byte[] rowIdBytes = { 0x0e, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00 };
        FBRowId fbRowId = new FBRowId(rowIdBytes);

        assertEquals("0E01000001000000", fbRowId.toString());
    }
}
