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
package org.firebirdsql.gds.ng.dbcrypt.simple;

import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mark Rotteveel
 */
class StaticValueDbCryptCallbackTest {

    @Test
    void returnsReplyWithFixedResponseValue_nonNull() {
        final byte[] responseData = { 2, 3, 4, 5, 6 };
        DbCryptCallback callback = new StaticValueDbCryptCallback(responseData);

        DbCryptData dbCryptData = callback.handleCallback(DbCryptData.EMPTY_DATA);
        assertSame(responseData, dbCryptData.getPluginData(), "pluginData");
        assertEquals(0, dbCryptData.getReplySize(), "replySize");
    }

    @Test
    void returnsReplyWithFixedResponseValue_null() {
        DbCryptCallback callback = new StaticValueDbCryptCallback(null);

        DbCryptData dbCryptData = callback.handleCallback(DbCryptData.EMPTY_DATA);
        assertNull(dbCryptData.getPluginData(), "pluginData");
        assertEquals(0, dbCryptData.getReplySize(), "replySize");
    }

}
