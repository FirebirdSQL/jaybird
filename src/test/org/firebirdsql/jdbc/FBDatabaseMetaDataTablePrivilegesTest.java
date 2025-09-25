// SPDX-FileCopyrightText: Copyright 2023-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.ifSchemaElse;
import static org.firebirdsql.common.FBTestProperties.resolveSchema;
import static org.firebirdsql.common.FbAssumptions.assumeFeature;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link FBDatabaseMetaData#getTablePrivileges(String, String, String)}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataTablePrivilegesTest {

    private static final String SYSDBA = "SYSDBA";
    private static final String USER1 = "USER1";
    private static final String user2 = getDefaultSupportInfo().supportsCaseSensitiveUserNames() ? "user2" : "USER2";
    private static final String PUBLIC = "PUBLIC";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase =
            UsesDatabaseExtension.usesDatabaseForAll(createDbInitStatements());

    private static final MetadataResultSetDefinition getTablePrivilegesDefinition =
            new MetadataResultSetDefinition(TablePrivilegesMetadata.class);

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        // Otherwise we need to take into account additional rules
        assumeThat("Expects test user to be SYSDBA", FBTestProperties.DB_USER, equalToIgnoringCase(SYSDBA));
        con = getConnectionViaDriverManager();
        dbmd = con.getMetaData();
    }

    private static List<String> createDbInitStatements() {
        var statements = new ArrayList<>(List.of(
                "create table TBL1 (COL1 integer, COL2 varchar(50), \"val3\" varchar(50))",
                "create table \"tbl2\" (COL1 integer, COL2 varchar(50), \"val3\" varchar(50))",
                "grant all on TBL1 to USER1",
                "grant select on TBL1 to PUBLIC",
                "grant update (COL1, \"val3\") on TBL1 to \"user2\"",
                "grant select on \"tbl2\" to \"user2\" with grant option",
                "grant references (COL1) on \"tbl2\" to USER1"));
        if (getDefaultSupportInfo().supportsSchemas()) {
            statements.addAll(List.of(
                    "create schema OTHER_SCHEMA",
                    "create table OTHER_SCHEMA.TBL3 (COL1 integer, COL2 varchar(50))",
                    "grant all on OTHER_SCHEMA.TBL3 to USER1",
                    "grant select on OTHER_SCHEMA.TBL3 to \"user2\""));
        }

        return statements;
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
     * Tests the ordinal positions and types for the metadata columns of getTablePrivileges().
     */
    @Test
    void testTablePrivilegesMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getTablePrivileges(null, null, "doesnotexist")) {
            getTablePrivilegesDefinition.validateResultSetColumns(columns);
        }
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schemaPattern, tableNamePattern
            <NIL>,         TBL1
            %,             TBL1
            PUBLIC,        TBL1
            #NOTE: Only works because there is no other TBL_ in default schema
            PUBLIC,        TBL_
            """)
    void testTablePrivileges_TBL1_all(String schemaPattern, String tableNamePattern) throws Exception {
        List<Map<TablePrivilegesMetadata, Object>> rules = getTBL1_all();

        validateExpectedColumnPrivileges(schemaPattern, tableNamePattern, rules);
    }

    private List<Map<TablePrivilegesMetadata, Object>> getTBL1_all() {
        return List.of(
                createRule("TBL1", SYSDBA, true, "DELETE"),
                createRule("TBL1", USER1, false, "DELETE"),
                createRule("TBL1", SYSDBA, true, "INSERT"),
                createRule("TBL1", USER1, false, "INSERT"),
                createRule("TBL1", SYSDBA, true, "REFERENCES"),
                createRule("TBL1", USER1, false, "REFERENCES"),
                createRule("TBL1", PUBLIC, false, "SELECT"),
                createRule("TBL1", SYSDBA, true, "SELECT"),
                createRule("TBL1", USER1, false, "SELECT"),
                createRule("TBL1", SYSDBA, true, "UPDATE"),
                createRule("TBL1", USER1, false, "UPDATE"),
                createRule("TBL1", user2, false, "UPDATE"));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schemaPattern, tableNamePattern
            <NIL>,         tbl2
            %,             tbl2
            PUBLIC,        tbl2
            #NOTE: Only works because there is no other tbl_ in default schema
            PUBLIC,        tbl_
            """)
    void testColumnPrivileges_tbl2_all(String schemaPattern, String tableNamePattern) throws Exception {
        List<Map<TablePrivilegesMetadata, Object>> rules = getTbl2_all();

        validateExpectedColumnPrivileges(schemaPattern, tableNamePattern, rules);
    }

    private List<Map<TablePrivilegesMetadata, Object>> getTbl2_all() {
        return List.of(
                createRule("tbl2", SYSDBA, true, "DELETE"),
                createRule("tbl2", SYSDBA, true, "INSERT"),
                createRule("tbl2", SYSDBA, true, "REFERENCES"),
                createRule("tbl2", USER1, false, "REFERENCES"),
                createRule("tbl2", SYSDBA, true, "SELECT"),
                createRule("tbl2", user2, true, "SELECT"),
                createRule("tbl2", SYSDBA, true, "UPDATE"));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schemaPattern,  tableNamePattern
            <NIL>,          TBL3
            %,              TBL3
            OTHER_SCHEMA,   TBL3
            OTHER\\_SCHEMA, TBL3
            OTHER%,         TBL3
            #NOTE: Only works because there is no other TBL_ in OTHER_SCHEMA
            OTHER_SCHEMA,   TBL_
            """)
    void testColumnPrivileges_otherSchemaTBL3_all(String schemaPattern, String tableNamePattern) throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsSchemas, "Test requires schema support");
        List<Map<TablePrivilegesMetadata, Object>> rules = getTBL3_all();

        validateExpectedColumnPrivileges(schemaPattern, tableNamePattern, rules);
    }

    private List<Map<TablePrivilegesMetadata, Object>> getTBL3_all() {
        return List.of(
                createRule("OTHER_SCHEMA", "TBL3", SYSDBA, true, "DELETE"),
                createRule("OTHER_SCHEMA", "TBL3", USER1, false, "DELETE"),
                createRule("OTHER_SCHEMA", "TBL3", SYSDBA, true, "INSERT"),
                createRule("OTHER_SCHEMA", "TBL3", USER1, false, "INSERT"),
                createRule("OTHER_SCHEMA", "TBL3", SYSDBA, true, "REFERENCES"),
                createRule("OTHER_SCHEMA", "TBL3", USER1, false, "REFERENCES"),
                createRule("OTHER_SCHEMA", "TBL3", SYSDBA, true, "SELECT"),
                createRule("OTHER_SCHEMA", "TBL3", USER1, false, "SELECT"),
                createRule("OTHER_SCHEMA", "TBL3", user2, false, "SELECT"),
                createRule("OTHER_SCHEMA", "TBL3", SYSDBA, true, "UPDATE"),
                createRule("OTHER_SCHEMA", "TBL3", USER1, false, "UPDATE"));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schemaPattern,  tableNamePattern
            <NIL>,          <NIL>
            %,              <NIL>
            <NIL>,          %
            %,              %
            """)
    void testColumnPrivileges_all(String schemaPattern, String tableNamePattern) throws Exception {
        var rules = new ArrayList<Map<TablePrivilegesMetadata, Object>>();
        if (getDefaultSupportInfo().supportsSchemas()) {
            rules.addAll(getTBL3_all());
        }
        rules.addAll(getTBL1_all());
        rules.addAll(getTbl2_all());

        validateExpectedColumnPrivileges(schemaPattern, tableNamePattern, rules);
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schemaPattern, tableNamePattern
            PUBLIC,        <NIL>
            PUBLIC,        %
            """)
    void testColumnPrivileges_defaultSchema_all(String schemaPattern, String tableNamePattern) throws Exception {
        var rules = new ArrayList<Map<TablePrivilegesMetadata, Object>>();
        rules.addAll(getTBL1_all());
        rules.addAll(getTbl2_all());

        validateExpectedColumnPrivileges(schemaPattern, tableNamePattern, rules);
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = "<NIL>", textBlock = """
            schemaPattern,  tableNamePattern
            OTHER_SCHEMA,   <NIL>
            OTHER\\_SCHEMA, %
            OTHER%,         <NIL>
            """)
    void testColumnPrivileges_otherSchema_all(String schemaPattern, String tableNamePattern) throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsSchemas, "Test requires schema support");
        List<Map<TablePrivilegesMetadata, Object>> rules = getTBL3_all();

        validateExpectedColumnPrivileges(schemaPattern, tableNamePattern, rules);
    }

    private Map<TablePrivilegesMetadata, Object> createRule(String tableName, String grantee, boolean grantable,
            String privilege) {
        return createRule(ifSchemaElse("PUBLIC", null), tableName, grantee, grantable, privilege);
    }

    private Map<TablePrivilegesMetadata, Object> createRule(String schema, String tableName, String grantee,
            boolean grantable, String privilege) {
        Map<TablePrivilegesMetadata, Object> rules = getDefaultValueValidationRules();
        rules.put(TablePrivilegesMetadata.TABLE_SCHEM, schema);
        rules.put(TablePrivilegesMetadata.TABLE_NAME, tableName);
        rules.put(TablePrivilegesMetadata.GRANTEE, grantee);
        rules.put(TablePrivilegesMetadata.PRIVILEGE, privilege);
        rules.put(TablePrivilegesMetadata.IS_GRANTABLE, grantable ? "YES" : "NO");
        return rules;
    }

    private void validateExpectedColumnPrivileges(String schemaPattern, String tableNamePattern,
            List<Map<TablePrivilegesMetadata, Object>> expectedTablePrivileges) throws SQLException {
        try (ResultSet tablePrivileges = dbmd.getTablePrivileges(null, resolveSchema(schemaPattern), tableNamePattern)) {
            int privilegeCount = 0;
            while (tablePrivileges.next()) {
                if (isProbablySystemTable(tablePrivileges.getString("TABLE_SCHEM"),
                        tablePrivileges.getString("TABLE_NAME"))) {
                    // skip system tables
                    continue;
                }
                if (privilegeCount < expectedTablePrivileges.size()) {
                    Map<TablePrivilegesMetadata, Object> rules = expectedTablePrivileges.get(privilegeCount);
                    getTablePrivilegesDefinition.checkValidationRulesComplete(rules);
                    getTablePrivilegesDefinition.validateRowValues(tablePrivileges, rules);
                }
                privilegeCount++;
            }
            assertEquals(expectedTablePrivileges.size(), privilegeCount, "Unexpected number of table privileges");
        }
    }

    private static boolean isProbablySystemTable(String schema, String tableName) {
        return "SYSTEM".equals(schema)
                || tableName.startsWith("RDB$")
                || tableName.startsWith("MON$")
                || tableName.startsWith("SEC$");
    }

    private static final Map<TablePrivilegesMetadata, Object> DEFAULT_TABLE_PRIVILEGES_VALUES;
    static {
        Map<TablePrivilegesMetadata, Object> defaults = new EnumMap<>(TablePrivilegesMetadata.class);
        defaults.put(TablePrivilegesMetadata.TABLE_CAT, null);
        defaults.put(TablePrivilegesMetadata.TABLE_SCHEM, ifSchemaElse("PUBLIC", null));
        defaults.put(TablePrivilegesMetadata.GRANTOR, SYSDBA);
        defaults.put(TablePrivilegesMetadata.JB_GRANTEE_TYPE, "USER");
        defaults.put(TablePrivilegesMetadata.JB_GRANTEE_SCHEMA, null);

        DEFAULT_TABLE_PRIVILEGES_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<TablePrivilegesMetadata, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_TABLE_PRIVILEGES_VALUES);
    }

    private enum TablePrivilegesMetadata implements MetaDataInfo {
        TABLE_CAT(1),
        TABLE_SCHEM(2),
        TABLE_NAME(3),
        GRANTOR(4),
        GRANTEE(5),
        PRIVILEGE(6),
        IS_GRANTABLE(7),
        JB_GRANTEE_TYPE(8),
        JB_GRANTEE_SCHEMA(9);

        private final int position;

        TablePrivilegesMetadata(int position) {
            this.position = position;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public Class<?> getColumnClass() {
            return String.class;
        }
    }
}
