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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;

import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Test suite for the FBDriver class implementation.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBDriver extends FBTestBase {

    private Driver driver;

    public TestFBDriver(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Class.forName(org.firebirdsql.jdbc.FBDriver.class.getName());
        driver = DriverManager.getDriver(getUrl());
    }

    public void testAcceptsURL() throws Exception {
        assertTrue(driver.acceptsURL(getUrl()));
    }

    public void testConnect() throws Exception {
        Connection connection = driver.connect(getUrl(), getDefaultPropertiesForConnection());
        try {
            assertTrue("Connection is null", connection != null);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void testJdbcCompliant() {
        // current driver is not JDBC compliant.
        assertTrue(driver.jdbcCompliant());
    }

    /**
     * This method tests if driver correctly handles warnings returned from
     * database. We use SQL dialect mismatch between client and server to
     * make server return us a warning.
     */
    public void testWarnings() throws Exception {
        Properties info = (Properties) getDefaultPropertiesForConnection().clone();
        info.setProperty("set_db_sql_dialect", "1");

        try {
            // open connection and convert DB to SQL dialect 1
            Connection dialect1Connection = DriverManager.getConnection(getUrl(), info);

            try {
                Statement stmt = dialect1Connection.createStatement();

                // execute select statement, driver will pass SQL dialect 3
                // for this statement and database server will return a warning
                stmt.executeQuery("SELECT 1 as col1 FROM rdb$database");

                stmt.close();

                SQLWarning warning = dialect1Connection.getWarnings();

                assertTrue("Connection should have at least one warning.", warning != null);

                dialect1Connection.clearWarnings();

                assertTrue("After clearing no warnings should be present.", dialect1Connection.getWarnings() == null);
            } finally {
                dialect1Connection.close();
            }
        } finally {
            info.setProperty("set_db_sql_dialect", "3");

            Connection dialect3Connection = DriverManager.getConnection(getUrl(), info);
            dialect3Connection.close();
        }
    }

    public void testDialect1() throws Exception {
        Properties info = (Properties) getDefaultPropertiesForConnection().clone();
        info.setProperty("isc_dpb_sql_dialect", "1");

        Connection dialect1Connection = DriverManager.getConnection(getUrl(), info);
        try {
            Statement stmt = dialect1Connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT  cast(\"today\" as date) - 7 FROM rdb$database");
                assertTrue("Should have at least one row.", rs.next());
            } catch (SQLException ex) {
                ex.printStackTrace();
                fail("In dialect 1 doublequotes in strings are allowed.");
            } finally {
                stmt.close();
            }
        } finally {
            dialect1Connection.close();
        }
    }

    public void testGetSQLState() throws Exception {
        Connection connection = getConnectionViaDriverManager();

        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.executeQuery("select * from");
                fail("Expected exception to be thrown");
            } catch (SQLException ex) {
                String sqlState = ex.getSQLState();
                assertNotNull("getSQLState() method does not return value", sqlState);
            } finally {
                stmt.close();
            }
        } finally {
            connection.close();
        }
    }

    public void testLongRange() throws Exception {
        Connection c = getConnectionViaDriverManager();
        try {
            Statement s = c.createStatement();
            try {
                s.execute("CREATE TABLE LONGTEST (LONGID DECIMAL(18) NOT NULL PRIMARY KEY)");
                try {
                    s.execute("INSERT INTO LONGTEST (LONGID) VALUES (" + Long.MAX_VALUE + ")");
                    ResultSet rs = s.executeQuery("SELECT LONGID FROM LONGTEST");
                    try {
                        assertTrue("Should have one row!", rs.next());
                        assertTrue("Retrieved wrong value!", rs.getLong(1) == Long.MAX_VALUE);
                    } finally {
                        rs.close();
                    }
                    s.execute("DELETE FROM LONGTEST");
                    s.execute("INSERT INTO LONGTEST (LONGID) VALUES (" + Long.MIN_VALUE + ")");
                    rs = s.executeQuery("SELECT LONGID FROM LONGTEST");
                    try {
                        assertTrue("Should have one row!", rs.next());
                        assertTrue("Retrieved wrong value!", rs.getLong(1) == Long.MIN_VALUE);
                    } finally {
                        rs.close();
                    }
                } finally {
                    s.execute("DROP TABLE LONGTEST");
                }
            } finally {
                s.close();
            }
        } finally {
            c.close();
        }
    }

    private static final TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");

    public void testDate() throws Exception {
        Connection c = getConnectionViaDriverManager();
        try {
            Statement s = c.createStatement();
            try {
                s.execute("CREATE TABLE DATETEST (DATEID INTEGER NOT NULL PRIMARY KEY, TESTDATE TIMESTAMP)");
                PreparedStatement ps = c.prepareStatement("INSERT INTO DATETEST (DATEID, TESTDATE) VALUES (?,?)");
                Calendar cal = new GregorianCalendar(timeZoneUTC);
                Timestamp x = Timestamp.valueOf("1917-02-17 20:59:31");
                try {
                    ps.setInt(1, 1);
                    ps.setTimestamp(2, x, cal);
                    ps.execute();
                } finally {
                    ps.close();
                }

                try {
                    ResultSet rs = s.executeQuery("SELECT TESTDATE FROM DATETEST WHERE DATEID=1");
                    try {

                        assertTrue("Should have one row!", rs.next());
                        Timestamp x2 = rs.getTimestamp(1, cal);
                        assertTrue("Retrieved wrong value! expected: " + x + ", actual: " + x2, x.equals(x2));
                    } finally {
                        rs.close();
                    }
                } finally {
                    s.execute("DROP TABLE DATETEST");
                }
            } finally {
                s.close();
            }
        } finally {
            c.close();
        }
    }

    /**
     * This test checks if transaction is rolled back when connection is closed,
     * but still has an active transaction associated with it.
     *
     * @throws Exception
     *         if something went wrong.
     */
    public void testClose() throws Exception {
        Connection connection = getConnectionViaDriverManager();
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

    public void testDummyPacketIntervalConnect() throws Exception {
        if (log != null) log.info(getUrl());
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("soTimeout", "2000");
        Connection connection = driver.connect(getUrl(), props);
        try {
            assertTrue("Connection is null", connection != null);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void testTransactionConfigThroughPropertiesObject() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // Note that for proper testing this needs to be different from the mapping in isc_tpb_mapping.properties
        props.setProperty("TRANSACTION_READ_COMMITTED",
                "isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_nowait");
        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            FirebirdConnection fbConnection = connection.unwrap(FirebirdConnection.class);
            TransactionParameterBuffer tpb =
                    fbConnection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

            assertTrue("expected isc_tpb_read_committed", tpb.hasArgument(ISCConstants.isc_tpb_read_committed));
            assertTrue("expected isc_tpb_no_rec_version", tpb.hasArgument(ISCConstants.isc_tpb_no_rec_version));
            assertTrue("expected isc_tpb_write", tpb.hasArgument(ISCConstants.isc_tpb_write));
            assertTrue("expected isc_tpb_nowait", tpb.hasArgument(ISCConstants.isc_tpb_nowait));
        } finally {
            connection.close();
        }
    }

    public void testTransactionConfigThroughConnectionString() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        // Note that for proper testing this needs to be different from the mapping in isc_tpb_mapping.properties
        String jdbcUrl = getUrl() + "?TRANSACTION_READ_COMMITTED=isc_tpb_read_committed,isc_tpb_no_rec_version,isc_tpb_write,isc_tpb_nowait";
        Connection connection = DriverManager.getConnection(jdbcUrl, props);
        try {
            FirebirdConnection fbConnection = connection.unwrap(FirebirdConnection.class);
            TransactionParameterBuffer tpb =
                    fbConnection.getTransactionParameters(Connection.TRANSACTION_READ_COMMITTED);

            assertTrue("expected isc_tpb_read_committed", tpb.hasArgument(ISCConstants.isc_tpb_read_committed));
            assertTrue("expected isc_tpb_no_rec_version", tpb.hasArgument(ISCConstants.isc_tpb_no_rec_version));
            assertTrue("expected isc_tpb_write", tpb.hasArgument(ISCConstants.isc_tpb_write));
            assertTrue("expected isc_tpb_nowait", tpb.hasArgument(ISCConstants.isc_tpb_nowait));
        } finally {
            connection.close();
        }
    }
}

