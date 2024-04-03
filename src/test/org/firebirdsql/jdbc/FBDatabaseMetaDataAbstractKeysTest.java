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
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNoNextRow;

/**
 * Base test class for subclasses of {@code org.firebirdsql.jdbc.metadata.AbstractKeysMethod}.
 *
 * @author Mark Rotteveel
 */
abstract class FBDatabaseMetaDataAbstractKeysTest {

    private static final String UNNAMED_CONSTRAINT_PREFIX = "INTEG_";
    private static final String UNNAMED_PK_INDEX_PREFIX = "RDB$PRIMARY";
    private static final String UNNAMED_FK_INDEX_PREFIX = "RDB$FOREIGN";

    //@formatter:off
    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            """
            create table TABLE_1 (
              ID integer constraint PK_TABLE_1 primary key
            )""",
            """
            create table TABLE_2 (
              ID1 integer not null,
              ID2 integer not null,
              TABLE_1_ID integer constraint FK_TABLE_2_TO_1 references TABLE_1 (ID),
              constraint PK_TABLE_2 unique (ID1, ID2) using index ALT_INDEX_NAME_2
            )""",
            """
            create table TABLE_3 (
              ID integer constraint PK_TABLE_3 primary key using index ALT_INDEX_NAME_3,
              TABLE_2_ID1 integer,
              TABLE_2_ID2 integer,
              constraint FK_TABLE_3_TO_2 foreign key (TABLE_2_ID1, TABLE_2_ID2) references TABLE_2 (ID1, ID2)
                on delete cascade on update set default
            )""",
            """
            create table TABLE_4 (
              ID integer primary key using index ALT_INDEX_NAME_4,
              TABLE_2_ID1 integer,
              TABLE_2_ID2 integer,
              constraint FK_TABLE_4_TO_2 foreign key (TABLE_2_ID1, TABLE_2_ID2) references TABLE_2 (ID1, ID2)
                on delete set default on update set null
            )""",
            """
            create table TABLE_5 (
              ID integer primary key,
              TABLE_2_ID1 integer,
              TABLE_2_ID2 integer,
              foreign key (TABLE_2_ID1, TABLE_2_ID2) references TABLE_2 (ID1, ID2)
                on delete set null on update no action using index ALT_INDEX_NAME_5
            )""",
            """
            create table TABLE_6 (
              ID integer primary key,
              TABLE_2_ID1 integer,
              TABLE_2_ID2 integer,
              foreign key (TABLE_2_ID1, TABLE_2_ID2) references TABLE_2 (ID1, ID2)
                on delete no action on update cascade
            )""",
            """
            create table TABLE_7 (
              ID integer primary key,
              TABLE_6_ID integer constraint FK_TABLE_7_TO_6 references TABLE_6 (ID) on update cascade
            )"""
    );
    //@formatter:on

    protected static final MetadataResultSetDefinition keysDefinition =
            new MetadataResultSetDefinition(KeysMetaData.class);

    protected static Connection con;
    protected static DatabaseMetaData dbmd;

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

    protected static List<Map<KeysMetaData, Object>> table1Fks() {
        // No FKs in this table
        return List.of();
    }

    protected static List<Map<KeysMetaData, Object>> table2Fks() {
        return List.of(
                createKeysTestData("TABLE_1", "ID", "TABLE_2", "TABLE_1_ID", 1, DatabaseMetaData.importedKeyNoAction,
                        DatabaseMetaData.importedKeyNoAction, "PK_TABLE_1", "FK_TABLE_2_TO_1", "PK_TABLE_1",
                        "FK_TABLE_2_TO_1"));
    }

    protected static List<Map<KeysMetaData, Object>> table3Fks() {
        return List.of(
                createKeysTestData("TABLE_2", "ID1", "TABLE_3", "TABLE_2_ID1", 1,
                        DatabaseMetaData.importedKeySetDefault, DatabaseMetaData.importedKeyCascade, "PK_TABLE_2",
                        "FK_TABLE_3_TO_2", "ALT_INDEX_NAME_2", "FK_TABLE_3_TO_2"),
                createKeysTestData("TABLE_2", "ID2", "TABLE_3", "TABLE_2_ID2", 2,
                        DatabaseMetaData.importedKeySetDefault, DatabaseMetaData.importedKeyCascade, "PK_TABLE_2",
                        "FK_TABLE_3_TO_2", "ALT_INDEX_NAME_2", "FK_TABLE_3_TO_2"));
    }

    protected static List<Map<KeysMetaData, Object>> table4Fks() {
        return List.of(
                createKeysTestData("TABLE_2", "ID1", "TABLE_4", "TABLE_2_ID1", 1,
                        DatabaseMetaData.importedKeySetNull, DatabaseMetaData.importedKeySetDefault, "PK_TABLE_2",
                        "FK_TABLE_4_TO_2", "ALT_INDEX_NAME_2", "FK_TABLE_4_TO_2"),
                createKeysTestData("TABLE_2", "ID2", "TABLE_4", "TABLE_2_ID2", 2,
                        DatabaseMetaData.importedKeySetNull, DatabaseMetaData.importedKeySetDefault, "PK_TABLE_2",
                        "FK_TABLE_4_TO_2", "ALT_INDEX_NAME_2", "FK_TABLE_4_TO_2"));
    }

    protected static List<Map<KeysMetaData, Object>> table5Fks() {
        return List.of(
                createKeysTestData("TABLE_2", "ID1", "TABLE_5", "TABLE_2_ID1", 1,
                        DatabaseMetaData.importedKeyNoAction, DatabaseMetaData.importedKeySetNull, "PK_TABLE_2",
                        UNNAMED_CONSTRAINT_PREFIX, "ALT_INDEX_NAME_2", "ALT_INDEX_NAME_5"),
                createKeysTestData("TABLE_2", "ID2", "TABLE_5", "TABLE_2_ID2", 2,
                        DatabaseMetaData.importedKeyNoAction, DatabaseMetaData.importedKeySetNull, "PK_TABLE_2",
                        UNNAMED_CONSTRAINT_PREFIX, "ALT_INDEX_NAME_2", "ALT_INDEX_NAME_5"));
    }

    protected static List<Map<KeysMetaData, Object>> table6Fks() {
        return List.of(
                createKeysTestData("TABLE_2", "ID1", "TABLE_6", "TABLE_2_ID1", 1,
                        DatabaseMetaData.importedKeyCascade, DatabaseMetaData.importedKeyNoAction, "PK_TABLE_2",
                        UNNAMED_CONSTRAINT_PREFIX, "ALT_INDEX_NAME_2", UNNAMED_FK_INDEX_PREFIX),
                createKeysTestData("TABLE_2", "ID2", "TABLE_6", "TABLE_2_ID2", 2,
                        DatabaseMetaData.importedKeyCascade, DatabaseMetaData.importedKeyNoAction, "PK_TABLE_2",
                        UNNAMED_CONSTRAINT_PREFIX, "ALT_INDEX_NAME_2", UNNAMED_FK_INDEX_PREFIX));
    }

    protected static List<Map<KeysMetaData, Object>> table7Fks() {
        return List.of(
                createKeysTestData("TABLE_6", "ID", "TABLE_7", "TABLE_6_ID", 1, DatabaseMetaData.importedKeyCascade,
                        DatabaseMetaData.importedKeyNoAction, UNNAMED_CONSTRAINT_PREFIX, "FK_TABLE_7_TO_6",
                        UNNAMED_PK_INDEX_PREFIX, "FK_TABLE_7_TO_6"));
    }

    protected void validateExpectedKeys(ResultSet keys, List<Map<KeysMetaData, Object>> expectedKeys)
            throws Exception {
        for (Map<KeysMetaData, Object> expectedColumn : expectedKeys) {
            assertNextRow(keys);
            keysDefinition.validateRowValues(keys, expectedColumn);
        }
        assertNoNextRow(keys);
    }

    protected static Map<KeysMetaData, Object> createKeysTestData(String pkTable, String pkColumn, String fkTable,
            String fkColumn, int keySeq, int updateRule, int deleteRule, String pkName, String fkName,
            String pkIndexName, String fkIndexName) {
        Map<KeysMetaData, Object> rules = getDefaultValidationRules();
        rules.put(KeysMetaData.PKTABLE_NAME, pkTable);
        rules.put(KeysMetaData.PKCOLUMN_NAME, pkColumn);
        rules.put(KeysMetaData.FKTABLE_NAME, fkTable);
        rules.put(KeysMetaData.FKCOLUMN_NAME, fkColumn);
        rules.put(KeysMetaData.KEY_SEQ, (short) keySeq);
        rules.put(KeysMetaData.UPDATE_RULE, (short) updateRule);
        rules.put(KeysMetaData.DELETE_RULE, (short) deleteRule);
        rules.put(KeysMetaData.FK_NAME, constraintNameValidation(fkName));
        rules.put(KeysMetaData.PK_NAME, constraintNameValidation(pkName));
        rules.put(KeysMetaData.JB_FK_INDEX_NAME, UNNAMED_FK_INDEX_PREFIX.equals(fkIndexName)
                ? Matchers.startsWith(UNNAMED_FK_INDEX_PREFIX) : fkIndexName);
        rules.put(KeysMetaData.JB_PK_INDEX_NAME, UNNAMED_PK_INDEX_PREFIX.equals(pkIndexName)
                ? Matchers.startsWith(UNNAMED_PK_INDEX_PREFIX) : pkIndexName);
        return rules;
    }

    private static Object constraintNameValidation(String constraintName) {
        return UNNAMED_CONSTRAINT_PREFIX.equals(constraintName)
                ? Matchers.startsWith(UNNAMED_CONSTRAINT_PREFIX) : constraintName;
    }

    private static final Map<KeysMetaData, Object> DEFAULT_COLUMN_VALUES;

    static {
        var defaults = new EnumMap<>(KeysMetaData.class);
        Arrays.stream(KeysMetaData.values()).forEach(key -> defaults.put(key, null));
        defaults.put(KeysMetaData.UPDATE_RULE, DatabaseMetaData.importedKeyNoAction);
        defaults.put(KeysMetaData.DELETE_RULE, DatabaseMetaData.importedKeyNoAction);
        defaults.put(KeysMetaData.DEFERRABILITY, DatabaseMetaData.importedKeyNotDeferrable);
        DEFAULT_COLUMN_VALUES = unmodifiableMap(defaults);
    }

    protected static Map<KeysMetaData, Object> getDefaultValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    enum KeysMetaData implements MetaDataInfo {
        PKTABLE_CAT(1, String.class),
        PKTABLE_SCHEM(2, String.class),
        PKTABLE_NAME(3, String.class),
        PKCOLUMN_NAME(4, String.class),
        FKTABLE_CAT(5, String.class),
        FKTABLE_SCHEM(6, String.class),
        FKTABLE_NAME(7, String.class),
        FKCOLUMN_NAME(8, String.class),
        KEY_SEQ(9, Short.class),
        UPDATE_RULE(10, Short.class),
        DELETE_RULE(11, Short.class),
        FK_NAME(12, String.class),
        PK_NAME(13, String.class),
        DEFERRABILITY(14, Short.class),
        JB_FK_INDEX_NAME(15, String.class),
        JB_PK_INDEX_NAME(16, String.class),
        ;

        private final int position;
        private final Class<?> columnClass;

        KeysMetaData(int position, Class<?> columnClass) {
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
