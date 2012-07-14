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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;

/**
 * Tests for {@link FBDatabaseMetaData} for procedure related metadata.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDatabaseMetaDataProcedures extends
        FBMetaDataTestBase<TestFBDatabaseMetaDataProcedures.ProcedureMetaData> {

    public TestFBDatabaseMetaDataProcedures(String name) {
        super(name, ProcedureMetaData.class);
    }

    public static final String CREATE_NORMAL_PROC_NO_RETURN = 
            "CREATE PROCEDURE normal_proc_no_return\n" +
            " ( param1 VARCHAR(100))\n" +
            "AS\n" +
            "BEGIN\n" +
            "  /* does nothing */\n" +
            "END";

    public static final String CREATE_NORMAL_PROC_WITH_RETURN = 
            "CREATE PROCEDURE normal_proc_with_return\n" +
            " ( param1 VARCHAR(100))\n" +
            "RETURNS (return1 VARCHAR(200), return2 INTEGER)\n" +
            "AS\n" +
            "BEGIN\n" +
            "  return1 = param1 || param1;\n" +
            "  return2 = 1;\n" +
            "END";

    public static final String CREATE_QUOTED_PROC_NO_RETURN = 
            "CREATE PROCEDURE \"quoted_proc_no_return\"\n" +
            " ( param1 VARCHAR(100))\n" +
            "AS\n" +
            "BEGIN\n" +
            "  /* does nothing */\n" +
            "END";

    public static final String ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN = 
            "COMMENT ON PROCEDURE normal_proc_with_return IS 'Some comment'";

    public static final String DROP_NORMAL_PROC_NO_RETURN = 
            "DROP PROCEDURE normal_proc_no_return";

    public static final String DROP_NORMAL_PROC_WITH_RETURN = 
            "DROP PROCEDURE normal_proc_with_return";

    public static final String DROP_QUOTED_PROC_NO_RETURN = 
            "DROP PROCEDURE \"quoted_proc_no_return\"";

    @Override
    protected List<String> getCreateStatements() {
        List<String> createDDL = new LinkedList<String>();
        for (ProcedureTestData testData : ProcedureTestData.values()) {
            createDDL.addAll(testData.getCreateDDL());
        }
        return createDDL;
    }

    @Override
    protected List<String> getDropStatements() {
        List<String> dropDDL = new LinkedList<String>();
        for (ProcedureTestData testData : ProcedureTestData.values()) {
            dropDDL.addAll(testData.getDropDDL());
        }
        return dropDDL;
    }

    /**
     * Tests the ordinal positions and types for the metadata columns of
     * getProcedures().
     */
    public void testProcedureMetaDataColumns() throws Exception {
        ResultSet procedures = dbmd.getProcedures(null, null, null);
        try {
            validateResultSetColumns(procedures);
        } finally {
            closeQuietly(procedures);
        }
    }

    /**
     * Tests getProcedures() with procedureName null, expecting all procedures to be returned.
     */
    public void testProcedureMetaData_all_procedureName_null() throws Exception {
        validateProcedureMetaData_everything(null);
    }
    
    /**
     * Tests getProcedures() with procedureName empty, expecting all procedures to be returned.
     */
    public void testProcedureMetaData_all_procedureName_empty() throws Exception {
        validateProcedureMetaData_everything("");
    }
    
    /**
     * Tests getProcedures() with procedureName all pattern (%), expecting all procedures to be returned.
     */
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
    public void testProcedureMetaData_specificProcedure() throws Exception {
        List<ProcedureTestData> expectedProcedures = Arrays.asList(ProcedureTestData.NORMAL_PROC_WITH_RETURN);
        ResultSet procedures = dbmd.getProcedures(null, null, expectedProcedures.get(0).getName());
        validateProcedures(procedures, expectedProcedures);
    }
    
    /**
     * Tests getProcedures with specific procedure name (quoted), expecting only that specific procedure to be returned.
     */
    public void testProcedureMetaData_specificProcedureQuoted() throws Exception {
        List<ProcedureTestData> expectedProcedures = Arrays.asList(ProcedureTestData.QUOTED_PROC_NO_RETURN);
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
                if (procedureCount < expectedProcedures.size()) {
                    ProcedureTestData expectedProcedure = expectedProcedures.get(procedureCount);
                    Map<ProcedureMetaData, Object> rules = expectedProcedure.getSpecificValidationRules(getDefaultValueValidationRules());
                    checkValidationRulesComplete(rules);
                    validateRowValues(procedures, rules);
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
        Map<ProcedureMetaData, Object> defaults = new EnumMap<ProcedureMetaData, Object>(
                ProcedureMetaData.class);
        defaults.put(ProcedureMetaData.PROCEDURE_CAT, null);
        defaults.put(ProcedureMetaData.PROCEDURE_SCHEM, null);
        defaults.put(ProcedureMetaData.FUTURE1, null);
        defaults.put(ProcedureMetaData.FUTURE2, null);
        defaults.put(ProcedureMetaData.FUTURE3, null);
        defaults.put(ProcedureMetaData.REMARKS, null);

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    @Override
    protected Map<ProcedureMetaData, Object> getDefaultValueValidationRules() throws Exception {
        return new EnumMap<ProcedureMetaData, Object>(DEFAULT_COLUMN_VALUES);
    }

    /**
     * Columns defined for the getProcedures() metadata.
     */
    enum ProcedureMetaData implements MetaDataInfo {
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

        private ProcedureMetaData(int position, Class<?> columnClass) {
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
            return new MetaDataValidator<ProcedureMetaData>(this);
        }
    }

    private enum ProcedureTestData {
        NORMAL_PROC_NO_RETURN("normal_proc_no_return", 
                Arrays.asList(CREATE_NORMAL_PROC_NO_RETURN),
                Arrays.asList(DROP_NORMAL_PROC_NO_RETURN)) {

            @Override
            Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules) {
                rules.put(ProcedureMetaData.PROCEDURE_NAME, "NORMAL_PROC_NO_RETURN");
                rules.put(ProcedureMetaData.PROCEDURE_TYPE, DatabaseMetaData.procedureNoResult);
                rules.put(ProcedureMetaData.SPECIFIC_NAME, "NORMAL_PROC_NO_RETURN");
                return rules;
            }
        },
        NORMAL_PROC_WITH_RETURN("normal_proc_with_return", 
                Arrays.asList(CREATE_NORMAL_PROC_WITH_RETURN, ADD_COMMENT_ON_NORMAL_PROC_WITH_RETURN), 
                Arrays.asList(DROP_NORMAL_PROC_WITH_RETURN)) {

            @Override
            Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules) {
                rules.put(ProcedureMetaData.PROCEDURE_NAME, "NORMAL_PROC_WITH_RETURN");
                rules.put(ProcedureMetaData.PROCEDURE_TYPE, DatabaseMetaData.procedureReturnsResult);
                rules.put(ProcedureMetaData.REMARKS, "Some comment");
                rules.put(ProcedureMetaData.SPECIFIC_NAME, "NORMAL_PROC_WITH_RETURN");
                return rules;
            }
        
        },
        QUOTED_PROC_NO_RETURN("\"quoted_proc_no_return\"",
                Arrays.asList(CREATE_QUOTED_PROC_NO_RETURN),
                Arrays.asList(DROP_QUOTED_PROC_NO_RETURN)) {

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
        private final List<String> dropDDL;

        private ProcedureTestData(String originalProcedureName, List<String> createDDL, List<String> dropDDL) {
            this.originalProcedureName = originalProcedureName;
            this.createDDL = createDDL;
            this.dropDDL = dropDDL;
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
         * @return List of DDL script(s) for dropping the procedure
         */
        List<String> getDropDDL() {
            return dropDDL;
        }

        /**
         * @param rules The default validation rules (to be modified by this method)
         * @return Map of validation rules specific to this procedure
         */
        abstract Map<ProcedureMetaData, Object> getSpecificValidationRules(Map<ProcedureMetaData, Object> rules);
    }
}
