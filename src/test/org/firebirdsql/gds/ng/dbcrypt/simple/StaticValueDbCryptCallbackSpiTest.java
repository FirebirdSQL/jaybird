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

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StaticValueDbCryptCallbackSpiTest {

    @Test
    void fixedResponseWithNullConfig() {
        final DbCryptCallback dbCryptCallback = new StaticValueDbCryptCallbackSpi()
                .createDbCryptCallback(null);

        DbCryptData dbCryptData = dbCryptCallback.handleCallback(DbCryptData.EMPTY_DATA);

        assertNull(dbCryptData.getPluginData());
    }

    @Test
    void fixedResponseWithBase64Config() {
        final DbCryptCallback dbCryptCallback = new StaticValueDbCryptCallbackSpi()
                .createDbCryptCallback("base64:ZWFzdXJlLg==");

        DbCryptData dbCryptData = dbCryptCallback.handleCallback(DbCryptData.EMPTY_DATA);

        assertArrayEquals("easure.".getBytes(StandardCharsets.US_ASCII), dbCryptData.getPluginData());
    }

    @Test
    void fixedResponseWithBase64urlConfig() {
        final DbCryptCallback dbCryptCallback = new StaticValueDbCryptCallbackSpi()
                .createDbCryptCallback("base64url:PDw_Pz8-Pg==");

        DbCryptData dbCryptData = dbCryptCallback.handleCallback(DbCryptData.EMPTY_DATA);

        assertArrayEquals("<<???>>".getBytes(StandardCharsets.US_ASCII), dbCryptData.getPluginData());
    }

    @Test
    void fixedResponseWithStringConfig() {
        final String dbCryptConfig = "abc\u02a5\u0b2c\u1d38\u213b";
        final DbCryptCallback dbCryptCallback = new StaticValueDbCryptCallbackSpi()
                .createDbCryptCallback(dbCryptConfig);

        DbCryptData dbCryptData = dbCryptCallback.handleCallback(DbCryptData.EMPTY_DATA);

        assertArrayEquals(dbCryptConfig.getBytes(StandardCharsets.UTF_8), dbCryptData.getPluginData());
    }
}
