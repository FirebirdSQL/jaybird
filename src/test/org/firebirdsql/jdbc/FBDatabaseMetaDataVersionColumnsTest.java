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

import org.firebirdsql.common.rules.UsesDatabase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.*;
import java.util.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class FBDatabaseMetaDataVersionColumnsTest {

    //@formatter:off
    private static final String NORMAL_TABLE_NAME = "NORMAL_TABLE";
    private static final String CREATE_NORMAL_TABLE = "create table " + NORMAL_TABLE_NAME + " ( "
            + " ID integer primary key"
            + ")";
    private static final String NORMAL_TABLE2_NAME = "NORMAL_TABLE2";
    private static final String CREATE_NORMAL_TABLE2 = "create table " + NORMAL_TABLE2_NAME + " ( "
            + " ID integer primary key"
            + ")";
    private static final String SINGLE_VIEW_NAME = "SINGLE_VIEW";
    private static final String CREATE_SINGLE_VIEW =
            "create view " + SINGLE_VIEW_NAME + " as select id from " + NORMAL_TABLE_NAME;
    private static final String MULTI_VIEW_NAME = "MULTI_VIEW";
    private static final String CREATE_MULTI_VIEW = "create view " + MULTI_VIEW_NAME + " as "
            + "select a.id as id1, b.id as id2 "
            + "from " + NORMAL_TABLE_NAME + " as a, " + NORMAL_TABLE2_NAME + " as b";
    private static final String EXTERNAL_TABLE_NAME = "EXTERNAL_TABLE";
    private static final String CREATE_EXTERNAL_TABLE = "create table " + EXTERNAL_TABLE_NAME
            + " external file 'test_external_tbl.dat' ( "
            + " ID integer not null"
            + ")";
    private static final String GTT_PRESERVE_NAME = "GTT_PRESERVE";
    private static final String CREATE_GTT_PRESERVE = "create global temporary table " + GTT_PRESERVE_NAME + " ("
            + " ID integer primary key"
            + ") "
            + " on commit preserve rows ";
    private static final String GTT_DELETE_NAME = "GTT_DELETE";
    private static final String CREATE_GTT_DELETE = "create global temporary table " + GTT_DELETE_NAME + " ("
            + " ID integer primary key"
            + ") "
            + " on commit delete rows ";
    //@formatter:on

    private static final MetaDataTestSupport<VersionColumnMetaData> metaDataTestSupport =
            new MetaDataTestSupport<>(VersionColumnMetaData.class);

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase(
            CREATE_NORMAL_TABLE,
            CREATE_NORMAL_TABLE2,
            CREATE_SINGLE_VIEW,
            CREATE_MULTI_VIEW,
            CREATE_EXTERNAL_TABLE,
            CREATE_GTT_PRESERVE,
            CREATE_GTT_DELETE);

    private boolean supportsRecordVersion = getDefaultSupportInfo()
            .supportsRecordVersionPseudoColumn();

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeClass
    public static void setUp() throws SQLException {
        con = getConnectionViaDriverManager();
        dbmd = con.getMetaData();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        try {
            con.close();
        } finally {
            con = null;
            dbmd = null;
        }
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of getVersionColumns().
     */
    @Test
    public void testPseudoColumnsMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getVersionColumns(null, null, "doesnotexist")) {
            metaDataTestSupport.validateResultSetColumns(columns);
        }
    }

    @Test
    public void testNormalTable() throws Exception {
        List<Map<VersionColumnMetaData, Object>> validationRules =
                createStandardValidationRules(NORMAL_TABLE_NAME);

        ResultSet versionColumns = dbmd.getVersionColumns(null, null, NORMAL_TABLE_NAME);
        validate(versionColumns, validationRules);
    }

    @Test
    public void testSingleView() throws Exception {
        List<Map<VersionColumnMetaData, Object>> validationRules =
                createStandardValidationRules(SINGLE_VIEW_NAME);

        ResultSet versionColumns = dbmd.getVersionColumns(null, null, SINGLE_VIEW_NAME);
        validate(versionColumns, validationRules);
    }

    @Test
    public void testMultiView() throws Exception {
        // Multi-table view has no record version
        List<Map<VersionColumnMetaData, Object>> validationRules = Collections.singletonList(
                createDbkeyValidationRules(16));

        ResultSet versionColumns = dbmd.getVersionColumns(null, null, MULTI_VIEW_NAME);
        validate(versionColumns, validationRules);
    }

    @Test
    public void testExternalTable() throws Exception {
        List<Map<VersionColumnMetaData, Object>> validationRules =
                createStandardValidationRules(EXTERNAL_TABLE_NAME);

        ResultSet versionColumns = dbmd.getVersionColumns(null, null, EXTERNAL_TABLE_NAME);
        validate(versionColumns, validationRules);
    }

    @Test
    public void testMonitoringTable() throws Exception {
        assumeTrue("Test requires monitoring tables", getDefaultSupportInfo().supportsMonitoringTables());
        List<Map<VersionColumnMetaData, Object>> validationRules =
                createStandardValidationRules("MON$DATABASE");

        ResultSet versionColumns = dbmd.getVersionColumns(null, null, "MON$DATABASE");
        validate(versionColumns, validationRules);
    }

    @Test
    public void testGttPreserve() throws Exception {
        List<Map<VersionColumnMetaData, Object>> validationRules =
                createStandardValidationRules(GTT_PRESERVE_NAME);

        ResultSet versionColumns = dbmd.getVersionColumns(null, null, GTT_PRESERVE_NAME);
        validate(versionColumns, validationRules);
    }

    @Test
    public void testGttDelete_allPseudoColumns() throws Exception {
        List<Map<VersionColumnMetaData, Object>> validationRules =
                createStandardValidationRules(GTT_DELETE_NAME);

        ResultSet versionColumns = dbmd.getVersionColumns(null, null, GTT_DELETE_NAME);
        validate(versionColumns, validationRules);
    }

    @Test
    public void testNoMatchingTables() throws Exception {
        ResultSet versionColumns = dbmd.getVersionColumns(null, null, "ABC");
        validate(versionColumns, Collections.<Map<VersionColumnMetaData, Object>>emptyList());
    }

    @Test
    public void testNullTable() throws Exception {
        ResultSet versionColumns = dbmd.getVersionColumns(null, null, null);
        validate(versionColumns, Collections.<Map<VersionColumnMetaData, Object>>emptyList());
    }

    @Test
    public void testEmptyStringTable() throws Exception {
        ResultSet versionColumns = dbmd.getVersionColumns(null, null, "");
        validate(versionColumns, Collections.<Map<VersionColumnMetaData, Object>>emptyList());
    }

    /**
     * Tests that a table name is not treated as a (non-escaped) like pattern.
     * <p>
     * If the name was treated as a (non-escaped) like pattern, the value of {@code NORMAL%} would return records
     * for {@code NORMAL_TABLE} and {@code NORMAL_TABLE2}.
     * </p>
     */
    @Test
    public void testPatternIsEscaped() throws Exception {
        ResultSet versionColumns = dbmd.getVersionColumns(null, null, "NORMAL%");
        validate(versionColumns, Collections.<Map<VersionColumnMetaData, Object>>emptyList());
    }

    private List<Map<VersionColumnMetaData, Object>> createStandardValidationRules(String tableName) {
        List<Map<VersionColumnMetaData, Object>> validationRules = new ArrayList<>();
        validationRules.add(createDbkeyValidationRules(8));
        if (supportsRecordVersion) {
            validationRules.add(createRecordVersionValidationRules());
        }
        return validationRules;
    }

    private void validate(ResultSet pseudoColumns, List<Map<VersionColumnMetaData, Object>> expectedPseudoColumns)
            throws Exception {
        try {
            int columnCount = 0;
            while (pseudoColumns.next()) {
                if (columnCount < expectedPseudoColumns.size()) {
                    Map<VersionColumnMetaData, Object> rules = expectedPseudoColumns.get(columnCount);
                    metaDataTestSupport.checkValidationRulesComplete(rules);
                    metaDataTestSupport.validateRowValues(pseudoColumns, rules);
                }
                columnCount++;
            }
            assertEquals("Unexpected number of columns", expectedPseudoColumns.size(), columnCount);
        } finally {
            closeQuietly(pseudoColumns);
        }
    }

    private static final Map<VersionColumnMetaData, Object> DEFAULT_COLUMN_VALUES;

    static {
        Map<VersionColumnMetaData, Object> defaults = new EnumMap<>(VersionColumnMetaData.class);
        defaults.put(VersionColumnMetaData.SCOPE, null);
        defaults.put(VersionColumnMetaData.DECIMAL_DIGITS, null);
        defaults.put(VersionColumnMetaData.PSEUDO_COLUMN, (short) DatabaseMetaData.versionColumnPseudo);

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<VersionColumnMetaData, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    private Map<VersionColumnMetaData, Object> createDbkeyValidationRules(int expectedDbKeyLength) {
        Map<VersionColumnMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(VersionColumnMetaData.COLUMN_NAME, "RDB$DB_KEY");
        rules.put(VersionColumnMetaData.DATA_TYPE, Types.ROWID);
        rules.put(VersionColumnMetaData.TYPE_NAME, "CHAR");
        rules.put(VersionColumnMetaData.COLUMN_SIZE, expectedDbKeyLength);
        rules.put(VersionColumnMetaData.BUFFER_LENGTH, expectedDbKeyLength);
        return rules;
    }

    private Map<VersionColumnMetaData, Object> createRecordVersionValidationRules() {
        Map<VersionColumnMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(VersionColumnMetaData.COLUMN_NAME, "RDB$RECORD_VERSION");
        rules.put(VersionColumnMetaData.DATA_TYPE, Types.BIGINT);
        rules.put(VersionColumnMetaData.TYPE_NAME, "BIGINT");
        rules.put(VersionColumnMetaData.COLUMN_SIZE, 19);
        rules.put(VersionColumnMetaData.BUFFER_LENGTH, 8);
        rules.put(VersionColumnMetaData.DECIMAL_DIGITS, (short) 0);
        return rules;
    }

    /**
     * Columns defined for the getPseudoColumns() metadata.
     */
    private enum VersionColumnMetaData implements MetaDataValidator.MetaDataInfo {
        SCOPE(1, Short.class),
        COLUMN_NAME(2, String.class),
        DATA_TYPE(3, Integer.class),
        TYPE_NAME(4, String.class),
        COLUMN_SIZE(5, Integer.class),
        BUFFER_LENGTH(6, Integer.class),
        DECIMAL_DIGITS(7, Short.class),
        PSEUDO_COLUMN(8, Short.class);

        private final int position;
        private final Class<?> columnClass;

        VersionColumnMetaData(int position, Class<?> columnClass) {
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

        @Override
        public MetaDataValidator<?> getValidator() {
            return new MetaDataValidator<>(this);
        }
    }

}
