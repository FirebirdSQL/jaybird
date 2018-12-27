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
import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link FBDatabaseMetaData} for procedure related metadata.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDatabaseMetaDataProcedures {

    // TODO Temporary fix for RDB$TIME_ZONE_UTIL.TRANSITIONS in Firebird 4
    private static final Set<String> PROCEDURES_TO_IGNORE = Collections.singleton("TRANSITIONS");

    //@formatter:off
    private static final String CREATE_NORMAL_PROC_NO_RETURN =
            "CREATE PROCEDURE normal_proc_no_return\n" +
            " ( param1 VARCHAR(100))\n" +
            "AS\n" +
            "DECLARE VARIABLE dummy INTEGER;\n" +
            "BEGIN\n" +
            "  dummy = 1 + 1;\n" +
            "END";

    private static final String CREATE_NORMAL_PROC_WITH_RETURN =
            "CREATE PROCEDURE normal_proc_with_return\n" +
            " ( param1 VARCHAR(100))\n" +
            "RETURNS (return1 VARCHAR(200), return2 INTEGER)\n" +
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

    private static final String ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN =
            "COMMENT ON PROCEDURE normal_proc_with_return IS 'Some comment'";
    //@formatter:on

    private static final MetaDataTestSupport<ProcedureMetaData> metaDataTestSupport =
            new MetaDataTestSupport<>(ProcedureMetaData.class);

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase(getCreateStatements());

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

    private static List<String> getCreateStatements() {
        List<String> createDDL = new ArrayList<>();
        for (ProcedureTestData testData : ProcedureTestData.values()) {
            createDDL.addAll(testData.getCreateDDL());
        }
        return createDDL;
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of
     * getProcedures().
     */
    @Test
    public void testProcedureMetaDataColumns() throws Exception {
        try (ResultSet procedures = dbmd.getProcedures(null, null, "doesnotexist")) {
            metaDataTestSupport.validateResultSetColumns(procedures);
        }
    }

    /**
     * Tests getProcedures() with procedureName null, expecting all procedures to be returned.
     */
    @Test
    public void testProcedureMetaData_all_procedureName_null() throws Exception {
        validateProcedureMetaData_everything(null);
    }

    /**
     * Tests getProcedures() with procedureName all pattern (%), expecting all procedures to be returned.
     */
    @Test
    public void testProcedureMetaData_all_procedureName_allPattern() throws Exception {
        validateProcedureMetaData_everything("%");
    }

    private void validateProcedureMetaData_everything(String procedureNamePattern) throws Exception {
        ResultSet procedures = dbmd.getProcedures(null, null, procedureNamePattern);
        List<ProcedureTestData> expectedProcedures = Arrays.asList(
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
    public void testProcedureMetaData_specificProcedure() throws Exception {
        List<ProcedureTestData> expectedProcedures =
                Collections.singletonList(ProcedureTestData.NORMAL_PROC_WITH_RETURN);
        ResultSet procedures = dbmd.getProcedures(null, null, expectedProcedures.get(0).getName());
        validateProcedures(procedures, expectedProcedures);
    }
    
    /**
     * Tests getProcedures with specific procedure name (quoted), expecting only that specific procedure to be returned.
     */
    @Test
    public void testProcedureMetaData_specificProcedureQuoted() throws Exception {
        List<ProcedureTestData> expectedProcedures = Collections.singletonList(ProcedureTestData.QUOTED_PROC_NO_RETURN);
        ResultSet procedures = dbmd.getProcedures(null, null, expectedProcedures.get(0).getName());
        validateProcedures(procedures, expectedProcedures);
    }
    
    // TODO Add tests for more complex patterns

    /**
     * Validates the procedures resultset against expectedProcedures.
     * 
     * @param procedures ResultSet with the procedure metadata.
     * @param expectedProcedures List of expected procedures (in expected order)
     */
    private void validateProcedures(ResultSet procedures, List<ProcedureTestData> expectedProcedures) throws Exception {
        try {
            int procedureCount = 0;
            while(procedures.next()) {
                String name = procedures.getString("PROCEDURE_NAME");
                if (PROCEDURES_TO_IGNORE.contains(name)) {
                    // TODO Temporary workaround
                    continue;
                }
                if (procedureCount < expectedProcedures.size()) {
                    ProcedureTestData expectedProcedure = expectedProcedures.get(procedureCount);
                    Map<ProcedureMetaData, Object> rules = expectedProcedure.getSpecificValidationRules(getDefaultValueValidationRules());
                    metaDataTestSupport.checkValidationRulesComplete(rules);
                    metaDataTestSupport.validateRowValues(procedures, rules);
                }
                procedureCount++;
            }
            assertEquals("Unexpected number of procedures returned", expectedProcedures.size(), procedureCount);
        } finally {
            closeQuietly(procedures);
        }
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

        @Override
        public MetaDataValidator<?> getValidator() {
            return new MetaDataValidator<>(this);
        }
    }

    private enum ProcedureTestData {
        NORMAL_PROC_NO_RETURN("normal_proc_no_return",
                Collections.singletonList(CREATE_NORMAL_PROC_NO_RETURN)) {

            @Override
            Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules) {
                rules.put(ProcedureMetaData.PROCEDURE_NAME, "NORMAL_PROC_NO_RETURN");
                rules.put(ProcedureMetaData.PROCEDURE_TYPE, DatabaseMetaData.procedureNoResult);
                rules.put(ProcedureMetaData.SPECIFIC_NAME, "NORMAL_PROC_NO_RETURN");
                return rules;
            }
        },
        NORMAL_PROC_WITH_RETURN("NORMAL_PROC_WITH_RETURN",
                Arrays.asList(CREATE_NORMAL_PROC_WITH_RETURN, ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN)) {

            @Override
            Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules) {
                rules.put(ProcedureMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
                rules.put(ProcedureMetaData.PROCEDURE_TYPE, DatabaseMetaData.procedureReturnsResult);
                rules.put(ProcedureMetaData.REMARKS, "Some comment");
                rules.put(ProcedureMetaData.SPECIFIC_NAME, "NORMAL_PROC_WITH_RETURN");
                return rules;
            }
        
        },
        QUOTED_PROC_NO_RETURN("quoted_proc_no_return",
                Collections.singletonList(CREATE_QUOTED_PROC_NO_RETURN)) {

            @Override
            Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules) {
                rules.put(ProcedureMetaData.PROCEDURE_NAME, "quoted_proc_no_return");
                rules.put(ProcedureMetaData.PROCEDURE_TYPE, DatabaseMetaData.procedureNoResult);
                rules.put(ProcedureMetaData.SPECIFIC_NAME, "quoted_proc_no_return");
                return rules;
            }
        }
        ;

        private final String originalProcedureName;
        private final List<String> createDDL;

        ProcedureTestData(String originalProcedureName, List<String> createDDL) {
            this.originalProcedureName = originalProcedureName;
            this.createDDL = createDDL;
        }

        /**
         * @return Name of the procedure in as defined in the DDL script
         *         (including case and quotation).
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
         * @param rules The default validation rules (to be modified by this method)
         * @return Map of validation rules specific to this procedure
         */
        abstract Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules);
    }
}
