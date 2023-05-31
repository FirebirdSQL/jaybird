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
import org.firebirdsql.util.FirebirdSupportInfo;
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
import java.util.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link java.sql.DatabaseMetaData#getFunctions(String, String, String)}.
 *
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataFunctionsTest {

    private static final String CREATE_UDF_EXAMPLE = "declare external function UDF$EXAMPLE\n"
            + "int by descriptor, int by descriptor\n"
            + "returns int by descriptor\n"
            + "entry_point 'idNvl' module_name 'fbudf'";

    private static final String ADD_COMMENT_ON_UDF_EXAMPLE =
            "comment on external function UDF$EXAMPLE is 'Comment on UDF$EXAMPLE'";

    private static final String CREATE_PSQL_EXAMPLE = "create function PSQL$EXAMPLE(X int) returns int\n"
            + "as\n"
            + "begin\n"
            + "  return X+1;\n"
            + "end";

    private static final String ADD_COMMENT_ON_PSQL_EXAMPLE =
            "comment on function PSQL$EXAMPLE is 'Comment on PSQL$EXAMPLE'";

    private static final String CREATE_PACKAGE_WITH_FUNCTION = "create package WITH$FUNCTION\n"
            + "as\n"
            + "begin\n"
            + "  function IN$PACKAGE(PARAM1 integer) returns INTEGER;\n"
            + "end";

    private static final String CREATE_PACKAGE_BODY_WITH_FUNCTION = "create package body WITH$FUNCTION\n"
            + "as\n"
            + "begin\n"
            + "  function IN$PACKAGE(PARAM1 integer) returns INTEGER\n"
            + "  as\n"
            + "  begin\n"
            + "    return PARAM1 + 1;\n"
            + "  end\n"
            + "end";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getCreateStatements());

    private static final MetaDataTestSupport<FunctionMetaData> metaDataTestSupport =
            new MetaDataTestSupport<>(FunctionMetaData.class, EnumSet.allOf(FunctionMetaData.class));
    // Skipping RDB$GET_CONTEXT and RDB$SET_CONTEXT as that seems to be an implementation artifact:
    // present in FB 2.5, absent in FB 3.0
    private static final Set<String> FUNCTIONS_TO_IGNORE = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("RDB$GET_CONTEXT", "RDB$SET_CONTEXT")));

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
        FirebirdSupportInfo supportInfo = getDefaultSupportInfo();
        List<String> statements = new ArrayList<>();
        statements.add(CREATE_UDF_EXAMPLE);
        if (supportInfo.supportsComment()) {
            statements.add(ADD_COMMENT_ON_UDF_EXAMPLE);
        }
        if (supportInfo.supportsPsqlFunctions()) {
            statements.add(CREATE_PSQL_EXAMPLE);
            if (supportInfo.supportsComment()) {
                statements.add(ADD_COMMENT_ON_PSQL_EXAMPLE);
            }
            if (supportInfo.supportsPackages()) {
                // Functions in packages should not show up in results
                statements.add(CREATE_PACKAGE_WITH_FUNCTION);
                statements.add(CREATE_PACKAGE_BODY_WITH_FUNCTION);
            }
        }
        // TODO See if we can add a UDR example as well.
        return statements;
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of getFunctions().
     */
    @Test
    void testFunctionMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getFunctions(null, null, "doesnotexist")) {
            metaDataTestSupport.validateResultSetColumns(columns);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = "%")
    @NullSource
    void testFunctionMetadata_everything_functionNamePattern(String functionNamePattern) throws Exception {
        try (ResultSet functions = dbmd.getFunctions(null, null, functionNamePattern)) {
            if (getDefaultSupportInfo().supportsPsqlFunctions()) {
                expectNextFunction(functions);
                validatePsqlExample(functions);
            }

            // Verify UDF$EXAMPLE
            expectNextFunction(functions);
            validateUdfExample(functions);

            expectNoMoreRows(functions);
        }
    }

    @Test
    void testFunctionMetaData_udfExampleOnly() throws Exception {
        try (ResultSet functions = dbmd.getFunctions(null, null, "UDF$EXAMPLE")) {
            assertTrue(functions.next(), "Expected a row");
            validateUdfExample(functions);
            assertFalse(functions.next(), "Expected no more rows");
        }
    }

    @Test
    void testFunctionMetaData_psqlExampleOnly() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPsqlFunctions(), "Requires PSQL function support");
        try (ResultSet functions = dbmd.getFunctions(null, null, "PSQL$EXAMPLE")) {
            assertTrue(functions.next(), "Expected a row");
            validatePsqlExample(functions);
            assertFalse(functions.next(), "Expected no more rows");
        }
    }

    @Test
    void testFunctionMetaData_caseSensitivity_udfExampleNotFound_with_lowercase() throws Exception {
        validateNoRows("udf$example");
    }

    @Test
    void testFunctionMetaData_functionInPackageNotFound() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsPackages(), "Requires package support");
        validateNoRows("IN$PACKAGE");
        validateNoRows("%IN$PACKAGE%");
    }

    @Test
    void testFunctionMetaData_emptyString_noResults() throws Exception {
        validateNoRows("");
    }

    private void validateNoRows(String functionNamePattern) throws Exception {
        try (ResultSet functions = dbmd.getFunctions(null, null, functionNamePattern)) {
            assertFalse(functions.next(), "Expected no rows");
        }
    }

    private void validatePsqlExample(ResultSet functions) throws SQLException {
        final boolean supportsComments = getDefaultSupportInfo().supportsComment();
        Map<FunctionMetaData, Object> psqlExampleRules = getDefaultValidationRules();
        psqlExampleRules.put(FunctionMetaData.FUNCTION_NAME, "PSQL$EXAMPLE");
        psqlExampleRules.put(FunctionMetaData.SPECIFIC_NAME, "PSQL$EXAMPLE");
        if (supportsComments) {
            psqlExampleRules.put(FunctionMetaData.REMARKS, "Comment on PSQL$EXAMPLE");
        }
        psqlExampleRules.put(FunctionMetaData.JB_FUNCTION_SOURCE, "begin\n"
                + "  return X+1;\n"
                + "end");
        psqlExampleRules.put(FunctionMetaData.JB_FUNCTION_KIND, "PSQL");

        metaDataTestSupport.validateRowValues(functions, psqlExampleRules);
    }

    private void validateUdfExample(ResultSet functions) throws SQLException {
        final boolean supportsComments = getDefaultSupportInfo().supportsComment();
        Map<FunctionMetaData, Object> udfExampleRules = getDefaultValidationRules();
        udfExampleRules.put(FunctionMetaData.FUNCTION_NAME, "UDF$EXAMPLE");
        udfExampleRules.put(FunctionMetaData.SPECIFIC_NAME, "UDF$EXAMPLE");
        if (supportsComments) {
            udfExampleRules.put(FunctionMetaData.REMARKS, "Comment on UDF$EXAMPLE");
        }
        udfExampleRules.put(FunctionMetaData.JB_FUNCTION_KIND, "UDF");
        udfExampleRules.put(FunctionMetaData.JB_MODULE_NAME, "fbudf");
        udfExampleRules.put(FunctionMetaData.JB_ENTRYPOINT, "idNvl");
        metaDataTestSupport.validateRowValues(functions, udfExampleRules);
    }

    private void expectNextFunction(ResultSet rs) throws SQLException {
        assertTrue(rs.next(), "Expected a row");
        while (FUNCTIONS_TO_IGNORE.contains(rs.getString("FUNCTION_NAME"))) {
            assertTrue(rs.next(), "Expected a row");
        }
    }

    private void expectNoMoreRows(ResultSet rs) throws SQLException {
        boolean hasRow;
        //noinspection AssignmentUsedAsCondition
        while ((hasRow = rs.next())) {
            if (!FUNCTIONS_TO_IGNORE.contains(rs.getString("FUNCTION_NAME"))) {
                break;
            }
        }
        assertFalse(hasRow, "Expected no more rows");
    }

    private static final Map<FunctionMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<FunctionMetaData, Object> defaults = new EnumMap<>(FunctionMetaData.class);
        defaults.put(FunctionMetaData.FUNCTION_CAT, null);
        defaults.put(FunctionMetaData.FUNCTION_SCHEM, null);
        defaults.put(FunctionMetaData.REMARKS, null);
        defaults.put(FunctionMetaData.FUNCTION_TYPE, (short) DatabaseMetaData.functionNoTable);
        defaults.put(FunctionMetaData.JB_FUNCTION_SOURCE, null);
        defaults.put(FunctionMetaData.JB_MODULE_NAME, null);
        defaults.put(FunctionMetaData.JB_ENTRYPOINT, null);
        defaults.put(FunctionMetaData.JB_ENGINE_NAME, null);

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<FunctionMetaData, Object> getDefaultValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }

    private enum FunctionMetaData implements MetaDataInfo {
        FUNCTION_CAT(1, String.class),
        FUNCTION_SCHEM(2, String.class),
        FUNCTION_NAME(3, String.class),
        REMARKS(4, String.class),
        FUNCTION_TYPE(5, Short.class),
        SPECIFIC_NAME(6, String.class),
        JB_FUNCTION_SOURCE(7, String.class),
        JB_FUNCTION_KIND(8, String.class),
        JB_MODULE_NAME(9, String.class),
        JB_ENTRYPOINT(10, String.class),
        JB_ENGINE_NAME(11, String.class);

        private final int position;
        private final Class<?> columnClass;

        FunctionMetaData(int position, Class<?> columnClass) {
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
