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
package org.firebirdsql.gds.ng.dbcrypt;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Tests for {@link DbCryptData}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class DbCryptDataTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void pluginDataNullAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, 0);

        assertNull(dbCryptData.getPluginData());
    }

    @Test
    public void pluginData32767BytesAllowed() {
        final byte[] pluginData = new byte[32767];
        DbCryptData dbCryptData = new DbCryptData(pluginData, 0);

        assertSame(pluginData, dbCryptData.getPluginData());
    }

    @Test
    public void pluginData32768Bytes_throwsIllegalArgumentException() {
        final byte[] pluginData = new byte[32768];
        expectedException.expect(IllegalArgumentException.class);

        new DbCryptData(pluginData, 0);
    }

    @Test
    public void replySizeNegativeAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, -1);

        assertEquals(-1, dbCryptData.getReplySize());
    }

    @Test
    public void replySizePositiveAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, 64);

        assertEquals(64, dbCryptData.getReplySize());
    }

    /**
     * We don't constrain the reply size value (even if it doesn't make sense to allow values bigger than 32767).
     */
    @Test
    public void replySizeIntegerMaxAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, dbCryptData.getReplySize());
    }

    /**
     * We don't constrain the reply size value.
     */
    @Test
    public void replySizeIntegerMinAllowed() {
        DbCryptData dbCryptData = new DbCryptData(null, Integer.MIN_VALUE);

        assertEquals(Integer.MIN_VALUE, dbCryptData.getReplySize());
    }

    @Test
    public void createReplyCreatesInstanceWithSuppliedData() {
        final byte[] pluginData = { 1, 2, 3, 4, 5 };
        DbCryptData dbCryptData = DbCryptData.createReply(pluginData);

        assertEquals("Expected zero reply size", 0, dbCryptData.getReplySize());
        assertSame("Expected same plugin data", pluginData, dbCryptData.getPluginData());
    }

    @Test
    public void createReplyWithNullDataReturnsEMPTY_DATA() {
        DbCryptData dbCryptData = DbCryptData.createReply(null);

        assertSame(DbCryptData.EMPTY_DATA, dbCryptData);
        assertEquals("Expected zero reply size", 0, dbCryptData.getReplySize());
        assertNull("Expected null plugin data", dbCryptData.getPluginData());
    }

}
