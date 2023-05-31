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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.management.FBManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Properties;
import java.util.stream.Stream;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for specific behavior in dialect 1 databases.
 *
 * @author Mark Rotteveel
 * @since 2.2
 */
class Dialect1SpecificsTest {

    //@formatter:off
    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE test_table(" +
            "  id INTEGER NOT NULL PRIMARY KEY, " +
            "  str VARCHAR(10) " +
            ")";

    private static final String SELECT_TEST_TABLE = "SELECT id, str FROM test_table";

    private static final String INSERT_INTO_TABLE_STATEMENT = "INSERT INTO test_table (id, str) VALUES(?, ?)";
    //@formatter:on

    private FBManager fbManager;

    @BeforeEach
    void basicSetUp() throws Exception {
        fbManager = createFBManager();

        if (getGdsType() == GDSType.getType("PURE_JAVA")
                || getGdsType() == GDSType.getType("NATIVE")
                || getGdsType() == GDSType.getType("FBOONATIVE")) {
            fbManager.setServer(DB_SERVER_URL);
            fbManager.setPort(DB_SERVER_PORT);
        }
        fbManager.setDialect(ISCConstants.SQL_DIALECT_V5);
        fbManager.start();
        fbManager.setForceCreate(true);
        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
    }

    @AfterEach
    void basicTearDown() throws Exception {
        try {
            fbManager.dropDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
        } finally {
            fbManager.stop();
            fbManager = null;
        }
    }

    /**
     * Tests whether updating a row in dialect 1 works
     * <p>
     * Rationale: {@link org.firebirdsql.jdbc.FBRowUpdater} quotes object names in dialect 3, but this shouldn't
     * happen in dialect 1.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testUpdateRow(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            createTestData(1, connection);

            try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                try (ResultSet rs1 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                    assertTrue(rs1.next(), "Expected a row");
                    rs1.updateString(2, "newString1");
                    rs1.updateRow();
                }

                try (ResultSet rs2 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                    assertTrue(rs2.next(), "Expected a row");
                    assertEquals("newString1", rs2.getString(2));
                }
            }
        }
    }

    /**
     * Tests whether inserting a row in dialect 1 works
     * <p>
     * Rationale: {@link org.firebirdsql.jdbc.FBRowUpdater} quotes object names in dialect 3, but this shouldn't
     * happen in dialect 1.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testInsertRow(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            createTestData(1, connection);

            try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                try (ResultSet rs1 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                    rs1.moveToInsertRow();
                    rs1.updateInt(1, 2);
                    rs1.updateString(2, "newString2");
                    rs1.insertRow();
                }

                try (ResultSet rs2 = stmt.executeQuery(SELECT_TEST_TABLE + " WHERE id = 2")) {
                    assertTrue(rs2.next(), "Expected a row");
                    assertEquals("newString2", rs2.getString(2));
                }
            }
        }
    }

    /**
     * Tests whether deleting a row in dialect 1 works
     * <p>
     * Rationale: {@link org.firebirdsql.jdbc.FBRowUpdater} quotes object names in dialect 3, but this shouldn't
     * happen in dialect 1.
     * </p>
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testDeleteRow(String scrollableCursorPropertyValue) throws Exception {
        try (Connection connection = createConnection(scrollableCursorPropertyValue)) {
            createTestData(1, connection);

            try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                try (ResultSet rs1 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                    assertTrue(rs1.next(), "Expected a row");
                    rs1.deleteRow();
                }

                try (ResultSet rs2 = stmt.executeQuery(SELECT_TEST_TABLE)) {
                    assertFalse(rs2.next(), "Expected no row");
                }
            }
        }
    }

    @Test
    void testGetDoubleNumeric() throws Exception {
        try (Connection connection = DriverManager.getConnection(getUrl(), getDefaultPropertiesForTest())) {
            DdlHelper.executeCreateTable(connection,
                    "create table testnumeric (id integer primary key, numericvalue numeric(18,2))");
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("insert into testnumeric(id, numericvalue) values(1, 34.01)");
                try (ResultSet rs = stmt.executeQuery("select * from testnumeric")) {
                    assertTrue(rs.next(), "Expected a row");
                    assertEquals(new BigDecimal("34.01"), rs.getBigDecimal("numericvalue"));
                }
            }
        }
    }

    @Test
    void testSetDoubleNumeric() throws Exception {
        try (Connection connection = DriverManager.getConnection(getUrl(), getDefaultPropertiesForTest())) {
            DdlHelper.executeCreateTable(connection,
                    "create table testnumeric (id integer primary key, numericvalue numeric(18,2))");
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "insert into testnumeric(id, numericvalue) values(1, ?)")) {
                pstmt.setBigDecimal(1, new BigDecimal("34.01242323234"));
                pstmt.executeUpdate();
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "select cast(numericvalue as varchar(50)) as numericvalue from testnumeric")) {
                assertTrue(rs.next(), "Expected a row");
                assertEquals("34.01", rs.getString("numericvalue"));
            }
        }
    }

    private static Properties getDefaultPropertiesForTest() {
        Properties properties = getDefaultPropertiesForConnection();
        properties.setProperty("sql_dialect", String.valueOf(ISCConstants.SQL_DIALECT_V5));
        return properties;
    }

    private static Connection createConnection(String scrollableCursorPropertyValue) throws SQLException {
        Properties props = getDefaultPropertiesForTest();
        props.setProperty(PropertyNames.scrollableCursor, scrollableCursorPropertyValue);
        return DriverManager.getConnection(getUrl(), props);
    }

    static Stream<String> scrollableCursorPropertyValues() {
        // We are unconditionally emitting SERVER, to check if the value behaves appropriately on versions that do
        // not support server-side scrollable cursors
        return Stream.of(PropertyConstants.SCROLLABLE_CURSOR_EMULATED, PropertyConstants.SCROLLABLE_CURSOR_SERVER);
    }

    private void createTestData(int recordCount, Connection connection) throws Exception {
        executeCreateTable(connection, CREATE_TABLE_STATEMENT);
        boolean currentAutoCommit = connection.getAutoCommit();
        if (currentAutoCommit) {
            connection.setAutoCommit(false);
        }
        try (PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TABLE_STATEMENT)) {
            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.execute();
            }
        } finally {
            if (currentAutoCommit) {
                connection.setAutoCommit(true);
            }
        }
    }
}
