// SPDX-FileCopyrightText: Copyright 2016-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.management.FBManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for database metadata in dialect 1.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataDialect1Test {

    private FBManager fbManager = null;

    @BeforeEach
    void setUp() throws Exception {
        fbManager = createFBManager();
        fbManager.setDialect(1);
        defaultDatabaseSetUp(fbManager);
    }

    @AfterEach
    void tearDown() throws Exception {
        defaultDatabaseTearDown(fbManager);
        fbManager = null;
    }

    @Test
    void testLargeNumeric() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("sqlDialect", "1");
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            DdlHelper.executeCreateTable(connection, "CREATE TABLE x (col NUMERIC(15,2))");
            final DatabaseMetaData md = connection.getMetaData();
            final ResultSet columns = md.getColumns(null, null, "X", "COL");
            assertTrue(columns.next());
            assertEquals("NUMERIC", columns.getString("TYPE_NAME"));
            assertEquals(Types.NUMERIC, columns.getInt("DATA_TYPE"));
            assertEquals(2, columns.getInt("DECIMAL_DIGITS"));
        }
    }

    /**
     * @see FBDatabaseMetaDataTest#testGetIdentifierQuoteString_dialect3Db(String, String)
     */
    @ParameterizedTest
    @CsvSource({
            "1, ' '",
            "2, \"",
            "3, \""
    })
    void testGetIdentifierQuoteString_dialect1Db(String connectionDialect, String expectedIdentifierQuote)
            throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("sqlDialect", connectionDialect);
        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            DatabaseMetaData md = connection.getMetaData();
            assertEquals(expectedIdentifierQuote, md.getIdentifierQuoteString());
        }
    }

}
