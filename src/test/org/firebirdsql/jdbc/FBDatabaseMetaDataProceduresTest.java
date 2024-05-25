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
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiFunction;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link FBDatabaseMetaData} for procedure related metadata.
 * 
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataProceduresTest {

    private static final String CREATE_NORMAL_PROC_NO_RETURN = """
            CREATE PROCEDURE normal_proc_no_return
             ( param1 VARCHAR(100))
            AS
            DECLARE VARIABLE dummy INTEGER;
            BEGIN
              dummy = 1 + 1;
            END""";

    private static final String CREATE_NORMAL_PROC_WITH_RETURN = """
            CREATE PROCEDURE normal_proc_with_return
             ( param1 VARCHAR(100))
            RETURNS (return1 VARCHAR(200), return2 INTEGER)
            AS
            BEGIN
              return1 = param1 || param1;
              return2 = 1;
            END""";

    private static final String CREATE_QUOTED_PROC_NO_RETURN = """
            CREATE PROCEDURE "quoted_proc_no_return"
             ( param1 VARCHAR(100))
            AS
            DECLARE VARIABLE dummy INTEGER;
            BEGIN
              dummy = 1 + 1;
            END""";

    private static final String ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN =
            "COMMENT ON PROCEDURE normal_proc_with_return IS 'Some comment'";

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

    private static final MetadataResultSetDefinition getProceduresDefinition =
            new MetadataResultSetDefinition(ProcedureMetaData.class);

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
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        var createDDL = new ArrayList<String>();
        for (ProcedureTestData testData : ProcedureTestData.values()) {
            if (testData.include(supportInfo)) {
                createDDL.addAll(testData.getCreateDDL());
            }
        }
        return createDDL;
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of
     * getProcedures().
     */
    @Test
    void testProcedureMetaDataColumns() throws Exception {
        try (ResultSet procedures = dbmd.getProcedures(null, null, "doesnotexist")) {
            getProceduresDefinition.validateResultSetColumns(procedures);
        }
    }

    /**
     * Tests getProcedures() with procedureName null, expecting all procedures to be returned.
     */
    @Test
    void testProcedureMetaData_all_procedureName_null() throws Exception {
        validateProcedureMetaData_everything(null);
    }

    /**
     * Tests getProcedures() with procedureName all pattern (%), expecting all procedures to be returned.
     */
    @Test
    void testProcedureMetaData_all_procedureName_allPattern() throws Exception {
        validateProcedureMetaData_everything("%");
    }

    private void validateProcedureMetaData_everything(String procedureNamePattern) throws Exception {
        ResultSet procedures = dbmd.getProcedures(null, null, procedureNamePattern);
        var expectedProcedures = List.of(
                ProcedureTestData.NORMAL_PROC_NO_RETURN, 
                ProcedureTestData.NORMAL_PROC_WITH_RETURN, 
                ProcedureTestData.QUOTED_PROC_NO_RETURN);
        try {
            validateProcedures(procedures, expectedProcedures);
        } finally {
            closeQuietly(procedures);
        }
    }
    
    /**
     * Tests getProcedures with specific procedure name, expecting only that specific procedure to be returned.
     */
    @Test
    void testProcedureMetaData_specificProcedure() throws Exception {
        var expectedProcedures = List.of(ProcedureTestData.NORMAL_PROC_WITH_RETURN);
        ResultSet procedures = dbmd.getProcedures(null, null, expectedProcedures.get(0).getName());
        validateProcedures(procedures, expectedProcedures);
    }
    
    /**
     * Tests getProcedures with specific procedure name (quoted), expecting only that specific procedure to be returned.
     */
    @Test
    void testProcedureMetaData_specificProcedureQuoted() throws Exception {
        var expectedProcedures = List.of(ProcedureTestData.QUOTED_PROC_NO_RETURN);
        ResultSet procedures = dbmd.getProcedures(null, null, expectedProcedures.get(0).getName());
        validateProcedures(procedures, expectedProcedures);
    }

    @Test
    void testProcedureMetaData_useCatalogAsPackage_everything() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();

            var expectedProcedures = List.of(
                    ProcedureTestData.NORMAL_PROC_NO_RETURN,
                    ProcedureTestData.NORMAL_PROC_WITH_RETURN,
                    ProcedureTestData.QUOTED_PROC_NO_RETURN,
                    ProcedureTestData.PROCEDURE_IN_PACKAGE);

            ResultSet procedures = dbmd.getProcedures(null, null, null);

            validateProcedures(procedures, expectedProcedures,
                    FBDatabaseMetaDataProceduresTest::modifyForUseCatalogAsPackage);
        }
    }

    @Test
    void testProcedureMetaData_useCatalogAsPackage_specificPackage() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();

            var expectedProcedures = List.of(ProcedureTestData.PROCEDURE_IN_PACKAGE);

            ResultSet procedures = dbmd.getProcedures("WITH$PROCEDURE", null, null);

            validateProcedures(procedures, expectedProcedures,
                    FBDatabaseMetaDataProceduresTest::modifyForUseCatalogAsPackage);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "WITH$PROCEDURE")
    void testProcedureMetaData_useCatalogAsPackage_specificPackageProcedure(String catalog) throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();

            var expectedProcedures = List.of(ProcedureTestData.PROCEDURE_IN_PACKAGE);

            ResultSet procedures = dbmd.getProcedures(catalog, null, "IN$PACKAGE");

            validateProcedures(procedures, expectedProcedures,
                    FBDatabaseMetaDataProceduresTest::modifyForUseCatalogAsPackage);
        }
    }

    @Test
    void testProcedureMetaData_useCatalogAsPackage_nonPackagedOnly() throws Exception {
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        assumeTrue(supportInfo.supportsPackages(), "Test requires package support");
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useCatalogAsPackage, "true");
        try (var connection = DriverManager.getConnection(getUrl(), props)) {
            dbmd = connection.getMetaData();

            var expectedProcedures = List.of(
                    ProcedureTestData.NORMAL_PROC_NO_RETURN,
                    ProcedureTestData.NORMAL_PROC_WITH_RETURN,
                    ProcedureTestData.QUOTED_PROC_NO_RETURN);

            ResultSet procedures = dbmd.getProcedures("", null, null);

            validateProcedures(procedures, expectedProcedures,
                    FBDatabaseMetaDataProceduresTest::modifyForUseCatalogAsPackage);
        }
    }
    
    // TODO Add tests for more complex patterns

    /**
     * Validates the procedures resultset against expectedProcedures.
     * 
     * @param procedures ResultSet with the procedure metadata.
     * @param expectedProcedures List of expected procedures (in expected order)
     */
    private void validateProcedures(ResultSet procedures, List<ProcedureTestData> expectedProcedures) throws Exception {
        validateProcedures(procedures, expectedProcedures, (p, m) -> m);
    }

    private void validateProcedures(ResultSet procedures, List<ProcedureTestData> expectedProcedures,
            BiFunction<ProcedureTestData, Map<ProcedureMetaData, Object>, Map<ProcedureMetaData, Object>> transform)
            throws Exception {
        try {
            int procedureCount = 0;
            while(procedures.next()) {
                if (isIgnoredProcedure(procedures.getString("SPECIFIC_NAME"))) continue;
                if (procedureCount < expectedProcedures.size()) {
                    ProcedureTestData expectedProcedure = expectedProcedures.get(procedureCount);
                    Map<ProcedureMetaData, Object> rules = transform.apply(expectedProcedure,
                            expectedProcedure.getSpecificValidationRules(getDefaultValueValidationRules()));
                    getProceduresDefinition.checkValidationRulesComplete(rules);
                    getProceduresDefinition.validateRowValues(procedures, rules);
                }
                procedureCount++;
            }
            assertEquals(expectedProcedures.size(), procedureCount, "Unexpected number of procedures returned");
        } finally {
            closeQuietly(procedures);
        }
    }

    static boolean isIgnoredProcedure(String specificName) {
        class Ignored {
            // Skipping procedures from system packages (when testing with useCatalogAsPackage=true)
            private static final List<String> PREFIXES_TO_IGNORE =
                    List.of("\"RDB$BLOB_UTIL\".", "\"RDB$PROFILER\".", "\"RDB$TIME_ZONE_UTIL\".", "\"RDB$SQL\".");
        }
        return Ignored.PREFIXES_TO_IGNORE.stream().anyMatch(specificName::startsWith);
    }

    static Map<ProcedureMetaData, Object> modifyForUseCatalogAsPackage(ProcedureTestData procedureTestData,
            Map<ProcedureMetaData, Object> rules) {
        if (!procedureTestData.isInPackage()) {
            rules.put(ProcedureMetaData.PROCEDURE_CAT, "");
        }
        return rules;
    }

    private static final Map<ProcedureMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<ProcedureMetaData, Object> defaults = new EnumMap<>(ProcedureMetaData.class);
        defaults.put(ProcedureMetaData.PROCEDURE_CAT, null);
        defaults.put(ProcedureMetaData.PROCEDURE_SCHEM, null);
        defaults.put(ProcedureMetaData.FUTURE1, null);
        defaults.put(ProcedureMetaData.FUTURE2, null);
        defaults.put(ProcedureMetaData.FUTURE3, null);
        defaults.put(ProcedureMetaData.REMARKS, null);

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<ProcedureMetaData, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    /**
     * Columns defined for the getProcedures() metadata.
     */
    private enum ProcedureMetaData implements MetaDataInfo {
        PROCEDURE_CAT(1, String.class),
        PROCEDURE_SCHEM(2, String.class), 
        PROCEDURE_NAME(3, String.class),
        FUTURE1(4, String.class),
        FUTURE2(5, String.class),
        FUTURE3(6, String.class),
        REMARKS(7, String.class),
        PROCEDURE_TYPE(8, Short.class),
        SPECIFIC_NAME(9, String.class)
        ;

        private final int position;
        private final Class<?> columnClass;

        ProcedureMetaData(int position, Class<?> columnClass) {
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

    private enum ProcedureTestData {
        NORMAL_PROC_NO_RETURN("normal_proc_no_return", List.of(CREATE_NORMAL_PROC_NO_RETURN)) {
            @Override
            Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules) {
                rules.put(ProcedureMetaData.PROCEDURE_NAME, "NORMAL_PROC_NO_RETURN");
                rules.put(ProcedureMetaData.PROCEDURE_TYPE, DatabaseMetaData.procedureNoResult);
                rules.put(ProcedureMetaData.SPECIFIC_NAME, "NORMAL_PROC_NO_RETURN");
                return rules;
            }
        },
        NORMAL_PROC_WITH_RETURN("NORMAL_PROC_WITH_RETURN",
                List.of(CREATE_NORMAL_PROC_WITH_RETURN, ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN)) {
            @Override
            Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules) {
                rules.put(ProcedureMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
                rules.put(ProcedureMetaData.PROCEDURE_TYPE, DatabaseMetaData.procedureReturnsResult);
                rules.put(ProcedureMetaData.REMARKS, "Some comment");
                rules.put(ProcedureMetaData.SPECIFIC_NAME, "NORMAL_PROC_WITH_RETURN");
                return rules;
            }

        },
        QUOTED_PROC_NO_RETURN("quoted_proc_no_return", List.of(CREATE_QUOTED_PROC_NO_RETURN)) {
            @Override
            Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules) {
                rules.put(ProcedureMetaData.PROCEDURE_NAME, "quoted_proc_no_return");
                rules.put(ProcedureMetaData.PROCEDURE_TYPE, DatabaseMetaData.procedureNoResult);
                rules.put(ProcedureMetaData.SPECIFIC_NAME, "quoted_proc_no_return");
                return rules;
            }
        },
        PROCEDURE_IN_PACKAGE("WITH$PROCEDURE.IN$PACKAGE",
                List.of(CREATE_PACKAGE_WITH_PROCEDURE, CREATE_PACKAGE_BODY_WITH_PROCEDURE)) {
            @Override
            Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules) {
                rules.put(ProcedureMetaData.PROCEDURE_CAT, "WITH$PROCEDURE");
                rules.put(ProcedureMetaData.PROCEDURE_NAME, "IN$PACKAGE");
                rules.put(ProcedureMetaData.PROCEDURE_TYPE, DatabaseMetaData.procedureReturnsResult);
                rules.put(ProcedureMetaData.SPECIFIC_NAME, "\"WITH$PROCEDURE\".\"IN$PACKAGE\"");
                return rules;
            }

            @Override
            boolean include(FirebirdSupportInfo supportInfo) {
                return supportInfo.supportsPackages();
            }

            @Override
            boolean isInPackage() {
                return true;
            }
        };

        private final String originalProcedureName;
        private final List<String> createDDL;

        ProcedureTestData(String originalProcedureName, List<String> createDDL) {
            this.originalProcedureName = originalProcedureName;
            this.createDDL = createDDL;
        }

        /**
         * @return Name of the procedure in as defined in the DDL script (including case).
         */
        String getName() {
            return originalProcedureName;
        }

        /**
         * @return List of DDL script(s) for creating the procedure
         */
        List<String> getCreateDDL() {
            return createDDL;
        }

        /**
         * Check if the definition should be included for the current Firebird server.
         *
         * @param supportInfo
         *         Firebird support info of the current Firebird server
         * @return {@code true} if this procedure should be included, {@code false} if it should be excluded
         */
        boolean include(FirebirdSupportInfo supportInfo) {
            return true;
        }

        /**
         * @return {@code true} if this is a packaged procedure, otherwise {@code false}
         */
        boolean isInPackage() {
            return false;
        }

        /**
         * @param rules
         *         The default validation rules (to be modified by this method)
         * @return Map of validation rules specific to this procedure
         */
        abstract Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules);
    }
}
