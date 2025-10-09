// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.ifSchemaElse;
import static org.firebirdsql.common.FbAssumptions.assumeSchemaSupport;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNoNextRow;

class FBDatabaseMetaDataBestRowIdentifierTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getInitStatements());

    private static final String TABLE_BEST_ROW_PK = """
            create table BEST_ROW_PK (
              C1 integer constraint PK_BEST_ROW_PK primary key
            )""";

    private static final String TABLE_BEST_ROW_NO_PK = """
            create table BEST_ROW_NO_PK (
              C1 integer not null
            )""";

    private static final String CREATE_OTHER_SCHEMA = "create schema OTHER_SCHEMA";

    private static final String TABLE_BEST_ROW_PK_OTHER_SCHEMA = """
            create table OTHER_SCHEMA.BEST_ROW_PK (
              ID1 integer not null,
              ID2 bigint not null,
              constraint PK_BEST_ROW_PK primary key (ID1, ID2)
            )""";

    private static final MetadataResultSetDefinition getBestRowIdentifierDefinition =
            new MetadataResultSetDefinition(BestRowIdentifierMetaData.class);

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

    private static List<String> getInitStatements() {
        var statements = new ArrayList<>(
                List.of(TABLE_BEST_ROW_PK,
                        TABLE_BEST_ROW_NO_PK));
        if (getDefaultSupportInfo().supportsSchemas()) {
            statements.add(CREATE_OTHER_SCHEMA);
            statements.add(TABLE_BEST_ROW_PK_OTHER_SCHEMA);
        }
        return statements;
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of getBestRowIdentifier(...).
     */
    @Test
    void testSchemaMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getBestRowIdentifier("", "", "", DatabaseMetaData.bestRowTransaction, true)) {
            getBestRowIdentifierDefinition.validateResultSetColumns(columns);
        }
    }

    @Test
    void testGetBestRowIdentifier() throws Exception {
        for (int scope : new int[] { DatabaseMetaData.bestRowTemporary, DatabaseMetaData.bestRowTransaction,
                DatabaseMetaData.bestRowSession }) {
            try (ResultSet rs = dbmd.getBestRowIdentifier("", ifSchemaElse("PUBLIC", ""), "BEST_ROW_PK", scope, true)) {
                validate(rs, rules_BEST_ROW_PK());
            }
        }

        for (int scope : new int[] { DatabaseMetaData.bestRowTemporary, DatabaseMetaData.bestRowTransaction }) {
            try (ResultSet rs = dbmd.getBestRowIdentifier(
                    "", ifSchemaElse("PUBLIC", ""), "BEST_ROW_NO_PK", scope, true)) {
                validate(rs, rules_BEST_ROW_NO_PK());
            }
        }

        try (ResultSet rs = dbmd.getBestRowIdentifier(
                "", ifSchemaElse("PUBLIC", ""), "BEST_ROW_NO_PK", DatabaseMetaData.bestRowSession, true)) {
            validate(rs, List.of());
        }
    }

    @Test
    void testGetBestRowIdentifier_otherSchema() throws Exception {
        assumeSchemaSupport();
        for (int scope : new int[] { DatabaseMetaData.bestRowTemporary, DatabaseMetaData.bestRowTransaction,
                DatabaseMetaData.bestRowSession }) {
            try (ResultSet rs = dbmd.getBestRowIdentifier("", "OTHER_SCHEMA", "BEST_ROW_PK", scope, true)) {
                validate(rs, rules_BEST_ROW_PK_OTHER_SCHEMA());
            }
        }
    }

    @Test
    void testGetBestRowIdentifier_allSchemas() throws Exception {
        assumeSchemaSupport();
        /* JDBC specifies that "null means that the schema name should not be used to narrow the search". This seems
        like useless behaviour to me for this method (you don't know to which table the columns are actually referring),
        but let's verify it (our implementation returns the columns of all tables with the same name, ordered by schema
        and field position). */
        for (int scope : new int[] { DatabaseMetaData.bestRowTemporary, DatabaseMetaData.bestRowTransaction,
                DatabaseMetaData.bestRowSession }) {
            try (ResultSet rs = dbmd.getBestRowIdentifier("", null, "BEST_ROW_PK", scope, true)) {
                var combinedRules = new ArrayList<Map<BestRowIdentifierMetaData, Object>>();
                combinedRules.addAll(rules_BEST_ROW_PK_OTHER_SCHEMA());
                combinedRules.addAll(rules_BEST_ROW_PK());
                validate(rs, combinedRules);
            }
        }
    }

    private static void validate(ResultSet rs, List<Map<BestRowIdentifierMetaData, Object>> rules) throws SQLException {
        for (Map<BestRowIdentifierMetaData, Object> rowRule : rules) {
            assertNextRow(rs);
            getBestRowIdentifierDefinition.validateRowValues(rs, rowRule);
        }
        assertNoNextRow(rs);
    }

    private static List<Map<BestRowIdentifierMetaData, Object>> rules_BEST_ROW_PK() {
        Map<BestRowIdentifierMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(BestRowIdentifierMetaData.SCOPE, DatabaseMetaData.bestRowSession);
        rules.put(BestRowIdentifierMetaData.COLUMN_NAME, "C1");
        rules.put(BestRowIdentifierMetaData.DATA_TYPE, Types.INTEGER);
        rules.put(BestRowIdentifierMetaData.TYPE_NAME, "INTEGER");
        rules.put(BestRowIdentifierMetaData.COLUMN_SIZE, 10);
        rules.put(BestRowIdentifierMetaData.DECIMAL_DIGITS, 0);
        rules.put(BestRowIdentifierMetaData.PSEUDO_COLUMN, DatabaseMetaData.bestRowNotPseudo);
        return List.of(rules);
    }

    private static List<Map<BestRowIdentifierMetaData, Object>> rules_BEST_ROW_NO_PK() {
        Map<BestRowIdentifierMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(BestRowIdentifierMetaData.SCOPE, DatabaseMetaData.bestRowTransaction);
        rules.put(BestRowIdentifierMetaData.COLUMN_NAME, "RDB$DB_KEY");
        rules.put(BestRowIdentifierMetaData.DATA_TYPE, Types.ROWID);
        rules.put(BestRowIdentifierMetaData.TYPE_NAME, "CHAR");
        rules.put(BestRowIdentifierMetaData.COLUMN_SIZE, 8);
        rules.put(BestRowIdentifierMetaData.DECIMAL_DIGITS, null);
        rules.put(BestRowIdentifierMetaData.PSEUDO_COLUMN, DatabaseMetaData.bestRowPseudo);
        return List.of(rules);
    }

    private static List<Map<BestRowIdentifierMetaData, Object>> rules_BEST_ROW_PK_OTHER_SCHEMA() {
        Map<BestRowIdentifierMetaData, Object> rulesRow1 = getDefaultValueValidationRules();
        rulesRow1.put(BestRowIdentifierMetaData.SCOPE, DatabaseMetaData.bestRowSession);
        rulesRow1.put(BestRowIdentifierMetaData.COLUMN_NAME, "ID1");
        rulesRow1.put(BestRowIdentifierMetaData.DATA_TYPE, Types.INTEGER);
        rulesRow1.put(BestRowIdentifierMetaData.TYPE_NAME, "INTEGER");
        rulesRow1.put(BestRowIdentifierMetaData.COLUMN_SIZE, 10);
        rulesRow1.put(BestRowIdentifierMetaData.DECIMAL_DIGITS, 0);
        rulesRow1.put(BestRowIdentifierMetaData.PSEUDO_COLUMN, DatabaseMetaData.bestRowNotPseudo);

        Map<BestRowIdentifierMetaData, Object> rulesRow2 = getDefaultValueValidationRules();
        rulesRow2.put(BestRowIdentifierMetaData.SCOPE, DatabaseMetaData.bestRowSession);
        rulesRow2.put(BestRowIdentifierMetaData.COLUMN_NAME, "ID2");
        rulesRow2.put(BestRowIdentifierMetaData.DATA_TYPE, Types.BIGINT);
        rulesRow2.put(BestRowIdentifierMetaData.TYPE_NAME, "BIGINT");
        rulesRow2.put(BestRowIdentifierMetaData.COLUMN_SIZE, 19);
        rulesRow2.put(BestRowIdentifierMetaData.DECIMAL_DIGITS, 0);
        rulesRow2.put(BestRowIdentifierMetaData.PSEUDO_COLUMN, DatabaseMetaData.bestRowNotPseudo);
        return List.of(rulesRow1, rulesRow2);
    }

    private static final Map<BestRowIdentifierMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        var defaults = new EnumMap<>(BestRowIdentifierMetaData.class);
        defaults.put(BestRowIdentifierMetaData.BUFFER_LENGTH, null);

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<BestRowIdentifierMetaData, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    private enum BestRowIdentifierMetaData implements MetaDataInfo {
        SCOPE(1, Short.class),
        COLUMN_NAME(2, String.class),
        DATA_TYPE(3, Integer.class),
        TYPE_NAME(4, String.class),
        COLUMN_SIZE(5, Integer.class),
        BUFFER_LENGTH(6, Integer.class),
        DECIMAL_DIGITS(7, Short.class),
        PSEUDO_COLUMN(8, Short.class),
        ;

        private final int position;
        private final Class<?> columnClass;

        BestRowIdentifierMetaData(int position, Class<?> columnClass) {
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
