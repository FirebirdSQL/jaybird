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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;
import org.junit.Test;

import static org.firebirdsql.common.JdbcResourceHelper.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link FBDatabaseMetaData} for index info related metadata.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDatabaseMetaDataIndexInfo extends FBMetaDataTestBase<TestFBDatabaseMetaDataIndexInfo.IndexInfoMetaData> {
    
    public TestFBDatabaseMetaDataIndexInfo() {
        super(IndexInfoMetaData.class);
    }
    
    public static final String CREATE_INDEX_TEST_TABLE_1 =
            "CREATE TABLE index_test_table_1 (\n" + 
            "    id INTEGER CONSTRAINT pk_idx_test_1_id PRIMARY KEY,\n" + 
            "    column1 VARCHAR(10),\n" + 
            "    column2 INTEGER,\n" + 
            "    column3 INTEGER CONSTRAINT uq_idx_test_1_column3 UNIQUE\n" + 
            ")";
    
    public static final String CREATE_COMPUTED_IDX_TBL_1 =
            "CREATE INDEX cmp_idx_test_table_1 ON index_test_table_1 COMPUTED BY (UPPER(column1))";
    
    public static final String CREATE_UQ_COMPUTED_IDX_TBL_1 =
            "CREATE UNIQUE INDEX uq_comp_idx_tbl1 ON index_test_table_1 COMPUTED BY (column2 + column3)";
    
    public static final String CREATE_ASC_IDX_TBL_1_COLUMN2 =
            "CREATE ASCENDING INDEX idx_asc_idx_tbl_1_column2 ON index_test_table_1 (column2)";
    
    public static final String CREATE_INDEX_TEST_TABLE_2 =
            "CREATE TABLE index_test_table_2 (\n" + 
            "    id INTEGER,\n" + 
            "    column1 VARCHAR(10),\n" + 
            "    column2 INTEGER,\n" + 
            "    column3 INTEGER,\n" + 
            "    CONSTRAINT pk_idx_test_2_id PRIMARY KEY (id),\n" + 
            "    CONSTRAINT fk_idx_test_2_column2_test_1 FOREIGN KEY (column2) REFERENCES index_test_table_1 (id)\n" + 
            ")";
    
    public static final String CREATE_DESC_IDX_TBL_2_ID =
            "CREATE DESCENDING INDEX idx_desc_idx_tbl2_id ON index_test_table_2 (id)";
    
    public static final String CREATE_IDX_TBL_2_COL1_AND_2 =
            "CREATE INDEX idx_tbl_2_col1_col2 ON index_test_table_2 (column1, column2)";
    
    public static final String CREATE_DESC_COMPUTED_IDX_TBL_2 =
            "CREATE DESCENDING INDEX cmp_idx_desc_test_table2 ON index_test_table_2 COMPUTED BY (UPPER(column1))";
    
    public static final String CREATE_UQ_DESC_IDX_TBL_2_COL3_AND_COL2 =
            "CREATE UNIQUE DESCENDING INDEX uq_desc_idx_tbl2_col3_col2 ON index_test_table_2 (column3, column2)";

    @Override
    protected List<String> getCreateStatements() {
        return Arrays.asList(
                CREATE_INDEX_TEST_TABLE_1,
                CREATE_INDEX_TEST_TABLE_2,
                CREATE_COMPUTED_IDX_TBL_1,
                CREATE_UQ_COMPUTED_IDX_TBL_1,
                CREATE_ASC_IDX_TBL_1_COLUMN2,
                CREATE_DESC_IDX_TBL_2_ID,
                CREATE_IDX_TBL_2_COL1_AND_2,
                CREATE_DESC_COMPUTED_IDX_TBL_2,
                CREATE_UQ_DESC_IDX_TBL_2_COL3_AND_COL2);
    }
    
    /**
     * Tests the ordinal positions and types for the metadata columns of
     * getIndexInfo().
     */
    @Test
    public void testIndexInfoMetaDataColumns() throws Exception {
        try (ResultSet indexInfo = dbmd.getIndexInfo(null, null, "doesnotexist", false, true)) {
            validateResultSetColumns(indexInfo);
        }
    }
    
    /**
     * Tests getIndexInfo() for index_test_table_1 and unique false, expecting all indices, including those 
     * defined by PK, FK and Unique constraint.
     * <p>
     * Secondary: uses lowercase name of the table and approximate false
     * </p>
     */
    @Test
    public void testIndexInfo_table1_all() throws Exception {
        List<Map<IndexInfoMetaData, Object>> expectedIndexInfo = new ArrayList<>(5);
        String tableName = "INDEX_TEST_TABLE_1";
        expectedIndexInfo.add(createRule(tableName, true, "CMP_IDX_TEST_TABLE_1", "(UPPER(column1))", 1, true));
        expectedIndexInfo.add(createRule(tableName, true, "IDX_ASC_IDX_TBL_1_COLUMN2", "COLUMN2", 1, true));
        expectedIndexInfo.add(createRule(tableName, false, "PK_IDX_TEST_1_ID", "ID", 1, true));
        expectedIndexInfo.add(createRule(tableName, false, "UQ_COMP_IDX_TBL1", "(column2 + column3)", 1, true));
        expectedIndexInfo.add(createRule(tableName, false, "UQ_IDX_TEST_1_COLUMN3", "COLUMN3", 1, true));
        
        ResultSet indexInfo = dbmd.getIndexInfo(null, null, "INDEX_TEST_TABLE_1", false, false);
        validate(indexInfo, expectedIndexInfo);
    }
    
    /**
     * Tests getIndexInfo() for index_test_table_1 and unique true, expecting only the unique indices.
     * <p>
     * Secondary: uses uppercase name of the table and approximate true
     * </p>
     */
    @Test
    public void testIndexInfo_table1_unique() throws Exception {
        List<Map<IndexInfoMetaData, Object>> expectedIndexInfo = new ArrayList<>(3);
        String tableName = "INDEX_TEST_TABLE_1";
        expectedIndexInfo.add(createRule(tableName, false, "PK_IDX_TEST_1_ID", "ID", 1, true));
        expectedIndexInfo.add(createRule(tableName, false, "UQ_COMP_IDX_TBL1", "(column2 + column3)", 1, true));
        expectedIndexInfo.add(createRule(tableName, false, "UQ_IDX_TEST_1_COLUMN3", "COLUMN3", 1, true));
        
        ResultSet indexInfo = dbmd.getIndexInfo(null, null, "INDEX_TEST_TABLE_1", true, true);
        validate(indexInfo, expectedIndexInfo);
    }
    
    /**
     * Tests getIndexInfo() for index_test_table_2 and unique false, expecting all indices, including those 
     * defined by PK, FK and Unique constraint.
     * <p>
     * Secondary: uses uppercase name of the table and approximate true
     * </p>
     */
    @Test
    public void testIndexInfo_table2_all() throws Exception {
        List<Map<IndexInfoMetaData, Object>> expectedIndexInfo = new ArrayList<>(8);
        String tableName = "INDEX_TEST_TABLE_2";
        expectedIndexInfo.add(createRule(tableName, true, "CMP_IDX_DESC_TEST_TABLE2", "(UPPER(column1))", 1, false));
        expectedIndexInfo.add(createRule(tableName, true, "FK_IDX_TEST_2_COLUMN2_TEST_1", "COLUMN2", 1, true));
        expectedIndexInfo.add(createRule(tableName, true, "IDX_DESC_IDX_TBL2_ID", "ID", 1, false));
        expectedIndexInfo.add(createRule(tableName, true, "IDX_TBL_2_COL1_COL2", "COLUMN1", 1, true));
        expectedIndexInfo.add(createRule(tableName, true, "IDX_TBL_2_COL1_COL2", "COLUMN2", 2, true));
        expectedIndexInfo.add(createRule(tableName, false, "PK_IDX_TEST_2_ID", "ID", 1, true));
        expectedIndexInfo.add(createRule(tableName, false, "UQ_DESC_IDX_TBL2_COL3_COL2", "COLUMN3", 1, false));
        expectedIndexInfo.add(createRule(tableName, false, "UQ_DESC_IDX_TBL2_COL3_COL2", "COLUMN2", 2, false));
        
        ResultSet indexInfo = dbmd.getIndexInfo(null, null, "INDEX_TEST_TABLE_2", false, true);
        validate(indexInfo, expectedIndexInfo);
    }
    
    /**
     * Tests getIndexInfo() for index_test_table_2 and unique false, expecting all indices, including those 
     * defined by PK, FK and Unique constraint.
     * <p>
     * Secondary: uses lowercase name of the table and approximate false
     * </p>
     */
    @Test
    public void testIndexInfo_table2_unique() throws Exception {
        List<Map<IndexInfoMetaData, Object>> expectedIndexInfo = new ArrayList<>(8);
        String tableName = "INDEX_TEST_TABLE_2";
        expectedIndexInfo.add(createRule(tableName, false, "PK_IDX_TEST_2_ID", "ID", 1, true));
        expectedIndexInfo.add(createRule(tableName, false, "UQ_DESC_IDX_TBL2_COL3_COL2", "COLUMN3", 1, false));
        expectedIndexInfo.add(createRule(tableName, false, "UQ_DESC_IDX_TBL2_COL3_COL2", "COLUMN2", 2, false));
        
        ResultSet indexInfo = dbmd.getIndexInfo(null, null, "INDEX_TEST_TABLE_2", true, false);
        validate(indexInfo, expectedIndexInfo);
    }
    
    // TODO Add tests with quoted identifiers
    
    private void validate(ResultSet indexInfo, List<Map<IndexInfoMetaData, Object>> expectedIndexInfo) throws Exception {
        try {
            int columnCount = 0;
            while (indexInfo.next()) {
                if (columnCount < expectedIndexInfo.size()) {
                    Map<IndexInfoMetaData, Object> rules = expectedIndexInfo.get(columnCount);
                    checkValidationRulesComplete(rules);
                    validateRowValues(indexInfo, rules);
                }
                columnCount++;
            }
            assertEquals("Unexpected number of columns", expectedIndexInfo.size(), columnCount);
        } finally {
            closeQuietly(indexInfo);
        }
    }

    private Map<IndexInfoMetaData, Object> createRule(String tableName, boolean nonUnique, String indexName, 
            String columnName, Integer ordinalPosition, boolean ascending) throws Exception {
        Map<IndexInfoMetaData, Object> indexRules = getDefaultValueValidationRules();
        indexRules.put(IndexInfoMetaData.TABLE_NAME, tableName);
        indexRules.put(IndexInfoMetaData.NON_UNIQUE, nonUnique ? "T" : "F");
        indexRules.put(IndexInfoMetaData.INDEX_NAME, indexName);
        indexRules.put(IndexInfoMetaData.COLUMN_NAME, columnName);
        indexRules.put(IndexInfoMetaData.ORDINAL_POSITION, ordinalPosition);
        indexRules.put(IndexInfoMetaData.ASC_OR_DESC, ascending ? "A" : "D");
        return indexRules;
    }
    
    private static final Map<IndexInfoMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<IndexInfoMetaData, Object> defaults = new EnumMap<>(IndexInfoMetaData.class);
        defaults.put(IndexInfoMetaData.TABLE_CAT, null);
        defaults.put(IndexInfoMetaData.TABLE_SCHEM, null);
        defaults.put(IndexInfoMetaData.INDEX_QUALIFIER, null);
        defaults.put(IndexInfoMetaData.TYPE, DatabaseMetaData.tableIndexOther);
        defaults.put(IndexInfoMetaData.CARDINALITY, null);
        defaults.put(IndexInfoMetaData.PAGES, null);
        defaults.put(IndexInfoMetaData.FILTER_CONDITION, null);
        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    @Override
    protected Map<IndexInfoMetaData, Object> getDefaultValueValidationRules() throws Exception {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    /**
     * Columns defined for the getIndexInfo() metadata.
     */
    enum IndexInfoMetaData implements MetaDataInfo {
        TABLE_CAT(1, String.class),
        TABLE_SCHEM(2, String.class),
        TABLE_NAME(3, String.class),
        NON_UNIQUE(4, String.class), // FB does not support boolean
        INDEX_QUALIFIER(5, String.class),
        INDEX_NAME(6, String.class),
        TYPE(7, Short.class),
        ORDINAL_POSITION(8, Short.class),
        COLUMN_NAME(9, String.class),
        ASC_OR_DESC(10, String.class),
        CARDINALITY(11, Integer.class),
        PAGES(12, Integer.class),
        FILTER_CONDITION(13, String.class)
        ;
        
        private final int position;
        private final Class<?> columnClass;

        IndexInfoMetaData(int position, Class<?> columnClass) {
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
            return new MetaDataValidator<>(this);
        }
    }
}
