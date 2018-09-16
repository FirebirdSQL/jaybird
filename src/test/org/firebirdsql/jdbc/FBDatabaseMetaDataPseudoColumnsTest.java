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
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBDatabaseMetaDataPseudoColumnsTest {

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

    private static final MetaDataTestSupport<PseudoColumnMetaData> metaDataTestSupport =
            new MetaDataTestSupport<>(PseudoColumnMetaData.class);

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
     * Tests the ordinal positions and types for the metadata columns of getColumns().
     */
    @Test
    public void testPseudoColumnsMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getPseudoColumns(null, null, "doesnotexist", null)) {
            metaDataTestSupport.validateResultSetColumns(columns);
        }
    }

    @Test
    public void testNormalTable_allPseudoColumns() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules =
                createStandardValidationRules(NORMAL_TABLE_NAME, "NO");

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, NORMAL_TABLE_NAME, "%");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testSingleView_allPseudoColumns() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules =
                createStandardValidationRules(SINGLE_VIEW_NAME, "NO");

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, SINGLE_VIEW_NAME, "%");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testMultiView_allPseudoColumns() throws Exception {
        // Multi-table view has no record version
        List<Map<PseudoColumnMetaData, Object>> validationRules = Collections.singletonList(
                createDbkeyValidationRules(MULTI_VIEW_NAME, 16));

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, MULTI_VIEW_NAME, "%");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testExternalTable_allPseudoColumns() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules =
                createStandardValidationRules(EXTERNAL_TABLE_NAME, "YES");

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, EXTERNAL_TABLE_NAME, "%");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testMonitoringTable_allPseudoColumns() throws Exception {
        assumeTrue("Test requires monitoring tables", getDefaultSupportInfo().supportsMonitoringTables());
        List<Map<PseudoColumnMetaData, Object>> validationRules =
                createStandardValidationRules("MON$DATABASE", "YES");

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, "MON$DATABASE", "%");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testGttPreserve_allPseudoColumns() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules =
                createStandardValidationRules(GTT_PRESERVE_NAME, "NO");

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, GTT_PRESERVE_NAME, "%");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testGttDelete_allPseudoColumns() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules =
                createStandardValidationRules(GTT_DELETE_NAME, "NO");

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, GTT_DELETE_NAME, "%");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testPattern_nullColumn_allPseudoColumns() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules =
                createStandardValidationRules(NORMAL_TABLE_NAME, "NO");

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, NORMAL_TABLE_NAME, null);
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testPattern_noMatchingColumns() throws Exception {
        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, NORMAL_TABLE_NAME, "ABC");
        validate(pseudoColumns, Collections.emptyList());
    }

    @Test
    public void testPattern_noMatchingTables() throws Exception {
        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, "ABC", "%");
        validate(pseudoColumns, Collections.emptyList());
    }

    @Test
    public void testPattern_usingWildcard_allColumns() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules =
                createStandardValidationRules(NORMAL_TABLE_NAME, "NO");

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, NORMAL_TABLE_NAME, "RDB$%");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testPattern_usingEscape_dbKeyOnly() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules = Collections.singletonList(
                createDbkeyValidationRules(NORMAL_TABLE_NAME, 8));

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, NORMAL_TABLE_NAME, "RDB$DB\\_KEY");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testPattern_singleWildCard_dbKeyOnly() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules = Collections.singletonList(
                createDbkeyValidationRules(NORMAL_TABLE_NAME, 8));

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, NORMAL_TABLE_NAME, "RDB$DB_KEY");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testPattern_usingEscape_recordVersionOnly() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules = supportsRecordVersion
                ? Collections.singletonList(createRecordVersionValidationRules(NORMAL_TABLE_NAME, "NO"))
                : Collections.emptyList();

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, NORMAL_TABLE_NAME, "RDB$RECORD\\_VERSION");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testPattern_singleWildCard_recordVersionOnly() throws Exception {
        List<Map<PseudoColumnMetaData, Object>> validationRules = supportsRecordVersion
                ? Collections.singletonList(createRecordVersionValidationRules(NORMAL_TABLE_NAME, "NO"))
                : Collections.emptyList();

        ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, NORMAL_TABLE_NAME, "RDB$RECORD_VERSION");
        validate(pseudoColumns, validationRules);
    }

    @Test
    public void testPattern_emptyStringColumn() throws Exception {
        try (ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, NORMAL_TABLE_NAME, "")) {
            assertFalse("expected empty result set", pseudoColumns.next());
        }
    }

    @Test
    public void testPattern_emptyStringTable() throws Exception {
        try (ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, "", "%")) {
            assertFalse("expected empty result set", pseudoColumns.next());
        }
    }

    @Test
    public void testPattern_wildCardTable() throws Exception {
        try (ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, "%", "RDB$DB\\_KEY")) {
            int tableCount = 0;
            while (pseudoColumns.next()) {
                tableCount += 1;
            }

            assertEquals("Unexpected number of pseudo columns",
                    getDefaultSupportInfo().getSystemTableCount() + 7, tableCount);
        }
    }

    @Test
    public void testPattern_nullTable() throws Exception {
        try (ResultSet pseudoColumns = dbmd.getPseudoColumns(null, null, null, "RDB$DB\\_KEY")) {
            int tableCount = 0;
            while (pseudoColumns.next()) {
                tableCount += 1;
            }

            assertEquals("Unexpected number of pseudo columns",
                    // System tables + the 7 tables created for this test
                    getDefaultSupportInfo().getSystemTableCount() + 7, tableCount);
        }
    }

    private List<Map<PseudoColumnMetaData, Object>> createStandardValidationRules(String tableName,
            String recordVersionNullable) {
        List<Map<PseudoColumnMetaData, Object>> validationRules = new ArrayList<>();
        validationRules.add(createDbkeyValidationRules(tableName, 8));
        if (supportsRecordVersion) {
            validationRules.add(createRecordVersionValidationRules(tableName, recordVersionNullable));
        }
        return validationRules;
    }

    private void validate(ResultSet pseudoColumns, List<Map<PseudoColumnMetaData, Object>> expectedPseudoColumns)
            throws Exception {
        try {
            int columnCount = 0;
            while (pseudoColumns.next()) {
                if (columnCount < expectedPseudoColumns.size()) {
                    Map<PseudoColumnMetaData, Object> rules = expectedPseudoColumns.get(columnCount);
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

    private static final Map<PseudoColumnMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<PseudoColumnMetaData, Object> defaults = new EnumMap<>(PseudoColumnMetaData.class);
        defaults.put(PseudoColumnMetaData.TABLE_CAT, null);
        defaults.put(PseudoColumnMetaData.TABLE_SCHEM, null);
        defaults.put(PseudoColumnMetaData.DECIMAL_DIGITS, null);
        defaults.put(PseudoColumnMetaData.NUM_PREC_RADIX, 10);
        defaults.put(PseudoColumnMetaData.COLUMN_USAGE, PseudoColumnUsage.NO_USAGE_RESTRICTIONS.name());
        defaults.put(PseudoColumnMetaData.REMARKS, null);
        defaults.put(PseudoColumnMetaData.CHAR_OCTET_LENGTH, null);
        defaults.put(PseudoColumnMetaData.IS_NULLABLE, "NO");

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<PseudoColumnMetaData, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    private Map<PseudoColumnMetaData, Object> createDbkeyValidationRules(String tableName, int expectedDbKeyLength) {
        Map<PseudoColumnMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(PseudoColumnMetaData.TABLE_NAME, tableName);
        rules.put(PseudoColumnMetaData.COLUMN_NAME, "RDB$DB_KEY");
        rules.put(PseudoColumnMetaData.DATA_TYPE, Types.ROWID);
        rules.put(PseudoColumnMetaData.COLUMN_SIZE, expectedDbKeyLength);
        rules.put(PseudoColumnMetaData.REMARKS, "non-null");
        rules.put(PseudoColumnMetaData.CHAR_OCTET_LENGTH, expectedDbKeyLength);
        return rules;
    }

    private Map<PseudoColumnMetaData, Object> createRecordVersionValidationRules(String tableName, String nullable) {
        Map<PseudoColumnMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(PseudoColumnMetaData.TABLE_NAME, tableName);
        rules.put(PseudoColumnMetaData.COLUMN_NAME, "RDB$RECORD_VERSION");
        rules.put(PseudoColumnMetaData.DATA_TYPE, Types.BIGINT);
        rules.put(PseudoColumnMetaData.COLUMN_SIZE, 19);
        rules.put(PseudoColumnMetaData.DECIMAL_DIGITS, 0);
        rules.put(PseudoColumnMetaData.IS_NULLABLE, nullable);
        return rules;
    }

    /**
     * Columns defined for the getPseudoColumns() metadata.
     */
    private enum PseudoColumnMetaData implements MetaDataValidator.MetaDataInfo {
        TABLE_CAT(1, String.class),
        TABLE_SCHEM(2, String.class),
        TABLE_NAME(3, String.class),
        COLUMN_NAME(4, String.class),
        DATA_TYPE(5, Integer.class),
        COLUMN_SIZE(6, Integer.class),
        DECIMAL_DIGITS(7, Integer.class),
        NUM_PREC_RADIX(8, Integer.class),
        COLUMN_USAGE(9, String.class),
        REMARKS(10, String.class) {
            @Override
            public MetaDataValidator<PseudoColumnMetaData> getValidator() {
                return new MetaDataValidator<PseudoColumnMetaData>(this) {
                    // We're not going to validate the actual remark value, just that it is present when expected.
                    public void assertColumnValue(ResultSet rs, Object expectedValue) throws SQLException {
                        String remarkValue = rs.getString(getPosition());
                        if (expectedValue == null) {
                            assertNull("Expected null remark value", remarkValue);
                        } else {
                            assertNotNull("Expected non-null remark value", remarkValue);
                        }
                    }
                };
            }
        },
        CHAR_OCTET_LENGTH(11, Integer.class),
        IS_NULLABLE(12, String.class);

        private final int position;
        private final Class<?> columnClass;

        PseudoColumnMetaData(int position, Class<?> columnClass) {
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
