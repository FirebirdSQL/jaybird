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
package org.firebirdsql.gds.ng.dbcrypt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DbCryptData}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class DbCryptDataTest {

    @Test
    void pluginDataNullAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, 0);

        assertNull(dbCryptData.getPluginData());
    }

    @Test
    void pluginData32767BytesAllowed() {
        final byte[] pluginData = new byte[32767];
        DbCryptData dbCryptData = new DbCryptData(pluginData, 0);

        assertSame(pluginData, dbCryptData.getPluginData());
    }

    @Test
    void pluginData32768Bytes_throwsIllegalArgumentException() {
        final byte[] pluginData = new byte[32768];

        assertThrows(IllegalArgumentException.class, () -> new DbCryptData(pluginData, 0));
    }

    @Test
    void replySizeNegativeAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, -1);

        assertEquals(-1, dbCryptData.getReplySize());
    }

    @Test
    void replySizePositiveAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, 64);

        assertEquals(64, dbCryptData.getReplySize());
    }

    /**
     * We don't constrain the reply size value (even if it doesn't make sense to allow values bigger than 32767).
     */
    @Test
    void replySizeIntegerMaxAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, dbCryptData.getReplySize());
    }

    /**
     * We don't constrain the reply size value.
     */
    @Test
    void replySizeIntegerMinAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, Integer.MIN_VALUE);

        assertEquals(Integer.MIN_VALUE, dbCryptData.getReplySize());
    }

    @Test
    void createReplyCreatesInstanceWithSuppliedData() {
        final byte[] pluginData = { 1, 2, 3, 4, 5 };
        DbCryptData dbCryptData = DbCryptData.createReply(pluginData);

        assertEquals(0, dbCryptData.getReplySize(), "Expected zero reply size");
        assertSame(pluginData, dbCryptData.getPluginData(), "Expected same plugin data");
    }

    @Test
    void createReplyWithNullDataReturnsEMPTY_DATA() {
        DbCryptData dbCryptData = DbCryptData.createReply(null);

        assertSame(DbCryptData.EMPTY_DATA, dbCryptData);
        assertEquals(0, dbCryptData.getReplySize(), "Expected zero reply size");
        assertNull(dbCryptData.getPluginData(), "Expected null plugin data");
    }

}
