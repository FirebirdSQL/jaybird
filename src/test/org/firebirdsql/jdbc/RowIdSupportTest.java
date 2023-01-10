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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exploratory test for RowId support
 *
 * @author Mark Rotteveel
 */
class RowIdSupportTest {

    //@formatter:off
    private static final String CREATE_TABLE =
            "create table testrowid ("
            + " column1 varchar(100)"
            + ")";

    private static final String CREATE_VIEW =
            "create view testrowidview(column1_1, column2_1) as "
            + " select a.column1 as column1_1, b.column1 as column1_2"
            + " from testrowid a"
            + " inner join testrowid b on a.column1 = b.column1";

    private static final String INSERT_VALUE = "insert into testrowid(column1) values (?)";
    private static final String SELECT_DB_KEY_IN_SELECT_LIST = "select rdb$db_key, column1 from testrowid";
    private static final String SELECT_WHERE_DB_KEY = "select column1 from testrowid where rdb$db_key = ?";
    //@formatter:on

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE,
            CREATE_VIEW);

    private static Connection connection;

    @BeforeAll
    static void setupAll() throws Exception {
        connection = getConnectionViaDriverManager();
    }

    @AfterEach
    void setup() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from testrowid");
        } finally {
            connection.setAutoCommit(false);
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }

    @Test
    void testDbMetaDataRowIdLifetime() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();

        assertEquals(RowIdLifetime.ROWID_VALID_TRANSACTION, metaData.getRowIdLifetime());
    }

    @Test
    void testDbMetaDataGetPseudoColumns_table() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getPseudoColumns(null, null, "TESTROWID", "RDB$DB\\_KEY")) {
            assertTrue(rs.next(), "Expected row for RDB$DB_KEY");
            assertEquals("RDB$DB_KEY", rs.getString("COLUMN_NAME"), "COLUMN_NAME");
            assertEquals("TESTROWID", rs.getString("TABLE_NAME"), "TABLE_NAME");
            assertEquals(Types.ROWID, rs.getObject("DATA_TYPE"), "DATA_TYPE");
            assertEquals(8, rs.getObject("COLUMN_SIZE"), "COLUMN_SIZE");
            assertNull(rs.getObject("DECIMAL_DIGITS"), "DECIMAL_DIGITS");
            assertEquals(10, rs.getObject("NUM_PREC_RADIX"), "NUM_PREC_RADIX");
            assertEquals("NO_USAGE_RESTRICTIONS", rs.getString("COLUMN_USAGE"), "COLUMN_USAGE");
            assertEquals(8, rs.getObject("CHAR_OCTET_LENGTH"), "CHAR_OCTET_LENGTH");
            assertEquals("NO", rs.getString("IS_NULLABLE"), "IS_NULLABLE");
        }
    }

    @Test
    void testDbMetaDataGetPseudoColumns_viewTwoTableJoin() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getPseudoColumns(null, null, "TESTROWIDVIEW", "RDB$DB\\_KEY")) {
            assertTrue(rs.next(), "Expected row for RDB$DB_KEY");
            assertEquals("RDB$DB_KEY", rs.getString("COLUMN_NAME"), "COLUMN_NAME");
            assertEquals("TESTROWIDVIEW", rs.getString("TABLE_NAME"), "TABLE_NAME");
            assertEquals(Types.ROWID, rs.getObject("DATA_TYPE"), "DATA_TYPE");
            assertEquals(16, rs.getObject("COLUMN_SIZE"), "COLUMN_SIZE");
            assertNull(rs.getObject("DECIMAL_DIGITS"), "DECIMAL_DIGITS");
            assertEquals(10, rs.getObject("NUM_PREC_RADIX"), "NUM_PREC_RADIX");
            assertEquals("NO_USAGE_RESTRICTIONS", rs.getString("COLUMN_USAGE"), "COLUMN_USAGE");
            assertEquals(16, rs.getObject("CHAR_OCTET_LENGTH"), "CHAR_OCTET_LENGTH");
            assertEquals("NO", rs.getString("IS_NULLABLE"), "IS_NULLABLE");
        }
    }

    @Test
    void testResultSetMetaData() throws Exception {
        initTestTable();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select rdb$db_key as XY, column1 from testrowid")) {
            ResultSetMetaData rsMetaData = rs.getMetaData();

            assertEquals(Types.ROWID, rsMetaData.getColumnType(1));
            assertEquals("java.sql.RowId", rsMetaData.getColumnClassName(1));
            assertEquals("CHAR", rsMetaData.getColumnTypeName(1));
        }
    }

    @Test
    void testPreparedStatementResultSetMetaData() throws Exception {
        initTestTable();

        try (PreparedStatement stmt = connection.prepareStatement(SELECT_DB_KEY_IN_SELECT_LIST)) {
            ResultSetMetaData rsMetaData = stmt.getMetaData();

            assertEquals(Types.ROWID, rsMetaData.getColumnType(1));
            assertEquals("java.sql.RowId", rsMetaData.getColumnClassName(1));
            assertEquals("CHAR", rsMetaData.getColumnTypeName(1));
        }
    }

    @Test
    void testPreparedStatementParameterMetaData() throws Exception {
        initTestTable();

        try (PreparedStatement stmt = connection.prepareStatement(SELECT_WHERE_DB_KEY)) {
            ParameterMetaData parameterMetaData = stmt.getParameterMetaData();

            // Rowid detection for parameters not possible; instead it is detected as binary
            assertEquals(Types.BINARY, parameterMetaData.getParameterType(1));
            assertEquals(byte[].class.getName(), parameterMetaData.getParameterClassName(1));
            assertEquals("CHAR", parameterMetaData.getParameterTypeName(1));
        }
    }

    @Test
    void testGetRowByRowId() throws Exception {
        initTestTable("value1", "value2");
        connection.setAutoCommit(false);

        try (Statement stmt1 = connection.createStatement();
             ResultSet rs1 = stmt1.executeQuery(SELECT_DB_KEY_IN_SELECT_LIST);
             PreparedStatement pstmt2 = connection.prepareStatement(SELECT_WHERE_DB_KEY)) {

            int rowCount = 0;
            while (rs1.next()) {
                rowCount++;
                RowId rowId = rs1.getRowId("rdb$db_key");
                pstmt2.setRowId(1, rowId);
                try (ResultSet rs2 = pstmt2.executeQuery()) {
                    assertTrue(rs2.next(), "Expected row matching rowid");
                    assertEquals(rs1.getString("column1"), rs2.getString("column1"), "Unexpected value for column1");
                }
            }
            assertEquals(2, rowCount, "Unexpected number of rows processed");
        }
    }

    @Test
    void testDbKeyNotUpdatable() throws Exception {
        initTestTable("value1");

        try (Statement stmt1 = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = stmt1.executeQuery(SELECT_DB_KEY_IN_SELECT_LIST)) {
            assertTrue(rs.next(), "Expected a row");

            assertThrows(FBDriverNotCapableException.class,
                    () -> rs.updateRowId("rdb$db_key", new FBRowId(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 })));
        }
    }

    private void initTestTable(String... columnValues) throws SQLException {
        if (columnValues == null || columnValues.length == 0) {
            return;
        }
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(INSERT_VALUE)) {
            for (String columnValue : columnValues) {
                statement.setString(1, columnValue);
                statement.execute();
            }
        }
        connection.setAutoCommit(true);
    }
}
