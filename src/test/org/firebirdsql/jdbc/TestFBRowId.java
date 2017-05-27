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
package org.firebirdsql.jdbc;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link FBRowId}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBRowId {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void constructFBRowId() {
        byte[] rowIdBytes = { 1, 2, 3, 4, 5, 6, 7, 8 };
        FBRowId fbRowId = new FBRowId(rowIdBytes);

        assertArrayEquals(rowIdBytes, fbRowId.getBytes());
    }

    @Test
    public void constructFBRowIdWithNullArrayThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);

        new FBRowId(null);
    }

    @Test
    public void toStringPrintsArrayAsHex() throws Exception {
        byte[] rowIdBytes = { 0x0e, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00 };
        FBRowId fbRowId = new FBRowId(rowIdBytes);

        assertEquals("0E01000001000000", fbRowId.toString());
    }
}
