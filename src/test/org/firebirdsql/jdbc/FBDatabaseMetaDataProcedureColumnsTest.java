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
import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.util.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for {@link FBDatabaseMetaData} for procedure columns related metadata.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBDatabaseMetaDataProcedureColumnsTest {
    
    // TODO This test will need to be expanded with version dependent features 
    // (eg TYPE OF <domain> (2.1), TYPE OF COLUMN <table.column> (2.5), NOT NULL (2.1), DEFAULT <value> (2.0)

    //@formatter:off
    private static final String CREATE_NORMAL_PROC_NO_ARG_NO_RETURN =
            "CREATE PROCEDURE proc_no_arg_no_return\n" + 
            "AS\n" + 
            "DECLARE VARIABLE dummy INTEGER;\n" + 
            "BEGIN\n" + 
            "  dummy = 1 + 1;\n" + 
            "END";
    
    private static final String CREATE_NORMAL_PROC_NO_RETURN =
            "CREATE PROCEDURE normal_proc_no_return\n" +
            " ( param1 VARCHAR(100),\n" +
            "   \"param2\" INTEGER)\n" +
            "AS\n" +
            "DECLARE VARIABLE dummy INTEGER;\n" +
            "BEGIN\n" +
            "  dummy = 1 + 1;\n" +
            "END";

    private static final String CREATE_NORMAL_PROC_WITH_RETURN =
            "CREATE PROCEDURE normal_proc_with_return\n" +
            " ( param1 VARCHAR(100),\n" +
            "   param2 DECIMAL(18,2),\n" +
            "   param3 NUMERIC(4,3),\n" +
            "   param4 TIMESTAMP)\n" +
            "RETURNS (return1 VARCHAR(200), return2 INTEGER, \"return3\" DOUBLE PRECISION)\n" +
            "AS\n" +
            "BEGIN\n" +
            "  return1 = param1 || param1;\n" +
            "  return2 = 1;\n" +
            "END";

    private static final String CREATE_QUOTED_PROC_NO_RETURN =
            "CREATE PROCEDURE \"quoted_proc_no_return\"\n" +
            " ( param1 VARCHAR(100))\n" +
            "AS\n" +
            "DECLARE VARIABLE dummy INTEGER;\n" +
            "BEGIN\n" +
            "  dummy = 1 + 1;\n" +
            "END";
    
    private static final String ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN_PARAM2 =
            "COMMENT ON PARAMETER normal_proc_with_return.param2 IS 'Some comment'";
    //@formatter:on

    private static final MetaDataTestSupport<ProcedureColumnMetaData> metaDataTestSupport =
            new MetaDataTestSupport<>(ProcedureColumnMetaData.class);

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getCreateStatements());

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

    private static List<String> getCreateStatements() {
        List<String> statements = new ArrayList<>(Arrays.asList(
                CREATE_NORMAL_PROC_NO_ARG_NO_RETURN,
                CREATE_NORMAL_PROC_NO_RETURN,
                CREATE_NORMAL_PROC_WITH_RETURN,
                CREATE_QUOTED_PROC_NO_RETURN));
        if (getDefaultSupportInfo().supportsComment()) {
            statements.add(ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN_PARAM2);
        }
        return statements;
    }
    
    /**
     * Tests the ordinal positions and types for the metadata columns of
     * getProcedureColumns().
     */
    @Test
    void testProcedureColumnsMetaDataColumns() throws Exception {
        try (ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "doesnotexist", "%")) {
            metaDataTestSupport.validateResultSetColumns(procedureColumns);
        }
    }
    
    /**
     * Tests getProcedureColumn with proc_no_arg_no_return, expecting empty ResultSet.
     */
    @Test
    void testProcedureColumns_noArg_noReturn() throws Exception {
        try (ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "proc_no_arg_no_return", "%")) {
            assertFalse(procedureColumns.next(), "Expected empty resultset for procedure with arguments or return values");
        }
    }
    
    /**
     * Tests getProcedureColumn with normal_proc_no_return using all columnPattern, expecting resultset with all defined rows.
     */
    @Test
    void testProcedureColumns_normalProc_noReturn_allPattern() throws Exception {
        List<Map<ProcedureColumnMetaData, Object>> expectedColumns = new ArrayList<>(2);
        Map<ProcedureColumnMetaData, Object> column = getDefaultValueValidationRules();
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "NORMAL_PROC_NO_RETURN");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "PARAM1");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnIn);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.VARCHAR);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "VARCHAR");
        column.put(ProcedureColumnMetaData.PRECISION, 100);
        column.put(ProcedureColumnMetaData.LENGTH, 100);
        column.put(ProcedureColumnMetaData.CHAR_OCTET_LENGTH, 100);
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 1);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        column = getDefaultValueValidationRules();
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "NORMAL_PROC_NO_RETURN");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "param2");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnIn);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.INTEGER);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "INTEGER");
        column.put(ProcedureColumnMetaData.PRECISION, 10);
        column.put(ProcedureColumnMetaData.LENGTH, 4);
        column.put(ProcedureColumnMetaData.SCALE, 0);
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 2);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        
        ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "NORMAL_PROC_NO_RETURN", "%");
        validate(procedureColumns, expectedColumns);        
    }

    /**
     * Tests getProcedureColumn with normal_proc_with_return using columnPattern all (%) string, expecting resultset with all defined rows.
     */
    @Test
    void testProcedureColumns_normalProc_withReturn_allPattern() throws Exception {
        List<Map<ProcedureColumnMetaData, Object>> expectedColumns = new ArrayList<>(7);
        Map<ProcedureColumnMetaData, Object> column = getDefaultValueValidationRules();
        final boolean supportsFloatBinaryPrecision = getDefaultSupportInfo().supportsFloatBinaryPrecision();
        // TODO Having result columns first might be against JDBC spec
        // TODO Describing result columns as procedureColumnOut might be against JDBC spec
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "RETURN1");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnOut);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.VARCHAR);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "VARCHAR");
        column.put(ProcedureColumnMetaData.PRECISION, 200);
        column.put(ProcedureColumnMetaData.LENGTH, 200);
        column.put(ProcedureColumnMetaData.CHAR_OCTET_LENGTH, 200);
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 1);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        column = getDefaultValueValidationRules();
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "RETURN2");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnOut);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.INTEGER);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "INTEGER");
        column.put(ProcedureColumnMetaData.PRECISION, 10);
        column.put(ProcedureColumnMetaData.LENGTH, 4);
        column.put(ProcedureColumnMetaData.SCALE, 0);
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 2);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        column = getDefaultValueValidationRules();
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "return3");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnOut);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.DOUBLE);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "DOUBLE PRECISION");
        column.put(ProcedureColumnMetaData.PRECISION, supportsFloatBinaryPrecision ? 53 : 15);
        column.put(ProcedureColumnMetaData.RADIX, supportsFloatBinaryPrecision ? 2 : 10);
        column.put(ProcedureColumnMetaData.LENGTH, 8);
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 3);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        column = getDefaultValueValidationRules();
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "PARAM1");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnIn);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.VARCHAR);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "VARCHAR");
        column.put(ProcedureColumnMetaData.PRECISION, 100);
        column.put(ProcedureColumnMetaData.LENGTH, 100);
        column.put(ProcedureColumnMetaData.CHAR_OCTET_LENGTH, 100);
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 1);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        column = getDefaultValueValidationRules();
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "PARAM2");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnIn);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.DECIMAL);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "DECIMAL");
        column.put(ProcedureColumnMetaData.PRECISION, 18);
        column.put(ProcedureColumnMetaData.LENGTH, 8);
        column.put(ProcedureColumnMetaData.SCALE, 2);
        column.put(ProcedureColumnMetaData.REMARKS, "Some comment");
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 2);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        column = getDefaultValueValidationRules();
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "PARAM3");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnIn);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.NUMERIC);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "NUMERIC");
        column.put(ProcedureColumnMetaData.PRECISION, 4);
        column.put(ProcedureColumnMetaData.LENGTH, 2);
        column.put(ProcedureColumnMetaData.SCALE, 3);
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 3);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        column = getDefaultValueValidationRules();
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "PARAM4");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnIn);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.TIMESTAMP);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "TIMESTAMP");
        column.put(ProcedureColumnMetaData.PRECISION, 19);
        column.put(ProcedureColumnMetaData.LENGTH, 8);
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 4);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        
        ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "NORMAL_PROC_WITH_RETURN", "%");
        validate(procedureColumns, expectedColumns); 
    }
    
    /**
     * Tests getProcedureColumn with quoted_proc_no_return using all columnPattern, expecting result set with all defined rows.
     */
    @Test
    void testProcedureColumns_quotedProc_noReturn_allPattern() throws Exception {
        List<Map<ProcedureColumnMetaData, Object>> expectedColumns = new ArrayList<>(1);
        Map<ProcedureColumnMetaData, Object> column = getDefaultValueValidationRules();
        column.put(ProcedureColumnMetaData.PROCEDURE_NAME, "quoted_proc_no_return");
        column.put(ProcedureColumnMetaData.COLUMN_NAME, "PARAM1");
        column.put(ProcedureColumnMetaData.COLUMN_TYPE, DatabaseMetaData.procedureColumnIn);
        column.put(ProcedureColumnMetaData.DATA_TYPE, Types.VARCHAR);
        column.put(ProcedureColumnMetaData.TYPE_NAME, "VARCHAR");
        column.put(ProcedureColumnMetaData.PRECISION, 100);
        column.put(ProcedureColumnMetaData.LENGTH, 100);
        column.put(ProcedureColumnMetaData.CHAR_OCTET_LENGTH, 100);
        column.put(ProcedureColumnMetaData.ORDINAL_POSITION, 1);
        column.put(ProcedureColumnMetaData.SPECIFIC_NAME, column.get(ProcedureColumnMetaData.PROCEDURE_NAME));
        expectedColumns.add(column);
        
        ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "quoted_proc_no_return", "%");
        validate(procedureColumns, expectedColumns);        
    }
    
    // TODO Add tests for more complex patterns for procedure and column
    
    private void validate(ResultSet procedureColumns, List<Map<ProcedureColumnMetaData, Object>> expectedColumns) throws Exception {
        try {
            int parameterCount = 0;
            while(procedureColumns.next()) {
                if (parameterCount < expectedColumns.size()) {
                    Map<ProcedureColumnMetaData, Object> rules = expectedColumns.get(parameterCount);
                    metaDataTestSupport.checkValidationRulesComplete(rules);
                    metaDataTestSupport.validateRowValues(procedureColumns, rules);
                }
                parameterCount++;
            }
            assertEquals(expectedColumns.size(), parameterCount, "Unexpected number of procedure columns returned");
        } finally {
            closeQuietly(procedureColumns);
        }
    }
    
    private static final Map<ProcedureColumnMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<ProcedureColumnMetaData, Object> defaults = new EnumMap<>(ProcedureColumnMetaData.class);
        defaults.put(ProcedureColumnMetaData.PROCEDURE_CAT, null);
        defaults.put(ProcedureColumnMetaData.PROCEDURE_SCHEM, null);
        defaults.put(ProcedureColumnMetaData.SCALE, null);
        defaults.put(ProcedureColumnMetaData.RADIX, 10);
        defaults.put(ProcedureColumnMetaData.NULLABLE, DatabaseMetaData.procedureNullable);
        defaults.put(ProcedureColumnMetaData.REMARKS, null);
        defaults.put(ProcedureColumnMetaData.COLUMN_DEF, null);
        defaults.put(ProcedureColumnMetaData.SQL_DATA_TYPE, null);
        defaults.put(ProcedureColumnMetaData.SQL_DATETIME_SUB, null);
        defaults.put(ProcedureColumnMetaData.CHAR_OCTET_LENGTH, null);
        defaults.put(ProcedureColumnMetaData.IS_NULLABLE, "YES");
        
        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<ProcedureColumnMetaData, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }
    
    /**
     * Columns defined for the getProcedureColumns() metadata.
     */
    private enum ProcedureColumnMetaData implements MetaDataInfo {
        PROCEDURE_CAT(1, String.class), 
        PROCEDURE_SCHEM(2, String.class), 
        PROCEDURE_NAME(3, String.class), 
        COLUMN_NAME(4, String.class), 
        COLUMN_TYPE(5, Short.class),
        DATA_TYPE(6, Integer.class), 
        TYPE_NAME(7, String.class), 
        PRECISION(8, Integer.class), 
        LENGTH(9, Integer.class), 
        SCALE(10, Short.class), 
        RADIX(11, Short.class), 
        NULLABLE(12, Short.class), 
        REMARKS(13, String.class), 
        COLUMN_DEF(14, String.class), 
        SQL_DATA_TYPE(15, Integer.class), 
        SQL_DATETIME_SUB(16, Integer.class), 
        CHAR_OCTET_LENGTH(17, Integer.class), 
        ORDINAL_POSITION(18, Integer.class), 
        IS_NULLABLE(19, String.class),
        SPECIFIC_NAME(20, String.class)
        ;

        private final int position;
        private final Class<?> columnClass;

        ProcedureColumnMetaData(int position, Class<?> columnClass) {
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
