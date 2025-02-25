// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
