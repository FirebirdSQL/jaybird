/*
 * $Id$
 *
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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.ISCConstants;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.sqlExceptionEqualTo;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.sqlState;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;

/**
 * Test suite for the FBDriver class implementation.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDriver extends FBJUnit4TestBase {

    private Connection connection;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @After
    public void tearDown() throws Exception {
        closeQuietly(connection);
    }

    @Test
    public void testAcceptsURL() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());

        assertTrue(driver.acceptsURL(getUrl()));
    }

    @Test
    public void testConnect() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());
        connection = driver.connect(getUrl(), getDefaultPropertiesForConnection());

        assertNotNull("Connection is null", connection);
    }

    @Test
    public void testJdbcCompliant() throws Exception {
        Driver driver = DriverManager.getDriver(getUrl());

        // current driver is not JDBC compliant.
        assertTrue(driver.jdbcCompliant());
    }

    /**
     * This method tests if driver correctly handles warnings returned from
     * database. We use SQL dialect mismatch between client and server to
     * make server return us a warning.
     */
    @Test
    public void testWarnings() throws Exception {
        Properties info = (Properties) getDefaultPropertiesForConnection().clone();
        info.setProperty("set_db_sql_dialect", "1");

        // open connection and convert DB to SQL dialect 1
        connection = DriverManager.getConnection(getUrl(), info);
        SQLWarning warning = connection.getWarnings();

        assertNotNull("Connection should have at least one warning.", warning);
        assertThat(warning, allOf(
                isA(SQLWarning.class),
                sqlExceptionEqualTo(ISCConstants.isc_dialect_reset_warning))
        );

        connection.clearWarnings();

        assertNull("After clearing no warnings should be present.", connection.getWarnings());
    }

    @Test
    public void testDialect1() throws Exception {
        Properties info = (Properties) getDefaultPropertiesForConnection().clone();
        info.setProperty("isc_dpb_sql_dialect", "1");

        connection = DriverManager.getConnection(getUrl(), info);
        Statement stmt = connection.createStatement();
        try {
            // Dialect 1 allows double quotes in strings
            ResultSet rs = stmt.executeQuery("SELECT  cast(\"today\" as date) - 7 FROM rdb$database");

            assertTrue("Should have at least one row.", rs.next());
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testGetSQLState() throws Exception {
        connection = getConnectionViaDriverManager();
        Statement stmt = connection.createStatement();
        try {
            expectedException.expect(SQLSyntaxErrorException.class);
            expectedException.expect(sqlState(is(FBSQLException.SQL_STATE_SYNTAX_ERROR)));

            stmt.executeQuery("select * from");
        } finally {
            stmt.close();
        }
    }

    @Test
    public void testLongRange() throws Exception {
        connection = getConnectionViaDriverManager();
        Statement s = connection.createStatement();
        try {
            s.execute("CREATE TABLE LONGTEST (LONGID DECIMAL(18) NOT NULL PRIMARY KEY)");
            s.execute("INSERT INTO LONGTEST (LONGID) VALUES (" + Long.MAX_VALUE + ")");
            ResultSet rs = s.executeQuery("SELECT LONGID FROM LONGTEST");
            try {
                assertTrue("Should have one row!", rs.next());
                assertEquals("Retrieved wrong value!", Long.MAX_VALUE, rs.getLong(1));
            } finally {
                rs.close();
            }

            s.execute("DELETE FROM LONGTEST");
            s.execute("INSERT INTO LONGTEST (LONGID) VALUES (" + Long.MIN_VALUE + ")");
            rs = s.executeQuery("SELECT LONGID FROM LONGTEST");
            try {
                assertTrue("Should have one row!", rs.next());
                assertEquals("Retrieved wrong value!", Long.MIN_VALUE, rs.getLong(1));
            } finally {
                rs.close();
            }
        } finally {
            s.close();
        }
    }

    private static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

    @Test
    public void testDate() throws Exception {
        connection = getConnectionViaDriverManager();
        Statement s = connection.createStatement();
        try {
            s.execute("CREATE TABLE DATETEST (DATEID INTEGER NOT NULL PRIMARY KEY, TESTDATE TIMESTAMP)");
            PreparedStatement ps = connection.prepareStatement("INSERT INTO DATETEST (DATEID, TESTDATE) VALUES (?,?)");
            Calendar cal = new GregorianCalendar(timeZoneUTC);
            Timestamp x = Timestamp.valueOf("1917-02-17 20:59:31");
            try {
                ps.setInt(1, 1);
                ps.setTimestamp(2, x, cal);
                ps.execute();
            } finally {
                ps.close();
            }

            ResultSet rs = s.executeQuery("SELECT TESTDATE FROM DATETEST WHERE DATEID=1");
            try {
                assertTrue("Should have one row!", rs.next());
                assertEquals("Retrieved wrong value!", x, rs.getTimestamp(1, cal));
            } finally {
                rs.close();
            }
        } finally {
            s.close();
        }
    }

    /**
     * This test checks if transaction is rolled back when connection is closed,
     * but still has an active transaction associated with it.
     *
     * @throws Exception
     *         if something went wrong.
     */
    @Test
    public void testClose() throws Exception {
        connection = getConnectionViaDriverManager();
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE test(id INTEGER, test_value INTEGER)");
            stmt.executeUpdate("INSERT INTO test VALUES (1, 1)");
            connection.setAutoCommit(false);
            stmt.executeUpdate("UPDATE test SET test_value = 2 WHERE id = 1");
            stmt.close();
        } finally {
            connection.close();
        }

        connection = getConnectionViaDriverManager();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT test_value FROM test WHERE id = 1");

            assertTrue("Should have at least one row", rs.next());
            assertEquals("Value should be 1.", 1, rs.getInt(1));
            assertTrue("Should have only one row.", !rs.next());

            rs.close();
            stmt.executeUpdate("DROP TABLE test");
            stmt.close();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testDummyPacketIntervalConnect() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("soTimeout", "2000");

        Driver driver = DriverManager.getDriver(getUrl());
        connection = driver.connect(getUrl(), props);

        assertNotNull("Connection is null", connection);
    }

}

