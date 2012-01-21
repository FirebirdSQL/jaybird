/*
 * $Id$
 * 
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;

/**
 * Tests for {@link FBDatabaseMetaData} for table related metadata.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDatabaseMetaDataTables extends FBMetaDataTestBase<TestFBDatabaseMetaDataTables.TableMetaData> {

    // Valid values for TABLE_TYPE (separate from those defined in AbstractDatabaseMetaData for testing)
    private static final String VIEW = "VIEW";
    private static final String TABLE = "TABLE";
    private static final String SYSTEM_TABLE = "SYSTEM TABLE";

    public TestFBDatabaseMetaDataTables(String name) {
        super(name, TableMetaData.class);
    }
    
    public static final String CREATE_NORMAL_TABLE =
            "CREATE TABLE test_normal_table (" + 
            "    id INTEGER PRIMARY KEY," + 
            "    varchar_field VARCHAR(100)" + 
            ")";
    
    public static final String CREATE_QUOTED_NORMAL_TABLE =
            "CREATE TABLE \"test_quoted_normal_table\" (\r\n" + 
            "    id INTEGER PRIMARY KEY,\r\n" + 
            "    varchar_field VARCHAR(100)\r\n" + 
            ")";
    
    public static final String CREATE_NORMAL_VIEW =
            "CREATE VIEW test_normal_view (id, varchar_1, varchar_2) " + 
            "AS " + 
            "SELECT t1.id, t1.varchar_field, t2.varchar_field " + 
            "FROM test_normal_table t1 " + 
            "INNER JOIN \"test_quoted_normal_table\" t2 ON t1.id = t2.id";
    
    public static final String CREATE_QUOTED_NORMAL_VIEW =
            "CREATE VIEW \"test_quoted_normal_view\" (id, varchar_1, varchar_2) " + 
            "AS " + 
            "SELECT t1.id, t1.varchar_field, t2.varchar_field " + 
            "FROM test_normal_table t1 " + 
            "INNER JOIN \"test_quoted_normal_table\" t2 ON t1.id = t2.id";
    
    public static final String DROP_NORMAL_TABLE =
            "DROP TABLE test_normal_table";
    
    public static final String DROP_QUOTED_NORMAL_TABLE =
            "DROP TABLE \"test_quoted_normal_table\"";
    
    public static final String DROP_NORMAL_VIEW =
            "DROP VIEW test_normal_view";
    
    public static final String DROP_QUOTED_NORMAL_VIEW =
            "DROP view \"test_quoted_normal_view\"";
    
    protected List<String> getDropStatements() {
        return Arrays.asList(
                DROP_NORMAL_VIEW,
                DROP_QUOTED_NORMAL_VIEW,
                DROP_NORMAL_TABLE,
                DROP_QUOTED_NORMAL_TABLE);
    }
    
    protected List<String> getCreateStatements() {
        return Arrays.asList(
                CREATE_NORMAL_TABLE,
                CREATE_QUOTED_NORMAL_TABLE,
                CREATE_NORMAL_VIEW,
                CREATE_QUOTED_NORMAL_VIEW);
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of
     * getTables().
     */
    public void testTableMetaDataColumns() throws Exception {
        ResultSet tables = dbmd.getTables(null, null, null, null);
        try {
            validateResultSetColumns(tables);
        } finally {
            closeQuietly(tables);
        }
    }

    /**
     * Tests getTables() with tableName null and types null, expecting all
     * tables of all types to be returned.
     */
    public void testTableMetaData_everything_tableName_null_types_null() throws Exception {
        validateTableMetaData_everything(null, null);
    }

    /**
     * Tests getTables() with tableName null and types all (supported) types,
     * expecting all tables of all types to be returned.
     */
    public void testTableMetaData_everything_tableName_null_allTypes() throws Exception {
        validateTableMetaData_everything(null, new String[] { SYSTEM_TABLE, TABLE, VIEW });
    }

    /**
     * Tests getTables() with tableName empty string and types null, expecting
     * all tables of all types to be returned.
     */
    public void testTableMetaData_everything_tableName_empty_types_null() throws Exception {
        validateTableMetaData_everything("", null);
    }

    /**
     * Tests getTables() with tableName empty string and types all (supported)
     * types, expecting all tables of all types to be returned.
     */
    public void testTableMetaData_everything_tableName_empty_types_allTypes() throws Exception {
        validateTableMetaData_everything("", new String[] { SYSTEM_TABLE, TABLE, VIEW });
    }

    /**
     * Tests getTables() with tableName all pattern (%) and types null,
     * expecting all tables of all types to be returned.
     */
    public void testTableMetaData_everything_tableName_allPattern_types_null() throws Exception {
        validateTableMetaData_everything("%", null);
    }

    /**
     * Helper method for test methods that retrieve table metadata for all
     * tables of all types.
     * 
     * @param tableNamePattern
     *            Pattern for the tableName (should be null, "", "%" only for
     *            this test)
     * @param types
     *            Array of types to retrieve
     */
    private void validateTableMetaData_everything(String tableNamePattern, String[] types) throws Exception {
        ResultSet tables = dbmd.getTables(null, null, tableNamePattern, types);
        // TODO: Should quoted table names be returned quoted?
        // Expected user tables + a selection of expected system tables (some that existed in Firebird 1.0)
        // TODO Add test for order?
        Set<String> expectedTables = new HashSet(Arrays.asList("TEST_NORMAL_TABLE",
                "test_quoted_normal_table", "TEST_NORMAL_VIEW", "test_quoted_normal_view",
                "RDB$FIELDS", "RDB$GENERATORS", "RDB$ROLES", "RDB$DATABASE", "RDB$TRIGGERS"));
        try {
            while (tables.next()) {
                String tableName = tables.getString(TableMetaData.TABLE_NAME.name());
                Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
                assertTrue("TABLE_NAME is not allowed to be null or empty", 
                        tableName != null && tableName.length() > 0);
                expectedTables.remove(tableName);

                if (tableName.startsWith("RDB$") || tableName.startsWith("MON$")) {
                    rules.put(TableMetaData.TABLE_TYPE, SYSTEM_TABLE);
                } else if (tableName.equals("TEST_NORMAL_TABLE") || tableName.equals("test_quoted_normal_table")) {
                    rules.put(TableMetaData.TABLE_TYPE, TABLE);
                } else if (tableName.equals("TEST_NORMAL_VIEW") || tableName.equals("test_quoted_normal_view")) {
                    rules.put(TableMetaData.TABLE_TYPE, VIEW);
                } else {
                    // Make sure we don't accidentally miss a table
                    fail("Unexpected TABLE_NAME: " + tableName);
                }

                validateRowValues(tables, rules);
            }

            assertTrue("getTables() did not return some expected tables: " + expectedTables,
                    expectedTables.isEmpty());
        } finally {
            closeQuietly(tables);
        }
    }

    /**
     * Tests getTables with tableName null and types SYSTEM TABLES, expecting
     * only system tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    public void testTableMetaData_allSystemTables_tableName_null() throws Exception {
        validateTableMetaData_allSystemTables(null);
    }

    /**
     * Tests getTables with tableName empty and types SYSTEM TABLES, expecting
     * only system tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    public void testTableMetaData_allSystemTables_tableName_empty() throws Exception {
        validateTableMetaData_allSystemTables("");
    }

    /**
     * Tests getTables with tableName all pattern (%) and types SYSTEM TABLES,
     * expecting only system tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    public void testTableMetaData_allSystemTables_tableName_allPattern() throws Exception {
        validateTableMetaData_allSystemTables("%");
    }

    /**
     * Helper method for test methods that retrieve table metadata of all system
     * tables.
     * 
     * @param tableNamePattern
     *            Pattern for the tableName (should be null, "", "%" only for
     *            this test)
     */
    private void validateTableMetaData_allSystemTables(String tableNamePattern) throws Exception {
        ResultSet tables = dbmd.getTables(null, null, tableNamePattern,
                new String[] { SYSTEM_TABLE });
        // Expected selection of expected system tables (some that existed in Firebird 1.0); we don't check all system tables
        Set<String> expectedTables = new HashSet(Arrays.asList("RDB$FIELDS", "RDB$GENERATORS",
                "RDB$ROLES", "RDB$DATABASE", "RDB$TRIGGERS"));
        Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(TableMetaData.TABLE_TYPE, SYSTEM_TABLE);
        try {
            while (tables.next()) {
                String tableName = tables.getString(TableMetaData.TABLE_NAME.name());
                assertTrue("TABLE_NAME is not allowed to be null or empty", 
                        tableName != null && tableName.length() > 0);
                expectedTables.remove(tableName);
                
                if (!(tableName.startsWith("RDB$") || tableName.startsWith("MON$"))) {
                    fail("Only expect tablenames starting with RDB$ or MON$, retrieved " + tableName);
                }

                validateRowValues(tables, rules);
            }

            assertTrue("getTables() did not return some expected tables: " + expectedTables,
                    expectedTables.isEmpty());
        } finally {
            closeQuietly(tables);
        }
    }

    /**
     * Tests getTables with tableName null and types SYSTEM TABLES, expecting
     * only system tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    public void testTableMetaData_allNormalTables_tableName_null() throws Exception {
        validateTableMetaData_allNormalTables(null);
    }

    /**
     * Tests getTables with tableName empty and types SYSTEM TABLES, expecting
     * only system tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    public void testTableMetaData_allNormalTables_tableName_empty() throws Exception {
        validateTableMetaData_allNormalTables("");
    }

    /**
     * Tests getTables with tableName all pattern (%) and types SYSTEM TABLES,
     * expecting only system tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    public void testTableMetaData_allNormalTables_tableName_allPattern() throws Exception {
        validateTableMetaData_allNormalTables("%");
    }

    /**
     * Helper method for test methods that retrieve table metadata of all normal
     * tables.
     * 
     * @param tableNamePattern
     *            Pattern for the tableName (should be null, "", "%" only for
     *            this test)
     */
    private void validateTableMetaData_allNormalTables(String tableNamePattern) throws Exception {
        ResultSet tables = dbmd.getTables(null, null, tableNamePattern, new String[] { TABLE });
        // TODO: Should quoted table names be returned quoted?
        // Expected normal tables
        Set<String> expectedTables = new HashSet(Arrays.asList("TEST_NORMAL_TABLE",
                "test_quoted_normal_table"));
        Set<String> retrievedTables = new HashSet<String>();
        Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(TableMetaData.TABLE_TYPE, TABLE);
        try {
            while (tables.next()) {
                String tableName = tables.getString(TableMetaData.TABLE_NAME.name());
                assertTrue("TABLE_NAME is not allowed to be null or empty",
                        tableName != null && tableName.length() > 0);
                retrievedTables.add(tableName);
                
                if ((tableName.startsWith("RDB$") || tableName.startsWith("MON$"))) {
                    fail("Only expect normal tables, not starting with RDB$ or MON$, retrieved " + tableName);
                }

                validateRowValues(tables, rules);
            }

            assertEquals("getTables() did not return expected tables: ", 
                    expectedTables, retrievedTables);
        } finally {
            closeQuietly(tables);
        }
    }

    /**
     * Tests getTables with tableName null and types SYSTEM TABLES, expecting
     * only system tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    public void testTableMetaData_allViews_tableName_null() throws Exception {
        validateTableMetaData_allViews(null);
    }

    /**
     * Tests getTables with tableName empty and types SYSTEM TABLES, expecting
     * only system tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    public void testTableMetaData_allViews_tableName_empty() throws Exception {
        validateTableMetaData_allViews("");
    }

    /**
     * Tests getTables with tableName all pattern (%) and types SYSTEM TABLES,
     * expecting only system tables to be returned.
     * <p>
     * This method only checks the existence of a subset of the system tables
     * <p>
     */
    public void testTableMetaData_allViews_tableName_allPattern() throws Exception {
        validateTableMetaData_allViews("%");
    }

    /**
     * Helper method for test methods that retrieve table metadata of all view
     * tables.
     * 
     * @param tableNamePattern
     *            Pattern for the tableName (should be null, "", "%" only for
     *            this test)
     */
    private void validateTableMetaData_allViews(String tableNamePattern) throws Exception {
        ResultSet tables = dbmd.getTables(null, null, tableNamePattern, new String[] { VIEW });
        // TODO: Should quoted table names be returned quoted?
        // Expected normal tables
        Set<String> expectedTables = new HashSet(Arrays.asList("TEST_NORMAL_VIEW",
                "test_quoted_normal_view"));
        Set<String> retrievedTables = new HashSet<String>();
        Map<TableMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(TableMetaData.TABLE_TYPE, VIEW);
        try {
            while (tables.next()) {
                String tableName = tables.getString(TableMetaData.TABLE_NAME.name());
                assertTrue("TABLE_NAME is not allowed to be null or empty", 
                        tableName != null && tableName.length() > 0);
                retrievedTables.add(tableName);

                if ((tableName.startsWith("RDB$") || tableName.startsWith("MON$"))) {
                    fail("Only expect views, not starting with RDB$ or MON$, retrieved " + tableName);
                }

                validateRowValues(tables, rules);
            }

            assertEquals("getTables() did not return expected tables: ", 
                    expectedTables, retrievedTables);
        } finally {
            closeQuietly(tables);
        }
    }

    /**
     * Tests getTables retrieving normal table that was created unquoted using
     * its lower case name with types null.
     */
    public void testTableMetaData_NormalUnquotedTable_lowercase_typesNull() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "TEST_NORMAL_TABLE");

        validateTableMetaDataSingleRow("test_normal_table", null, validationRules);
    }

    /**
     * Tests getTables retrieving normal table that was created unquoted using
     * its upper case name with types TABLE.
     */
    public void testTableMetaData_NormalUnquotedTable_uppercase_typesTABLE() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "TEST_NORMAL_TABLE");

        validateTableMetaDataSingleRow("TEST_NORMAL_TABLE", new String[] { TABLE }, validationRules);
    }

    /**
     * Tests getTables retrieving normal table that was created quoted using its
     * normal case name with all types.
     */
    public void testTableMetaData_NormalQuotedTable_AllTypes() throws Exception {
        Map<TableMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(TableMetaData.TABLE_TYPE, TABLE);
        validationRules.put(TableMetaData.TABLE_NAME, "test_quoted_normal_table");

        /*
         * TODO: Might break spec:
         * "tableNamePattern - a table name pattern; must match the table name as it is stored in the database"
         * Firebird stores the quoted name unquoted in its orignal case. On
         * the other hand, all related metadata methods specifically talk about
         * quoted versus unquoted and case insensitive
         */
        // validateTableMetaDataSingleRow("test_quoted_normal_table", new String[] { SYSTEM_TABLE, TABLE, VIEW }, validationRules);
        validateTableMetaDataSingleRow("\"test_quoted_normal_table\"", new String[] { SYSTEM_TABLE, TABLE, VIEW }, validationRules);
    }

    /**
     * Helper method for test methods that retrieve a single metadata row.
     * 
     * @param tableNamePattern
     *            Pattern of the tablename
     * @param types
     *            Table types to request
     * @param validationRules
     *            Total (all required rows) map of the value validation rules
     *            for the single row.
     */
    private void validateTableMetaDataSingleRow(String tableNamePattern, String[] types,
            Map<TableMetaData, Object> validationRules) throws Exception {
        
        checkValidationRulesComplete(validationRules);
        ResultSet tables = dbmd.getTables(null, null, tableNamePattern, types);
        try {
            assertTrue("Expected row in table metadata", tables.next());
            validateRowValues(tables, validationRules);
            assertFalse("Expected only one row in resultset", tables.next());
        } finally {
            closeQuietly(tables);
        }
    }

    /**
     * Tests getTables retrieving views, specifying the name of a normal table:
     * expecting empty resultset
     */
    public void testTableMetaData_NormalUnquotedTable_typesVIEW() throws Exception {
        validateTableMetaDataNoRow("test_normal_table", new String[] { VIEW });
    }

    /**
     * Tests getTables retrieving normal tables, specifying the name of a system
     * table: expecting empty resultset
     */
    public void testTableMetaData_SystemTable_typesTABLE() throws Exception {
        validateTableMetaDataNoRow("RDB$DATABASE", new String[] { TABLE });
    }

    /**
     * Tests getTables retrieving system tables, specifying the name of a view:
     * expecting empty resultset
     */
    public void testTableMetaData_UnquotedView_typesSYSTEM() throws Exception {
        validateTableMetaDataNoRow("test_normal_view", new String[] { SYSTEM_TABLE });
    }

    /**
     * Helper method for test methods that retrieve metadata expecting no
     * results.
     * 
     * @param tableNamePattern
     *            Pattern of the tablename
     * @param types
     *            Table types to request
     */
    private void validateTableMetaDataNoRow(String tableNamePattern, String[] types) throws Exception {
        ResultSet tables = dbmd.getTables(null, null, tableNamePattern, types);
        try {
            assertFalse(String.format("Expected empty resultset for requesting %s with types %s",
                    tableNamePattern, Arrays.toString(types)), tables.next());
        } finally {
            closeQuietly(tables);
        }
    }
    
    // TODO: Add more extensive tests of patterns and combination of table types

    private static final Map<TableMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<TableMetaData, Object> defaults = new EnumMap<TableMetaData, Object>(
                TableMetaData.class);
        defaults.put(TableMetaData.TABLE_CAT, null);
        defaults.put(TableMetaData.TABLE_SCHEM, null);
        defaults.put(TableMetaData.REMARKS, null);
        defaults.put(TableMetaData.TYPE_CAT, null);
        defaults.put(TableMetaData.TYPE_SCHEM, null);
        defaults.put(TableMetaData.TYPE_NAME, null);
        // NOTE Self-referencing is for structured types, which Firebird does not provide (See SQL2003 4.14.5)
        defaults.put(TableMetaData.SELF_REFERENCING_COL_NAME, null);
        defaults.put(TableMetaData.REF_GENERATION, null);
        defaults.put(TableMetaData.OWNER_NAME, "SYSDBA");

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    protected Map<TableMetaData, Object> getDefaultValueValidationRules() throws Exception {
        return new EnumMap<TableMetaData, Object>(DEFAULT_COLUMN_VALUES);
    }

    /**
     * Columns defined for the getTables() metadata.
     */
    enum TableMetaData implements MetaDataInfo {
        TABLE_CAT(1, String.class), 
        TABLE_SCHEM(2, String.class), 
        TABLE_NAME(3, String.class),
        TABLE_TYPE(4, String.class),
        REMARKS(5, String.class),
        TYPE_CAT(6, String.class),
        TYPE_SCHEM(7, String.class),
        TYPE_NAME(8, String.class),
        SELF_REFERENCING_COL_NAME(9, String.class),
        REF_GENERATION(10, String.class),
        OWNER_NAME(11, String.class)
        ;

        private final int position;
        private final Class<?> columnClass;

        private TableMetaData(int position, Class<?> columnClass) {
            this.position = position;
            this.columnClass = columnClass;
        }

        public int getPosition() {
            return position;
        }

        public Class<?> getColumnClass() {
            return columnClass;
        }

        public MetaDataValidator<?> getValidator() {
            return new MetaDataValidator<TableMetaData>(this);
        }
    }
}
