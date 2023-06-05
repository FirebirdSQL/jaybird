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
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jdbc.metadata.FbMetadataConstants;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.jdbc.FBDatabaseMetaDataProceduresTest.isIgnoredProcedure;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link FBDatabaseMetaData} for procedure columns related metadata.
 * 
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataProcedureColumnsTest {
    
    // TODO This test will need to be expanded with version dependent features 
    // (eg TYPE OF <domain> (2.1), TYPE OF COLUMN <table.column> (2.5), NOT NULL (2.1), DEFAULT <value> (2.0)

    private static final String CREATE_NORMAL_PROC_NO_ARG_NO_RETURN = """
            CREATE PROCEDURE proc_no_arg_no_return
            AS
            DECLARE VARIABLE dummy INTEGER;
            BEGIN
              dummy = 1 + 1;
            END""";
    
    private static final String CREATE_NORMAL_PROC_NO_RETURN = """
            CREATE PROCEDURE normal_proc_no_return
             ( param1 VARCHAR(100),
               "param2" INTEGER)
            AS
            DECLARE VARIABLE dummy INTEGER;
            BEGIN
              dummy = 1 + 1;
            END""";

    private static final String CREATE_NORMAL_PROC_WITH_RETURN = """
            CREATE PROCEDURE normal_proc_with_return
             ( param1 VARCHAR(100),
               param2 DECIMAL(18,2),
               param3 NUMERIC(4,3),
               param4 TIMESTAMP)
            RETURNS (return1 VARCHAR(200), return2 INTEGER, "return3" DOUBLE PRECISION)
            AS
            BEGIN
              return1 = param1 || param1;
              return2 = 1;
            END""";

    private static final String CREATE_QUOTED_PROC_NO_RETURN = """
            CREATE PROCEDURE "quoted_proc_no_return"
             ( param1 VARCHAR(100),
               param2 VARCHAR(100) default 'param2 default')
            AS
            DECLARE VARIABLE dummy INTEGER;
            BEGIN
              dummy = 1 + 1;
            END""";
    
    private static final String ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN_PARAM2 =
            "COMMENT ON PARAMETER normal_proc_with_return.param2 IS 'Some comment'";

    private static final String CREATE_PACKAGE_WITH_PROCEDURE = """
            create package WITH$PROCEDURE
            as
            begin
              procedure IN$PACKAGE(PARAM1 integer) returns (return1 INTEGER);
            end""";

    private static final String CREATE_PACKAGE_BODY_WITH_PROCEDURE = """
            create package body WITH$PROCEDURE
            as
            begin
              procedure IN$PACKAGE(PARAM1 integer) returns (RETURN1 integer)
              as
              begin
                RETURN1 = PARAM1 * PARAM1;
              end
            end""";

    private static final MetadataResultSetDefinition getProcedureColumnsDefinition =
            new MetadataResultSetDefinition(ProcedureColumnMetaData.class);

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getCreateStatements());

    private static Connection con;
    private DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        con = getConnectionViaDriverManager();
    }

    @BeforeEach
    void setup() throws SQLException {
        dbmd = con.getMetaData();
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        try {
            con.close();
        } finally {
            con = null;
        }
    }

    private static List<String> getCreateStatements() {
        var statements = new ArrayList<>(Arrays.asList(
                CREATE_NORMAL_PROC_NO_ARG_NO_RETURN,
                CREATE_NORMAL_PROC_NO_RETURN,
                CREATE_NORMAL_PROC_WITH_RETURN,
                CREATE_QUOTED_PROC_NO_RETURN));
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        if (supportInfo.supportsComment()) {
            statements.add(ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN_PARAM2);
        }
        if (supportInfo.supportsPackages()) {
            statements.add(CREATE_PACKAGE_WITH_PROCEDURE);
            statements.add(CREATE_PACKAGE_BODY_WITH_PROCEDURE);
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
            getProcedureColumnsDefinition.validateResultSetColumns(procedureColumns);
        }
    }
    
    /**
     * Tests getProcedureColumn with proc_no_arg_no_return, expecting empty ResultSet.
     */
    @Test
    void testProcedureColumns_noArg_noReturn() throws Exception {
        try (ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "proc_no_arg_no_return", "%")) {
            assertFalse(procedureColumns.next(), "Expected empty result set for procedure with arguments or return values");
        }
    }
    
    /**
     * Tests getProcedureColumn with normal_proc_no_return using all columnPattern, expecting result set with all defined rows.
     */
    @Test
    void testProcedureColumns_normalProc_noReturn_allPattern() throws Exception {
        var expectedColumns = getNormalProcNoReturn_allColumns();
        
        ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "NORMAL_PROC_NO_RETURN", "%");
        validate(procedureColumns, expectedColumns);        
    }

    private static List<Map<ProcedureColumnMetaData, Object>> getNormalProcNoReturn_allColumns() {
        return List.of(
                createStringType(Types.VARCHAR, "NORMAL_PROC_NO_RETURN", "PARAM1", 1, 100, true,
                        DatabaseMetaData.procedureColumnIn),
                createNumericalType(Types.INTEGER, "NORMAL_PROC_NO_RETURN", "param2", 2, INTEGER_PRECISION, 0, true,
                        DatabaseMetaData.procedureColumnIn));
    }

    /**
     * Tests getProcedureColumn with normal_proc_no_return and specific column pattern, expecting result set with one row.
     */
    @ParameterizedTest
    @ValueSource(strings = { "param2", "%2", "param_" })
    void testProcedureColumns_normalProc_noReturn_secondColumn(String columnNamePattern) throws Exception {
        var expectedColumns = List.of(
                createNumericalType(Types.INTEGER, "NORMAL_PROC_NO_RETURN", "param2", 2, INTEGER_PRECISION, 0, true,
                        DatabaseMetaData.procedureColumnIn));

        ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "NORMAL_PROC_NO_RETURN", columnNamePattern);
        validate(procedureColumns, expectedColumns);
    }

    /**
     * Tests getProcedureColumn with normal_proc_with_return using columnPattern all (%) string, expecting result set with all defined rows.
     */
    @Test
    void testProcedureColumns_normalProc_withReturn_allPattern() throws Exception {
        var expectedColumns = getNormalProcWithReturn_allColumns();
        
        ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "NORMAL_PROC_WITH_RETURN", "%");
        validate(procedureColumns, expectedColumns); 
    }

    private static List<Map<ProcedureColumnMetaData, Object>> getNormalProcWithReturn_allColumns() {
        return List.of(
                // TODO Having result columns first might be against JDBC spec
                // TODO Describing result columns as procedureColumnOut might be against JDBC spec
                createStringType(Types.VARCHAR, "NORMAL_PROC_WITH_RETURN", "RETURN1", 1, 200, true,
                        DatabaseMetaData.procedureColumnOut),
                createNumericalType(Types.INTEGER, "NORMAL_PROC_WITH_RETURN", "RETURN2", 2, INTEGER_PRECISION, 0, true,
                        DatabaseMetaData.procedureColumnOut),
                createDouble("NORMAL_PROC_WITH_RETURN", "return3", 3, true, DatabaseMetaData.procedureColumnOut),
                createStringType(Types.VARCHAR, "NORMAL_PROC_WITH_RETURN", "PARAM1", 1, 100, true,
                        DatabaseMetaData.procedureColumnIn),
                withRemark(createNumericalType(Types.DECIMAL, "NORMAL_PROC_WITH_RETURN", "PARAM2", 2,
                                NUMERIC_BIGINT_PRECISION, 2, true, DatabaseMetaData.procedureColumnIn),
                        "Some comment"),
                createNumericalType(Types.NUMERIC, "NORMAL_PROC_WITH_RETURN", "PARAM3", 3, NUMERIC_SMALLINT_PRECISION,
                        3, true, DatabaseMetaData.procedureColumnIn),
                createDateTime(Types.TIMESTAMP, "NORMAL_PROC_WITH_RETURN", "PARAM4", 4, true,
                        DatabaseMetaData.procedureColumnIn));
    }

    /**
     * Tests getProcedureColumn with quoted_proc_no_return using all columnPattern, expecting result set with all defined rows.
     */
    @Test
    void testProcedureColumns_quotedProc_noReturn_allPattern() throws Exception {
        var expectedColumns = getQuotedProcNoReturn_allColumns();
        
        ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, "quoted_proc_no_return", "%");
        validate(procedureColumns, expectedColumns);        
    }

    private static List<Map<ProcedureColumnMetaData, Object>> getQuotedProcNoReturn_allColumns() {
        return List.of(
                createStringType(Types.VARCHAR, "quoted_proc_no_return", "PARAM1", 1, 100, true,
                        DatabaseMetaData.procedureColumnIn),
                withDefault("'param2 default'",
                        createStringType(Types.VARCHAR, "quoted_proc_no_return", "PARAM2", 2, 100, true,
                                DatabaseMetaData.procedureColumnIn)));
    }

    @Test
    void testProcedureColumns_byColumnNameOnly() throws Exception {
        ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, null, "PARAM4");

        validate(procedureColumns,
                singletonList(createDateTime(Types.TIMESTAMP, "NORMAL_PROC_WITH_RETURN", "PARAM4", 4, true,
                        DatabaseMetaData.procedureColumnIn)));
    }

    @Test
    void testProcedureColumns_useCatalogAsPackage_everything() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();

            var expectedColumns = new ArrayList<>(getNormalProcNoReturn_allColumns());
            expectedColumns.addAll(getNormalProcWithReturn_allColumns());
            expectedColumns.addAll(getQuotedProcNoReturn_allColumns());
            withCatalog("", expectedColumns);
            expectedColumns.addAll(getInPackage_allColumns());

            ResultSet procedureColumns = dbmd.getProcedureColumns(null, null, null, null);
            validate(procedureColumns, expectedColumns);
        }
    }

    @Test
    void testProcedureColumns_useCatalogAsPackage_specificPackage() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();

            List<Map<ProcedureColumnMetaData, Object>> expectedColumns = getInPackage_allColumns();

            ResultSet procedureColumns = dbmd.getProcedureColumns("WITH$PROCEDURE", null, null, null);
            validate(procedureColumns, expectedColumns);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "WITH$PROCEDURE")
    void testProcedureColumns_useCatalogAsPackage_specificPackageProcedure(String catalog) throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();

            List<Map<ProcedureColumnMetaData, Object>> expectedColumns = getInPackage_allColumns();

            ResultSet procedureColumns = dbmd.getProcedureColumns(catalog, null, "IN$PACKAGE", null);
            validate(procedureColumns, expectedColumns);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "WITH$PROCEDURE")
    void testProcedureColumns_useCatalogAsPackage_specificPackageProcedureColumn(String catalog) throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();

            List<Map<ProcedureColumnMetaData, Object>> expectedColumns =
                    withCatalog("WITH$PROCEDURE",
                            withSpecificName("\"WITH$PROCEDURE\".\"IN$PACKAGE\"",
                                    List.of(createNumericalType(Types.INTEGER, "IN$PACKAGE", "RETURN1", 1, 10, 0, true,
                                                    DatabaseMetaData.procedureColumnOut))));

            ResultSet procedureColumns = dbmd.getProcedureColumns(catalog, null, "IN$PACKAGE", "RETURN1");
            validate(procedureColumns, expectedColumns);
        }
    }

    @Test
    void testProcedureColumns_useCatalogAsPackage_nonPackagedOnly() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();

            var expectedColumns = new ArrayList<>(getNormalProcNoReturn_allColumns());
            expectedColumns.addAll(getNormalProcWithReturn_allColumns());
            expectedColumns.addAll(getQuotedProcNoReturn_allColumns());
            withCatalog("", expectedColumns);

            ResultSet procedureColumns = dbmd.getProcedureColumns("", null, null, null);
            validate(procedureColumns, expectedColumns);
        }
    }

    private static List<Map<ProcedureColumnMetaData, Object>> getInPackage_allColumns() {
        return withCatalog("WITH$PROCEDURE",
                withSpecificName("\"WITH$PROCEDURE\".\"IN$PACKAGE\"",
                        // TODO Having result columns first might be against JDBC spec
                        // TODO Describing result columns as procedureColumnOut might be against JDBC spec
                        List.of(
                                createNumericalType(Types.INTEGER, "IN$PACKAGE", "RETURN1", 1, 10, 0, true,
                                        DatabaseMetaData.procedureColumnOut),
                                createNumericalType(Types.INTEGER, "IN$PACKAGE", "PARAM1", 1, 10, 0, true,
                                        DatabaseMetaData.procedureColumnIn))));
    }
    
    // TODO Add tests for more complex patterns for procedure and column
    
    private void validate(ResultSet procedureColumns, List<Map<ProcedureColumnMetaData, Object>> expectedColumns)
            throws Exception {
        try {
            int parameterCount = 0;
            while(procedureColumns.next()) {
                if (isIgnoredProcedure(procedureColumns.getString("SPECIFIC_NAME"))) continue;
                if (parameterCount < expectedColumns.size()) {
                    Map<ProcedureColumnMetaData, Object> rules = expectedColumns.get(parameterCount);
                    getProcedureColumnsDefinition.checkValidationRulesComplete(rules);
                    getProcedureColumnsDefinition.validateRowValues(procedureColumns, rules);
                }
                parameterCount++;
            }
            assertEquals(expectedColumns.size(), parameterCount, "Unexpected number of procedure columns returned");
        } finally {
            closeQuietly(procedureColumns);
        }
    }

    private static Map<ProcedureColumnMetaData, Object> createColumn(String procedureName, String columnName,
            int ordinalPosition, boolean nullable, int columnType) {
        Map<ProcedureColumnMetaData, Object> rules = getDefaultValueValidationRules();
        rules.put(ProcedureColumnMetaData.PROCEDURE_NAME, procedureName);
        rules.put(ProcedureColumnMetaData.SPECIFIC_NAME, procedureName);
        rules.put(ProcedureColumnMetaData.COLUMN_NAME, columnName);
        rules.put(ProcedureColumnMetaData.ORDINAL_POSITION, ordinalPosition);
        rules.put(ProcedureColumnMetaData.COLUMN_TYPE, columnType);
        if (!nullable) {
            rules.put(ProcedureColumnMetaData.NULLABLE, DatabaseMetaData.procedureNoNulls);
            rules.put(ProcedureColumnMetaData.IS_NULLABLE, "NO");
        }
        return rules;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<ProcedureColumnMetaData, Object> createStringType(int jdbcType, String procedureName,
            String columnName, int ordinalPosition, int length, boolean nullable, int columnType) {
        Map<ProcedureColumnMetaData, Object> rules =
                createColumn(procedureName, columnName, ordinalPosition, nullable, columnType);
        rules.put(ProcedureColumnMetaData.DATA_TYPE, jdbcType);
        String typeName = switch (jdbcType) {
            case Types.CHAR, Types.BINARY -> "CHAR";
            case Types.VARCHAR, Types.VARBINARY -> "VARCHAR";
            default -> throw new IllegalArgumentException("Wrong type code for createStringType: " + jdbcType);
        };
        rules.put(ProcedureColumnMetaData.TYPE_NAME, typeName);
        rules.put(ProcedureColumnMetaData.PRECISION, length);
        rules.put(ProcedureColumnMetaData.LENGTH, length);
        rules.put(ProcedureColumnMetaData.CHAR_OCTET_LENGTH, length);
        return rules;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<ProcedureColumnMetaData, Object> createNumericalType(int jdbcType, String procedureName,
            String columnName, int ordinalPosition, int precision, int scale, boolean nullable, int columnType) {
        Map<ProcedureColumnMetaData, Object> rules =
                createColumn(procedureName, columnName, ordinalPosition, nullable, columnType);
        rules.put(ProcedureColumnMetaData.DATA_TYPE, jdbcType);
        String typeName;
        int length;
        switch (jdbcType) {
        case Types.BIGINT -> {
            typeName = "BIGINT";
            length = 8;
        }
        case Types.INTEGER -> {
            typeName = "INTEGER";
            length = 4;
        }
        case Types.SMALLINT -> {
            typeName = "SMALLINT";
            length = 2;
        }
        case Types.NUMERIC -> {
            typeName = "NUMERIC";
            length = precision > 5 ? (precision > 9 ? (precision > 18 ? 16 : 8) : 4) : 2;
        }
        case Types.DECIMAL -> {
            typeName = "DECIMAL";
            length = precision > 9 ? (precision > 18 ? 16 : 8) : 4;
        }
        default -> throw new IllegalArgumentException("Wrong type code for createNumericalType: " + jdbcType);
        }
        rules.put(ProcedureColumnMetaData.TYPE_NAME, typeName);
        rules.put(ProcedureColumnMetaData.PRECISION, precision);
        rules.put(ProcedureColumnMetaData.SCALE, scale);
        rules.put(ProcedureColumnMetaData.LENGTH, length);
        return rules;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<ProcedureColumnMetaData, Object> createDateTime(int jdbcType, String procedureName,
            String columnName, int ordinalPosition, boolean nullable, int columnType) {
        Map<ProcedureColumnMetaData, Object> rules =
                createColumn(procedureName, columnName, ordinalPosition, nullable, columnType);
        rules.put(ProcedureColumnMetaData.DATA_TYPE, jdbcType);
        String typeName;
        int precision;
        int length;
        switch (jdbcType) {
        case Types.DATE -> {
            typeName = "DATE";
            precision = DATE_PRECISION;
            length = 4;
        }
        case Types.TIME -> {
            typeName = "TIME";
            precision = TIME_PRECISION;
            length = 4;
        }
        case Types.TIMESTAMP -> {
            typeName = "TIMESTAMP";
            precision = TIMESTAMP_PRECISION;
            length = 8;
        }
        case Types.TIME_WITH_TIMEZONE -> {
            typeName = "TIME WITH TIME ZONE";
            precision = TIME_WITH_TIMEZONE_PRECISION;
            length = 8; // TODO Possibly 6
        }
        case Types.TIMESTAMP_WITH_TIMEZONE -> {
            typeName = "TIMESTAMP WITH TIME ZONE";
            precision = TIMESTAMP_WITH_TIMEZONE_PRECISION;
            length = 12; // TODO Possibly 10
        }
        default -> throw new IllegalArgumentException("Wrong type code for createNumericalType: " + jdbcType);
        }
        rules.put(ProcedureColumnMetaData.TYPE_NAME, typeName);
        rules.put(ProcedureColumnMetaData.PRECISION, precision);
        rules.put(ProcedureColumnMetaData.LENGTH, length);
        return rules;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<ProcedureColumnMetaData, Object> createDouble(String procedureName, String columnName,
            int ordinalPosition, boolean nullable, int columnType) {
        Map<ProcedureColumnMetaData, Object> rules =
                createColumn(procedureName, columnName, ordinalPosition, nullable, columnType);
        rules.put(ProcedureColumnMetaData.DATA_TYPE, Types.DOUBLE);
        rules.put(ProcedureColumnMetaData.TYPE_NAME, "DOUBLE PRECISION");
        if (getDefaultSupportInfo().supportsFloatBinaryPrecision()) {
            rules.put(ProcedureColumnMetaData.PRECISION, 53);
            rules.put(ProcedureColumnMetaData.RADIX, 2);
        } else {
            rules.put(ProcedureColumnMetaData.PRECISION, 15);
        }
        rules.put(ProcedureColumnMetaData.LENGTH, 8);
        return rules;
    }

    @SuppressWarnings("SameParameterValue")
    private static Map<ProcedureColumnMetaData, Object> withRemark(Map<ProcedureColumnMetaData, Object> column,
            String remark) {
        column.put(ProcedureColumnMetaData.REMARKS, remark);
        return column;
    }

    private static Map<ProcedureColumnMetaData, Object> withDefault(String defaultDefinition,
            Map<ProcedureColumnMetaData, Object> rules) {
        rules.put(ProcedureColumnMetaData.COLUMN_DEF, defaultDefinition);
        return rules;
    }

    private static List<Map<ProcedureColumnMetaData, Object>> withCatalog(
            String catalog, List<Map<ProcedureColumnMetaData, Object>> rules) {
        for (Map<ProcedureColumnMetaData, Object> rowRule : rules) {
            rowRule.put(ProcedureColumnMetaData.PROCEDURE_CAT, catalog);
        }
        return rules;
    }

    private static List<Map<ProcedureColumnMetaData, Object>> withSpecificName(
            String specificName, List<Map<ProcedureColumnMetaData, Object>> rules) {
        for (Map<ProcedureColumnMetaData, Object> rowRule : rules) {
            rowRule.put(ProcedureColumnMetaData.SPECIFIC_NAME, specificName);
        }
        return rules;
    }
    
    private static final Map<ProcedureColumnMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<ProcedureColumnMetaData, Object> defaults = new EnumMap<>(ProcedureColumnMetaData.class);
        defaults.put(ProcedureColumnMetaData.PROCEDURE_CAT, null);
        defaults.put(ProcedureColumnMetaData.PROCEDURE_SCHEM, null);
        defaults.put(ProcedureColumnMetaData.SCALE, null);
        defaults.put(ProcedureColumnMetaData.RADIX, FbMetadataConstants.RADIX_DECIMAL);
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
    }
}
