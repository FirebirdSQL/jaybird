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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link FBDatabaseMetaData#getColumnPrivileges(String, String, String, String)}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataColumnPrivilegesTest {

    private static final String SYSDBA = "SYSDBA";
    private static final String USER1 = "USER1";
    private static final String user2 = getDefaultSupportInfo().supportsCaseSensitiveUserNames() ? "user2" : "USER2";
    private static final String PUBLIC = "PUBLIC";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            "create table TBL1 (COL1 integer, COL2 varchar(50), \"val3\" varchar(50))",
            "create table \"tbl2\" (COL1 integer, COL2 varchar(50), \"val3\" varchar(50))",
            "grant all on TBL1 to USER1",
            "grant select on TBL1 to PUBLIC",
            "grant update (COL1, \"val3\") on TBL1 to \"user2\"",
            "grant select on \"tbl2\" to \"user2\" with grant option",
            "grant references (COL1) on \"tbl2\" to USER1");

    private static final MetadataResultSetDefinition getColumnPrivilegesDefinition =
            new MetadataResultSetDefinition(ColumnPrivilegesMetadata.class);

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        // Otherwise we need to take into account additional rules
        assumeThat("Expects test user to be SYSDBA", FBTestProperties.DB_USER, equalToIgnoringCase(SYSDBA));
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
     * Tests the ordinal positions and types for the metadata columns of getColumnPrivileges().
     */
    @Test
    void testColumnPrivilegesMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getColumnPrivileges(null, null, "doesnotexist", null)) {
            getColumnPrivilegesDefinition.validateResultSetColumns(columns);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = "%")
    @NullSource
    void testColumnPrivileges_TBL1_all(String allPattern) throws Exception {
        List<Map<ColumnPrivilegesMetadata, Object>> rules = Arrays.asList(
                createRule("TBL1", "COL1", SYSDBA, true, "DELETE"),
                createRule("TBL1", "COL1", USER1, false, "DELETE"),
                createRule("TBL1", "COL1", SYSDBA, true, "INSERT"),
                createRule("TBL1", "COL1", USER1, false, "INSERT"),
                createRule("TBL1", "COL1", SYSDBA, true, "REFERENCES"),
                createRule("TBL1", "COL1", USER1, false, "REFERENCES"),
                createRule("TBL1", "COL1", PUBLIC, false, "SELECT"),
                createRule("TBL1", "COL1", SYSDBA, true, "SELECT"),
                createRule("TBL1", "COL1", USER1, false, "SELECT"),
                createRule("TBL1", "COL1", SYSDBA, true, "UPDATE"),
                createRule("TBL1", "COL1", USER1, false, "UPDATE"),
                createRule("TBL1", "COL1", user2, false, "UPDATE"),
                createRule("TBL1", "COL2", SYSDBA, true, "DELETE"),
                createRule("TBL1", "COL2", USER1, false, "DELETE"),
                createRule("TBL1", "COL2", SYSDBA, true, "INSERT"),
                createRule("TBL1", "COL2", USER1, false, "INSERT"),
                createRule("TBL1", "COL2", SYSDBA, true, "REFERENCES"),
                createRule("TBL1", "COL2", USER1, false, "REFERENCES"),
                createRule("TBL1", "COL2", PUBLIC, false, "SELECT"),
                createRule("TBL1", "COL2", SYSDBA, true, "SELECT"),
                createRule("TBL1", "COL2", USER1, false, "SELECT"),
                createRule("TBL1", "COL2", SYSDBA, true, "UPDATE"),
                createRule("TBL1", "COL2", USER1, false, "UPDATE"),
                createRule("TBL1", "val3", SYSDBA, true, "DELETE"),
                createRule("TBL1", "val3", USER1, false, "DELETE"),
                createRule("TBL1", "val3", SYSDBA, true, "INSERT"),
                createRule("TBL1", "val3", USER1, false, "INSERT"),
                createRule("TBL1", "val3", SYSDBA, true, "REFERENCES"),
                createRule("TBL1", "val3", USER1, false, "REFERENCES"),
                createRule("TBL1", "val3", PUBLIC, false, "SELECT"),
                createRule("TBL1", "val3", SYSDBA, true, "SELECT"),
                createRule("TBL1", "val3", USER1, false, "SELECT"),
                createRule("TBL1", "val3", SYSDBA, true, "UPDATE"),
                createRule("TBL1", "val3", USER1, false, "UPDATE"),
                createRule("TBL1", "val3", user2, false, "UPDATE"));

        validateExpectedColumnPrivileges("TBL1", allPattern, rules);
    }

    @Test
    void testColumnPrivileges_TBL1_COL_wildcard() throws Exception {
        List<Map<ColumnPrivilegesMetadata, Object>> rules = Arrays.asList(
                createRule("TBL1", "COL1", SYSDBA, true, "DELETE"),
                createRule("TBL1", "COL1", USER1, false, "DELETE"),
                createRule("TBL1", "COL1", SYSDBA, true, "INSERT"),
                createRule("TBL1", "COL1", USER1, false, "INSERT"),
                createRule("TBL1", "COL1", SYSDBA, true, "REFERENCES"),
                createRule("TBL1", "COL1", USER1, false, "REFERENCES"),
                createRule("TBL1", "COL1", PUBLIC, false, "SELECT"),
                createRule("TBL1", "COL1", SYSDBA, true, "SELECT"),
                createRule("TBL1", "COL1", USER1, false, "SELECT"),
                createRule("TBL1", "COL1", SYSDBA, true, "UPDATE"),
                createRule("TBL1", "COL1", USER1, false, "UPDATE"),
                createRule("TBL1", "COL1", user2, false, "UPDATE"),
                createRule("TBL1", "COL2", SYSDBA, true, "DELETE"),
                createRule("TBL1", "COL2", USER1, false, "DELETE"),
                createRule("TBL1", "COL2", SYSDBA, true, "INSERT"),
                createRule("TBL1", "COL2", USER1, false, "INSERT"),
                createRule("TBL1", "COL2", SYSDBA, true, "REFERENCES"),
                createRule("TBL1", "COL2", USER1, false, "REFERENCES"),
                createRule("TBL1", "COL2", PUBLIC, false, "SELECT"),
                createRule("TBL1", "COL2", SYSDBA, true, "SELECT"),
                createRule("TBL1", "COL2", USER1, false, "SELECT"),
                createRule("TBL1", "COL2", SYSDBA, true, "UPDATE"),
                createRule("TBL1", "COL2", USER1, false, "UPDATE"));

        validateExpectedColumnPrivileges("TBL1", "COL%", rules);
    }

    @Test
    void testColumnPrivileges_tbl2_all() throws Exception {
        List<Map<ColumnPrivilegesMetadata, Object>> rules = Arrays.asList(
                createRule("tbl2", "COL1", SYSDBA, true, "DELETE"),
                createRule("tbl2", "COL1", SYSDBA, true, "INSERT"),
                createRule("tbl2", "COL1", SYSDBA, true, "REFERENCES"),
                createRule("tbl2", "COL1", USER1, false, "REFERENCES"),
                createRule("tbl2", "COL1", SYSDBA, true, "SELECT"),
                createRule("tbl2", "COL1", user2, true, "SELECT"),
                createRule("tbl2", "COL1", SYSDBA, true, "UPDATE"),
                createRule("tbl2", "COL2", SYSDBA, true, "DELETE"),
                createRule("tbl2", "COL2", SYSDBA, true, "INSERT"),
                createRule("tbl2", "COL2", SYSDBA, true, "REFERENCES"),
                createRule("tbl2", "COL2", SYSDBA, true, "SELECT"),
                createRule("tbl2", "COL2", user2, true, "SELECT"),
                createRule("tbl2", "COL2", SYSDBA, true, "UPDATE"),
                createRule("tbl2", "val3", SYSDBA, true, "DELETE"),
                createRule("tbl2", "val3", SYSDBA, true, "INSERT"),
                createRule("tbl2", "val3", SYSDBA, true, "REFERENCES"),
                createRule("tbl2", "val3", SYSDBA, true, "SELECT"),
                createRule("tbl2", "val3", user2, true, "SELECT"),
                createRule("tbl2", "val3", SYSDBA, true, "UPDATE"));

        validateExpectedColumnPrivileges("tbl2", "%", rules);
    }

    private Map<ColumnPrivilegesMetadata, Object> createRule(String tableName, String columnName, String grantee,
            boolean grantable, String privilege) {
        Map<ColumnPrivilegesMetadata, Object> rules = getDefaultValueValidationRules();
        rules.put(ColumnPrivilegesMetadata.TABLE_NAME, tableName);
        rules.put(ColumnPrivilegesMetadata.COLUMN_NAME, columnName);
        rules.put(ColumnPrivilegesMetadata.GRANTEE, grantee);
        rules.put(ColumnPrivilegesMetadata.PRIVILEGE, privilege);
        rules.put(ColumnPrivilegesMetadata.IS_GRANTABLE, grantable ? "YES" : "NO");
        return rules;
    }

    private void validateExpectedColumnPrivileges(String tableName, String columnNamePattern,
            List<Map<ColumnPrivilegesMetadata, Object>> expectedColumnPrivileges) throws SQLException {
        try (ResultSet columnPrivileges = dbmd.getColumnPrivileges(null, null, tableName, columnNamePattern)) {
            int privilegeCount = 0;
            while (columnPrivileges.next()) {
                if (privilegeCount < expectedColumnPrivileges.size()) {
                    Map<ColumnPrivilegesMetadata, Object> rules = expectedColumnPrivileges.get(privilegeCount);
                    getColumnPrivilegesDefinition.checkValidationRulesComplete(rules);
                    getColumnPrivilegesDefinition.validateRowValues(columnPrivileges, rules);
                }
                privilegeCount++;
            }
            assertEquals(expectedColumnPrivileges.size(), privilegeCount, "Unexpected number of column privileges");
        }
    }

    private static final Map<ColumnPrivilegesMetadata, Object> DEFAULT_COLUMN_PRIVILEGES_VALUES;
    static {
        Map<ColumnPrivilegesMetadata, Object> defaults = new EnumMap<>(ColumnPrivilegesMetadata.class);
        defaults.put(ColumnPrivilegesMetadata.TABLE_CAT, null);
        defaults.put(ColumnPrivilegesMetadata.TABLE_SCHEM, null);
        defaults.put(ColumnPrivilegesMetadata.GRANTOR, SYSDBA);
        defaults.put(ColumnPrivilegesMetadata.JB_GRANTEE_TYPE, "USER");

        DEFAULT_COLUMN_PRIVILEGES_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<ColumnPrivilegesMetadata, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_PRIVILEGES_VALUES);
    }

    private enum ColumnPrivilegesMetadata implements MetaDataInfo {
        TABLE_CAT(1),
        TABLE_SCHEM(2),
        TABLE_NAME(3),
        COLUMN_NAME(4),
        GRANTOR(5),
        GRANTEE(6),
        PRIVILEGE(7),
        IS_GRANTABLE(8),
        JB_GRANTEE_TYPE(9);

        private final int position;

        ColumnPrivilegesMetadata(int position) {
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
