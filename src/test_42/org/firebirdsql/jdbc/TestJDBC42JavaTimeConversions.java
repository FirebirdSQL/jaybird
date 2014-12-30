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

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.jdbc.field.TypeConversionException;

import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

/**
 * Test the JDBC 4.2 conversions for <code>java.time</code> (JSR 310) types.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.2
 */
public class TestJDBC42JavaTimeConversions extends FBTestBase {

    private Connection connection;

    private static final String CREATE_TABLE =
            "CREATE TABLE javatimetest (" +
                    "  ID INTEGER PRIMARY KEY," +
                    "  aDate DATE," +
                    "  aTime TIME," +
                    "  aTimestamp TIMESTAMP," +
                    "  aChar CHAR(100)," +
                    "  aVarchar VARCHAR(100)" +
                    ")";

    public TestJDBC42JavaTimeConversions(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        connection = getConnectionViaDriverManager();
        executeCreateTable(connection, CREATE_TABLE);
    }

    public void tearDown() throws Exception {
        try {
            closeQuietly(connection);
        } finally {
            super.tearDown();
        }
    }

    public void testLocalDate_ToDateColumn() throws Exception {
        final LocalDate localDate = LocalDate.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                Date aDate = rs.getDate(1);
                LocalDate asLocalDate = aDate.toLocalDate();
                assertEquals("Expected retrieved java.time.LocalDate as DATE to be the same as inserted value", localDate, asLocalDate);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalDate_ToTimeColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)");
        try {
            final LocalDate localDate = LocalDate.now();
            pstmt.setObject(1, localDate);
            fail("Expected setting a java.time.LocalDate to a TIME column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testLocalDate_ToTimestampColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)");
        try {
            final LocalDate localDate = LocalDate.now();
            pstmt.setObject(1, localDate);
            fail("Expected setting a java.time.LocalDate to a TIMESTAMP column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testLocalDate_ToCharColumn() throws Exception {
        final LocalDate localDate = LocalDate.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aChar = rs.getString(1);
                assertEquals("Expected retrieved java.time.LocalDate as CHAR to have the same (trimmed) string value as the inserted value",
                        localDate.toString(), aChar.trim());
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalDate_ToVarCharColumn() throws Exception {
        final LocalDate localDate = LocalDate.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localDate);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aVarchar = rs.getString(1);
                assertEquals("Expected retrieved java.time.LocalDate as VARCHAR to have the same string value as the inserted value",
                        localDate.toString(), aVarchar);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalTime_ToDateColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)");
        try {
            final LocalTime localTime = LocalTime.now();
            pstmt.setObject(1, localTime);
            fail("Expected setting a java.time.LocalTime to a DATE column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testLocalTime_ToTimeColumn() throws Exception {
        final LocalTime localTime = LocalTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                Time aTime = rs.getTime(1);
                LocalTime asLocalTime = aTime.toLocalTime();
                // TODO We need to add better support for LocalTime to actually support subsecond precision
                assertEquals("Expected retrieved java.time.LocalTime as TIME to be the same as inserted value",
                        localTime.truncatedTo(ChronoUnit.SECONDS), asLocalTime);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalTime_ToTimestampColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)");
        try {
            final LocalTime localTime = LocalTime.now();
            pstmt.setObject(1, localTime);
            fail("Expected setting a java.time.LocalTime to a TIMESTAMP column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testLocalTime_ToCharColumn() throws Exception {
        final LocalTime localTime = LocalTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aChar = rs.getString(1);
                assertEquals("Expected retrieved java.time.LocalTime as CHAR (trimmed) to be the same as toString of inserted value",
                        localTime.toString(), aChar.trim());
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalTime_ToVarcharColumn() throws Exception {
        final LocalTime localTime = LocalTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aVarchar = rs.getString(1);
                assertEquals("Expected retrieved java.time.LocalTime as VARCHAR to be the same as toString of inserted value",
                        localTime.toString(), aVarchar);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalDateTime_ToDateColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aDate FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                java.sql.Date aDate = rs.getDate(1);
                LocalDateTime asLocalDateTime = aDate.toLocalDate().atStartOfDay();
                assertEquals("Expected retrieved java.time.LocalDateTime as DATE to be the same as inserted value truncated to days",
                        localDateTime.truncatedTo(ChronoUnit.DAYS), asLocalDateTime);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalDateTime_ToTimeColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aTime FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                java.sql.Time aTime = rs.getTime(1);
                LocalTime asLocalTime = aTime.toLocalTime();
                // TODO We need to add better support for LocalTime to actually support subsecond precision
                assertEquals("Expected retrieved java.time.LocalDateTime as TIME to be the same as LocalTime portion of inserted value",
                        localDateTime.toLocalTime().truncatedTo(ChronoUnit.SECONDS), asLocalTime);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalDateTime_ToTimestampColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aTimestamp FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                java.sql.Timestamp aTimestamp = rs.getTimestamp(1);
                LocalDateTime asLocalDateTime = aTimestamp.toLocalDateTime();
                assertEquals("Expected retrieved java.time.LocalDateTime as TIMESTAMP to be the same as inserted value",
                        localDateTime, asLocalDateTime);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalDateTime_ToCharColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aChar = rs.getString(1);
                assertEquals("Expected retrieved java.time.LocalDateTime as CHAR (trimmed) to be the same as toString of inserted value",
                        localDateTime.toString(), aChar.trim());
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testLocalDateTime_ToVarcharColumn() throws Exception {
        final LocalDateTime localDateTime = LocalDateTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, localDateTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aVarchar = rs.getString(1);
                assertEquals("Expected retrieved java.time.LocalDateTime as VARCHAR to be the same as toString of inserted value",
                        localDateTime.toString(), aVarchar);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testOffsetTime_ToDateColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)");
        try {
            final OffsetTime offsetTime = OffsetTime.now();
            pstmt.setObject(1, offsetTime);
            fail("Expected setting a java.time.OffsetTime to a DATE column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testOffsetTime_ToTimeColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)");
        try {
            final OffsetTime offsetTime = OffsetTime.now();
            pstmt.setObject(1, offsetTime);
            fail("Expected setting a java.time.OffsetTime to a TIME column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testOffsetTime_ToTimestampColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)");
        try {
            final OffsetTime offsetTime = OffsetTime.now();
            pstmt.setObject(1, offsetTime);
            fail("Expected setting a java.time.OffsetTime to a Timestamp column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testOffsetTime_ToCharColumn() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aChar = rs.getString(1);
                assertEquals("Expected retrieved java.time.OffsetTime as CHAR (trimmed) to be the same as toString of inserted value",
                        offsetTime.toString(), aChar.trim());
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testOffsetTime_ToVarcharColumn() throws Exception {
        final OffsetTime offsetTime = OffsetTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, offsetTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aVarchar = rs.getString(1);
                assertEquals("Expected retrieved java.time.OffsetTime as VARCHAR to be the same as toString of inserted value",
                        offsetTime.toString(), aVarchar);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    // XXXX

    public void testOffsetDateTime_ToDateColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aDate) VALUES (1, ?)");
        try {
            final OffsetDateTime offsetDateTime = OffsetDateTime.now();
            pstmt.setObject(1, offsetDateTime);
            fail("Expected setting a java.time.OffsetTime to a DATE column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testOffsetDateTime_ToTimeColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTime) VALUES (1, ?)");
        try {
            final OffsetDateTime offsetDateTime = OffsetDateTime.now();
            pstmt.setObject(1, offsetDateTime);
            fail("Expected setting a java.time.OffsetTime to a TIME column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testOffsetDateTime_ToTimestampColumn() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aTimestamp) VALUES (1, ?)");
        try {
            final OffsetDateTime offsetDateTime = OffsetDateTime.now();
            pstmt.setObject(1, offsetDateTime);
            fail("Expected setting a java.time.OffsetTime to a Timestamp column to fail");
        } catch (TypeConversionException ex) {
            // Expected exception
        } finally {
            pstmt.close();
        }
    }

    public void testOffsetDateTime_ToCharColumn() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aChar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aChar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aChar = rs.getString(1);
                assertEquals("Expected retrieved java.time.OffsetTime as CHAR (trimmed) to be the same as toString of inserted value",
                        offsetDateTime.toString(), aChar.trim());
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    public void testOffsetDateTime_ToVarcharColumn() throws Exception {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        PreparedStatement pstmt = connection.prepareStatement("INSERT INTO javatimetest (ID, aVarchar) VALUES (1, ?)");
        try {
            pstmt.setObject(1, offsetDateTime);
            pstmt.execute();
        } finally {
            pstmt.close();
        }

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT aVarchar FROM javatimetest WHERE ID = 1");
            try {
                assertTrue("Expected a row", rs.next());
                String aVarchar = rs.getString(1);
                assertEquals("Expected retrieved java.time.OffsetTime as VARCHAR to be the same as toString of inserted value",
                        offsetDateTime.toString(), aVarchar);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }
}
