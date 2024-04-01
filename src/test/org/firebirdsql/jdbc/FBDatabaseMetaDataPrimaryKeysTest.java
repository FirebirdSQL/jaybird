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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNoNextRow;

/**
 * Test for {@link FBDatabaseMetaData#getPrimaryKeys(String, String, String)}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataPrimaryKeysTest {

    private static final String UNNAMED_PK_PREFIX = "INTEG_";
    private static final String UNNAMED_INDEX_PREFIX = "RDB$PRIMARY";

    //@formatter:off
    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            """
            create table UNNAMED_SINGLE_COLUMN_PK (
              ID integer primary key
            )""",
            """
            create table UNNAMED_MULTI_COLUMN_PK (
              ID1 integer not null,
              ID2 integer not null,
              primary key (ID1, ID2)
            )""",
            """
            create table UNNAMED_PK_NAMED_INDEX (
              ID integer primary key using index ALT_NAMED_INDEX_3
            )""",
            """
            create table NAMED_SINGLE_COLUMN_PK (
              ID integer constraint PK_NAMED_4 primary key
            )""",
            """
            create table NAMED_MULTI_COLUMN_PK (
              ID1 integer not null,
              ID2 integer not null,
              constraint PK_NAMED_5 primary key (ID1, ID2)
            )""",
            """
            create table NAMED_PK_NAMED_INDEX (
              ID integer constraint PK_NAMED_6 primary key using index ALT_NAMED_INDEX_6
            )"""
    );
    //@formatter:on

    private static final MetadataResultSetDefinition getPrimaryKeysDefinition =
            new MetadataResultSetDefinition(PrimaryKeysMetaData.class);

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        con = getConnectionViaDriverManager();
        dbmd = con.getMetaData();
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        try {
            con.close();
        } finally {
            con = null;
            dbmd = null;
        }
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of getPrimaryKeys().
     */
    @Test
    void testPrimaryKeysMetaDataColumns() throws Exception {
        try (ResultSet functions = dbmd.getPrimaryKeys(null, null, "doesnotexist")) {
            getPrimaryKeysDefinition.validateResultSetColumns(functions);
        }
    }

    @Test
    void unnamedSingleColumnPk() throws Exception {
        validateExpectedPrimaryKeys("UNNAMED_SINGLE_COLUMN_PK", List.of(
                createPrimaryKeysRow("UNNAMED_SINGLE_COLUMN_PK", "ID", 1, UNNAMED_PK_PREFIX, UNNAMED_INDEX_PREFIX)));
    }

    @Test
    void unnamedMultiColumnPk() throws Exception {
        validateExpectedPrimaryKeys("UNNAMED_MULTI_COLUMN_PK", List.of(
                createPrimaryKeysRow("UNNAMED_MULTI_COLUMN_PK", "ID1", 1, UNNAMED_PK_PREFIX, UNNAMED_INDEX_PREFIX),
                createPrimaryKeysRow("UNNAMED_MULTI_COLUMN_PK", "ID2", 2, UNNAMED_PK_PREFIX, UNNAMED_INDEX_PREFIX)));
    }

    @Test
    void unnamedPkNamedIndex() throws Exception {
        validateExpectedPrimaryKeys("UNNAMED_PK_NAMED_INDEX", List.of(
                createPrimaryKeysRow("UNNAMED_PK_NAMED_INDEX", "ID", 1, UNNAMED_PK_PREFIX, "ALT_NAMED_INDEX_3")));
    }

    @Test
    void namedSingleColumnPk() throws Exception {
        validateExpectedPrimaryKeys("NAMED_SINGLE_COLUMN_PK", List.of(
                createPrimaryKeysRow("NAMED_SINGLE_COLUMN_PK", "ID", 1, "PK_NAMED_4", "PK_NAMED_4")));
    }

    @Test
    void namedMultiColumnPk() throws Exception {
        validateExpectedPrimaryKeys("NAMED_MULTI_COLUMN_PK", List.of(
                createPrimaryKeysRow("NAMED_MULTI_COLUMN_PK", "ID1", 1, "PK_NAMED_5", "PK_NAMED_5"),
                createPrimaryKeysRow("NAMED_MULTI_COLUMN_PK", "ID2", 2, "PK_NAMED_5", "PK_NAMED_5")));
    }

    @Test
    void namedPkNamedIndex() throws Exception {
        validateExpectedPrimaryKeys("NAMED_PK_NAMED_INDEX", List.of(
                createPrimaryKeysRow("NAMED_PK_NAMED_INDEX", "ID", 1, "PK_NAMED_6", "ALT_NAMED_INDEX_6")));
    }

    private static Map<PrimaryKeysMetaData, Object> createPrimaryKeysRow(String tableName, String columnName,
            int keySeq, String pkName, String jbIndexName) {
        Map<PrimaryKeysMetaData, Object> rules = getDefaultValidationRules();
        rules.put(PrimaryKeysMetaData.TABLE_NAME, tableName);
        rules.put(PrimaryKeysMetaData.COLUMN_NAME, columnName);
        rules.put(PrimaryKeysMetaData.KEY_SEQ, (short) keySeq);
        rules.put(PrimaryKeysMetaData.PK_NAME,
                UNNAMED_PK_PREFIX.equals(pkName) ? Matchers.startsWith(UNNAMED_PK_PREFIX) : pkName);
        rules.put(PrimaryKeysMetaData.JB_INDEX_NAME,
                UNNAMED_INDEX_PREFIX.equals(jbIndexName) ? Matchers.startsWith(UNNAMED_INDEX_PREFIX) : jbIndexName);
        return rules;
    }

    private void validateExpectedPrimaryKeys(String tableName, List<Map<PrimaryKeysMetaData, Object>> expectedColumns)
            throws Exception {
        try (ResultSet columns = dbmd.getPrimaryKeys(null, null, tableName)) {
            for (Map<PrimaryKeysMetaData, Object> expectedColumn : expectedColumns) {
                assertNextRow(columns);
                getPrimaryKeysDefinition.validateRowValues(columns, expectedColumn);
            }
            assertNoNextRow(columns);
        }
    }

    private static Map<PrimaryKeysMetaData, Object> getDefaultValidationRules() {
        return new EnumMap<>(PrimaryKeysMetaData.class);
    }

    private enum PrimaryKeysMetaData implements MetaDataInfo {
        TABLE_CAT(1, String.class),
        TABLE_SCHEM(2, String.class),
        TABLE_NAME(3, String.class),
        COLUMN_NAME(4, String.class),
        KEY_SEQ(5, Short.class),
        PK_NAME(6, String.class),
        JB_INDEX_NAME(7, String.class);

        private final int position;
        private final Class<?> columnClass;

        PrimaryKeysMetaData(int position, Class<?> columnClass) {
            this.position = position;
            this.columnClass = columnClass;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public Class<?> getColumnClass() {
            return columnClass;
        }
    }
}
