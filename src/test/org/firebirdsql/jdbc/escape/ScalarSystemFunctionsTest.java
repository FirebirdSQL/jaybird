// SPDX-FileCopyrightText: Copyright 2012-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Locale;

import static org.firebirdsql.common.FBTestProperties.DB_NAME;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for support of the scalar system function escapes as defined in
 * appendix D.4 of the JDBC 4.1 specification.
 *
 * @author Mark Rotteveel
 */
class ScalarSystemFunctionsTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static Connection con;
    private static Statement stmt;

    @BeforeAll
    static void setupAll() throws Exception {
        con = FBTestProperties.getConnectionViaDriverManager();
        stmt = con.createStatement();
    }

    @AfterAll
    static void tearDownAll() {
        try {
            closeQuietly(stmt, con);
        } finally {
            stmt = null;
            con = null;
        }
    }

    @Test
    void testDatabase() throws Exception {
        ResultSet rs = stmt.executeQuery("SELECT {fn DATABASE()} FROM RDB$DATABASE");
        assertTrue(rs.next(), "Expected at least one row");
        assertThat("Unexpected result for function escape DATABASE()",
                rs.getString(1), anyOf(endsWith(DB_NAME.toUpperCase(Locale.ROOT)), endsWith(DB_NAME)));
    }

    @Test
    void testIfNull() throws Exception {
        ResultSet rs = stmt.executeQuery("SELECT {fn IFNULL(NULL, 'abcd')} FROM RDB$DATABASE");
        assertTrue(rs.next(), "Expected at least one row");
        assertEquals("abcd", rs.getString(1), "Unexpected result for function escape IFNULL(NULL, 'abcd')");
    }

    @Test
    void testUser() throws Exception {
        ResultSet rs = stmt.executeQuery("SELECT {fn USER()} FROM RDB$DATABASE");
        assertTrue(rs.next(), "Expected at least one row");
        assertEquals(FBTestProperties.DB_USER.toUpperCase(Locale.ROOT), rs.getString(1),
                "Unexpected result for function escape USER()");
    }

}
