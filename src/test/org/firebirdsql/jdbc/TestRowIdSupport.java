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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Exploratory test for RowId support
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestRowIdSupport extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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

    @Test
    public void testDbMetaDataRowIdLifetime() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            DatabaseMetaData metaData = connection.getMetaData();

            assertEquals(RowIdLifetime.ROWID_VALID_TRANSACTION, metaData.getRowIdLifetime());
        }
    }

    @Test
    public void testDbMetaDataGetPseudoColumns_table() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, CREATE_TABLE);

            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getPseudoColumns(null, null, "TESTROWID", "RDB$DB\\_KEY")) {
                assertTrue("Expected row for RDB$DB_KEY", rs.next());
                assertEquals("COLUMN_NAME", "RDB$DB_KEY", rs.getString("COLUMN_NAME"));
                assertEquals("TABLE_NAME", "TESTROWID", rs.getString("TABLE_NAME"));
                assertEquals("DATA_TYPE", Types.ROWID, rs.getObject("DATA_TYPE"));
                assertEquals("COLUMN_SIZE", 8, rs.getObject("COLUMN_SIZE"));
                assertEquals("DECIMAL_DIGITS", 0, rs.getObject("DECIMAL_DIGITS"));
                assertEquals("NUM_PREC_RADIX", 10, rs.getObject("NUM_PREC_RADIX"));
                assertEquals("COLUMN_USAGE", "NO_USAGE_RESTRICTIONS", rs.getString("COLUMN_USAGE"));
                assertEquals("CHAR_OCTET_LENGTH", 8, rs.getObject("CHAR_OCTET_LENGTH"));
                assertEquals("IS_NULLABLE", "NO", rs.getString("IS_NULLABLE"));
            }
        }
    }

    @Test
    public void testDbMetaDataGetPseudoColumns_viewTwoTableJoin() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            executeCreateTable(connection, CREATE_TABLE);
            executeCreateTable(connection, CREATE_VIEW);
            connection.commit();

            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getPseudoColumns(null, null, "TESTROWIDVIEW", "RDB$DB\\_KEY")) {
                assertTrue("Expected row for RDB$DB_KEY", rs.next());
                assertEquals("COLUMN_NAME", "RDB$DB_KEY", rs.getString("COLUMN_NAME"));
                assertEquals("TABLE_NAME", "TESTROWIDVIEW", rs.getString("TABLE_NAME"));
                assertEquals("DATA_TYPE", Types.ROWID, rs.getObject("DATA_TYPE"));
                assertEquals("COLUMN_SIZE", 16, rs.getObject("COLUMN_SIZE"));
                assertEquals("DECIMAL_DIGITS", 0, rs.getObject("DECIMAL_DIGITS"));
                assertEquals("NUM_PREC_RADIX", 10, rs.getObject("NUM_PREC_RADIX"));
                assertEquals("COLUMN_USAGE", "NO_USAGE_RESTRICTIONS", rs.getString("COLUMN_USAGE"));
                assertEquals("CHAR_OCTET_LENGTH", 16, rs.getObject("CHAR_OCTET_LENGTH"));
                assertEquals("IS_NULLABLE", "NO", rs.getString("IS_NULLABLE"));
            }
        }
    }

    @Test
    public void testResultSetMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            initTestTable(connection);

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("select rdb$db_key as XY, column1 from testrowid")) {
                ResultSetMetaData rsMetaData = rs.getMetaData();

                assertEquals(Types.ROWID, rsMetaData.getColumnType(1));
                assertEquals("java.sql.RowId", rsMetaData.getColumnClassName(1));
                assertEquals("CHAR", rsMetaData.getColumnTypeName(1));
            }
        }
    }

    @Test
    public void testPreparedStatementResultSetMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            initTestTable(connection);

            try (PreparedStatement stmt = connection.prepareStatement(SELECT_DB_KEY_IN_SELECT_LIST)) {
                ResultSetMetaData rsMetaData = stmt.getMetaData();

                assertEquals(Types.ROWID, rsMetaData.getColumnType(1));
                assertEquals("java.sql.RowId", rsMetaData.getColumnClassName(1));
                assertEquals("CHAR", rsMetaData.getColumnTypeName(1));
            }
        }
    }

    @Test
    public void testPreparedStatementParameterMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            initTestTable(connection);

            try (PreparedStatement stmt = connection.prepareStatement(SELECT_WHERE_DB_KEY)) {
                ParameterMetaData parameterMetaData = stmt.getParameterMetaData();

                // Rowid detection for parameters not possible; instead it is detected as binary
                assertEquals(Types.BINARY, parameterMetaData.getParameterType(1));
                assertEquals(byte[].class.getName(), parameterMetaData.getParameterClassName(1));
                assertEquals("CHAR", parameterMetaData.getParameterTypeName(1));
            }
        }
    }

    @Test
    public void testGetRowByRowId() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            initTestTable(connection, "value1", "value2");
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
                        assertTrue("Expected row matching rowid", rs2.next());
                        assertEquals("Unexpected value for column1",
                                rs1.getString("column1"), rs2.getString("column1"));
                    }
                }
                assertEquals("Unexpected number of rows processed", 2, rowCount);
            }
        }
    }

    @Test
    public void testDbKeyNotUpdatable() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            initTestTable(connection, "value1");

            try (Statement stmt1 = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                 ResultSet rs = stmt1.executeQuery(SELECT_DB_KEY_IN_SELECT_LIST)) {
                assertTrue("Expected a row", rs.next());

                expectedException.expect(FBDriverNotCapableException.class);
                rs.updateRowId("rdb$db_key", new FBRowId(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }));
            }
        }
    }

    private void initTestTable(Connection connection, String... columnValues) throws SQLException {
        executeCreateTable(connection, CREATE_TABLE);
        if (columnValues == null || columnValues.length == 0) {
            return;
        }
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(INSERT_VALUE)) {
            for (String columnValue : columnValues) {
                statement.setString(1, columnValue);
                statement.addBatch();
            }
            statement.executeBatch();
        }
        connection.setAutoCommit(true);
    }
}
