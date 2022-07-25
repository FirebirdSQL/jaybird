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

import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for support of the time and date literal escapes as defined in
 * section 13.4.2 of the JDBC 4.1 specification.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class TimeDateLiteralEscapesTest {

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

    /**
     * Test of the {d 'yyyy-mm-dd'} escape.
     */
    @Test
    void testDateEscape() throws Exception {
        try (ResultSet rs = stmt.executeQuery("SELECT {d '2012-12-22'} FROM RDB$DATABASE")) {
            assertTrue(rs.next(), "Expected one row");
            Object column1 = rs.getObject(1);
            assertThat("Expected result of {d escape} to be of type java.sql.Date",
                    column1, instanceOf(java.sql.Date.class));
            assertEquals("2012-12-22", column1.toString(), "Unexpected value for {d escape}");
        }
    }

    /**
     * Test of the {t 'hh:mm:ss'} escape
     */
    @Test
    void testTimeEscape() throws Exception {
        try (ResultSet rs = stmt.executeQuery("SELECT {t '15:05:56'} FROM RDB$DATABASE")) {
            assertTrue(rs.next(), "Expected one row");
            Object column1 = rs.getObject(1);
            assertThat("Expected result of {t escape} to be of type java.sql.Time",
                    column1, instanceOf(java.sql.Time.class));
            assertEquals("15:05:56", column1.toString(), "Unexpected value for {t escape}");
        }
    }

    /**
     * Test of the {ts 'yyyy-mm-dd hh:mm:ss'} escape (without fractional seconds)
     */
    @Test
    void testTimestampEscape() throws Exception {
        try (ResultSet rs = stmt.executeQuery("SELECT {ts '2012-12-22 15:05:56'} FROM RDB$DATABASE")) {
            assertTrue(rs.next(), "Expected one row");
            Object column1 = rs.getObject(1);
            assertThat("Expected result of {t escape} to be of type java.sql.Timestamp",
                    column1, instanceOf(java.sql.Timestamp.class));
            assertEquals("2012-12-22 15:05:56.0", column1.toString(), "Unexpected value for {ts escape}");
        }
    }

    /**
     * Test of the {ts 'yyyy-mm-dd hh:mm:ss.f..'} escape (with fractional seconds)
     */
    @Test
    void testTimestampEscapeMillisecond() throws Exception {
        try (ResultSet rs = stmt.executeQuery("SELECT {ts '2012-12-22 15:05:56.123'} FROM RDB$DATABASE")) {
            assertTrue(rs.next(), "Expected one row");
            Object column1 = rs.getObject(1);
            assertThat("Expected result of {t escape} to be of type java.sql.Timestamp",
                    column1, instanceOf(java.sql.Timestamp.class));
            assertEquals("2012-12-22 15:05:56.123", column1.toString(), "Unexpected value for {ts escape}");
        }
    }
}
