// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
