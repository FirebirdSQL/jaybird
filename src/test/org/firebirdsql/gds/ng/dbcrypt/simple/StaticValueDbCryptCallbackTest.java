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
package org.firebirdsql.gds.ng.dbcrypt.simple;

import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class StaticValueDbCryptCallbackTest {

    @Test
    public void returnsReplyWithFixedResponseValue_nonNull() {
        final byte[] responseData = { 2, 3, 4, 5, 6 };
        DbCryptCallback callback = new StaticValueDbCryptCallback(responseData);

        DbCryptData dbCryptData = callback.handleCallback(DbCryptData.EMPTY_DATA);
        assertSame("pluginData", responseData, dbCryptData.getPluginData());
        assertEquals("replySize", 0, dbCryptData.getReplySize());
    }

    @Test
    public void returnsReplyWithFixedResponseValue_null() {
        DbCryptCallback callback = new StaticValueDbCryptCallback(null);

        DbCryptData dbCryptData = callback.handleCallback(DbCryptData.EMPTY_DATA);
        assertNull("pluginData", dbCryptData.getPluginData());
        assertEquals("replySize", 0, dbCryptData.getReplySize());
    }

}
