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
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.*;
import java.util.Properties;
import java.util.stream.Stream;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.assertions.SQLExceptionAssertions.assertThrowsFbStatementClosed;
import static org.firebirdsql.common.assertions.SQLExceptionAssertions.assertThrowsFbStatementOnlyMethod;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * This test case checks callable statements by executing procedure through
 * {@link java.sql.CallableStatement} and {@link java.sql.PreparedStatement}.
 *
 * @author David Jencks
 * @author Mark Rotteveel
 */
class FBCallableStatementTest {

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    private static final String CREATE_PROCEDURE = """
            CREATE PROCEDURE factorial(
              max_rows INTEGER,
              mode INTEGER
            ) RETURNS (
              row_num INTEGER,
              factorial INTEGER
            ) AS
              DECLARE VARIABLE temp INTEGER;
              DECLARE VARIABLE counter INTEGER;
            BEGIN
              counter = 0;
              temp = 1;
              WHILE (counter <= max_rows) DO BEGIN
                row_num = counter;
                IF (row_num = 0) THEN
                  temp = 1;
                ELSE
                  temp = temp * row_num;
                factorial = temp;
                counter = counter + 1;
                IF (mode = 1) THEN
                  SUSPEND;
              END
              IF (mode = 2) THEN
                SUSPEND;
            END""";

    private static final String SELECT_PROCEDURE = "SELECT * FROM factorial(?, 2)";
    private static final String CALL_SELECT_PROCEDURE = "{call factorial(?, 1, ?, ?)}";
    private static final String EXECUTE_PROCEDURE = "{call factorial(?, ?, ?, ?)}";
    private static final String EXECUTE_PROCEDURE_AS_STMT = "{call factorial(?, 0)}";

    private static final String CREATE_PROCEDURE_EMP_SELECT = """
            CREATE PROCEDURE get_emp_proj(emp_no SMALLINT)
             RETURNS (proj_id VARCHAR(25)) AS
             BEGIN
                FOR SELECT PROJ_ID
                    FROM employee_project
                    WHERE emp_no = :emp_no ORDER BY proj_id
                    INTO :proj_id
                DO
                    SUSPEND;
            END""";

    private static final String SELECT_PROCEDURE_EMP_SELECT = "SELECT * FROM get_emp_proj(?)";
    private static final String EXECUTE_PROCEDURE_EMP_SELECT = "{call get_emp_proj(?)}";

    private static final String CREATE_PROCEDURE_EMP_INSERT = """
            CREATE PROCEDURE set_emp_proj(emp_no SMALLINT, proj_id VARCHAR(10)
             , last_name VARCHAR(10), proj_name VARCHAR(25))
             AS
             BEGIN
                INSERT INTO employee_project (emp_no, proj_id, last_name, proj_name)
                VALUES (:emp_no, :proj_id, :last_name, :proj_name);
            END""";

    private static final String EXECUTE_PROCEDURE_EMP_INSERT = "{call set_emp_proj (?,?,?,?)}";
    private static final String EXECUTE_PROCEDURE_EMP_INSERT_1 = "EXECUTE PROCEDURE set_emp_proj (?,?,?,?)";
    private static final String EXECUTE_PROCEDURE_EMP_INSERT_SPACES = "EXECUTE PROCEDURE \nset_emp_proj\t   ( ?,?\t,?\n  ,?)";

    private static final String CREATE_EMPLOYEE_PROJECT = """
            CREATE TABLE employee_project(
              emp_no INTEGER NOT NULL,
              proj_id VARCHAR(10) NOT NULL,
              last_name VARCHAR(10) NOT NULL,
              proj_name VARCHAR(25) NOT NULL,
              proj_desc BLOB SUB_TYPE 1,
              product VARCHAR(25)
            )""";

    private static final String CREATE_SIMPLE_OUT_PROC = """
            CREATE PROCEDURE test_out (inParam VARCHAR(10)) RETURNS (outParam VARCHAR(10))
            AS BEGIN
                outParam = inParam;
            END""";

    private static final String EXECUTE_SIMPLE_OUT_PROCEDURE = "{call test_out ?, ? }";
    private static final String EXECUTE_SIMPLE_OUT_PROCEDURE_1 = "{?=CALL test_out(?)}";
    private static final String EXECUTE_IN_OUT_PROCEDURE = "{call test_out ?}";
    private static final String EXECUTE_SIMPLE_OUT_PROCEDURE_CONST = "EXECUTE PROCEDURE test_out 'test'";
    private static final String EXECUTE_SIMPLE_OUT_PROCEDURE_CONST_WITH_QUESTION = "EXECUTE PROCEDURE test_out 'test?'";
    private static final String EXECUTE_SIMPLE_OUT_WITH_OUT_PARAM = "EXECUTE PROCEDURE test_out(?, ?)";

    private static final String CREATE_PROCEDURE_WITHOUT_PARAMS = """
            CREATE PROCEDURE test_no_params
            AS BEGIN
                exit;
            END""";

    private static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS = "{call test_no_params}";
    private static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS_1 = "{call test_no_params()}";
    private static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS_2 = "{call test_no_params () }";
    private static final String EXECUTE_PROCEDURE_WITHOUT_PARAMS_3 = "EXECUTE PROCEDURE test_no_params ()";

    private static final String CREATE_PROCEDURE_SELECT_WITHOUT_PARAMS = """
            CREATE PROCEDURE select_no_params
             RETURNS (proj_id VARCHAR(25))
            AS BEGIN
                proj_id = 'abc';
                SUSPEND;
            END""";

    private static final String EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS = "{call select_no_params}";
    private static final String EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS_1 = "{call select_no_params()}";
    private static final String EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS_2 = "{call select_no_params () }";

    private static final String CREATE_PROCEDURE_BLOB_RESULT = """
            CREATE PROCEDURE blob_result
            RETURNS (
                SQL_SELECT BLOB SUB_TYPE 1 )
            AS
            BEGIN
                sql_select = '
                    EXECUTE BLOCK
                    RETURNS(
                        column_info_column_name VARCHAR(30),
                        column_value VARCHAR(100),
                        column_alias VARCHAR(20))
                    AS
                    DECLARE VARIABLE i INTEGER;
                    BEGIN
                      i = 0;
                      WHILE (i < 10)
                      DO BEGIN
                        column_info_column_name = ''FK_TETEL__DYN'';
                        column_value = ascii_char(ascii_val(''a'') + i);
                        column_alias = UPPER(column_value);
                        SUSPEND;

                        i = i + 1;
                      END
                    END';
            END""";

    private static final String EXECUTE_PROCEDURE_BLOB_RESULT = "EXECUTE PROCEDURE blob_result";

    private Connection con;

    @BeforeEach
    void setUp() throws Exception {
        con = getConnectionViaDriverManager();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeQuietly(con);
    }

    @Test
    void testRun() throws Exception {
        executeDDL(con, CREATE_PROCEDURE);

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE)) {
            cstmt.registerOutParameter(3, Types.INTEGER);
            cstmt.registerOutParameter(4, Types.INTEGER);
            ((FirebirdCallableStatement) cstmt).setSelectableProcedure(false);
            cstmt.setInt(1, 5);
            cstmt.setInt(2, 0);
            cstmt.execute();
            int ans = cstmt.getInt(4);
            assertEquals(120, ans, "Wrong answer");
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_PROCEDURE)) {
            stmt.setInt(1, 5);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Should have at least one row");
                int result = rs.getInt(2);
                assertEquals(120, result, "Wrong result");

                assertFalse(rs.next(), "Should have exactly one row");
            }
        }

        try (CallableStatement cs = con.prepareCall(CALL_SELECT_PROCEDURE)) {
            ((FirebirdCallableStatement) cs).setSelectableProcedure(true);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.setInt(1, 5);
            cs.execute();
            try (ResultSet rs = cs.getResultSet()) {
                assertTrue(rs.next(), "Should have at least one row");
                int result = cs.getInt(3);
                assertEquals(1, result, "Wrong result");

                int counter = 1;
                while (rs.next()) {
                    assertEquals(rs.getInt(2), cs.getInt(3));
                    counter++;
                }

                assertEquals(6, counter, "Should have 6 rows");
            }
        }
    }

    @Test
    void testRun_emp_cs() throws Exception {
        con.setAutoCommit(false);
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_INSERT);
        executeDDL(con, CREATE_PROCEDURE_EMP_SELECT);
        con.setAutoCommit(true);

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT)) {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "HWRII");
            cstmt.setString(3, "Stevens");
            cstmt.setString(4, "Translator upgrade");
            cstmt.execute();
            cstmt.setInt(1, 22);
            cstmt.setString(2, "OTHER");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
        }

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT)) {
            cstmt.setInt(1, 44);
            try (ResultSet rs = cstmt.executeQuery()) {
                assertTrue(rs.next(), "Should have at least one row");
                assertEquals("DGPII", rs.getString(1), "First row value must be DGPII");
            }

            cstmt.setInt(1, 22);
            try (ResultSet rs = cstmt.executeQuery()) {
                assertTrue(rs.next(), "Should have one row");
                assertEquals("OTHER", rs.getString(1), "First row value must be OTHER");
                assertFalse(rs.next(), "Should have one row");
            }
        }

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT)) {
            cstmt.setInt(1, 44);
            cstmt.execute();
            assertEquals("DGPII", cstmt.getString(1), "First row value must be DGPII");

            cstmt.setInt(1, 22);
            cstmt.execute();
            assertEquals("OTHER", cstmt.getString(1), "First row value must be OTHER");
        }

        try (PreparedStatement stmt = con.prepareStatement(SELECT_PROCEDURE_EMP_SELECT)) {
            stmt.setInt(1, 44);
            stmt.execute();
            try (ResultSet rs = stmt.getResultSet()) {
                int count = 0;
                for (String expectedValue : new String[] { "DGPII", "HWRII", "VBASE" }) {
                    count++;
                    assertTrue(rs.next(), String.format("Expected row %d", count));
                    assertEquals(expectedValue, rs.getString(1), String.format("Unexpected value for row %d", count));
                }
                assertFalse(rs.next(), "Should have no more rows");
            }

            stmt.setInt(1, 22);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Should have one row");
                assertEquals("OTHER", rs.getString(1), "First row value must be OTHER");
                assertFalse(rs.next(), "Should have one row");
            }
        }

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT_1)) {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "HWRII");
            cstmt.setString(3, "Stevens");
            cstmt.setString(4, "Translator upgrade");
            cstmt.execute();
            cstmt.setInt(1, 22);
            cstmt.setString(2, "OTHER");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
        }

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT_SPACES)) {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.execute();
        }
    }

    @Test
    void testFatalError() throws Exception {
        executeDDL(con, CREATE_PROCEDURE);

        try (PreparedStatement stmt = con.prepareStatement(EXECUTE_PROCEDURE_AS_STMT)) {
            stmt.setInt(1, 5);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Should have at least one row");
                int result = rs.getInt(2);
                assertEquals(120, result, "Wrong result");

                assertFalse(rs.next(), "Should have exactly one row");
            }
        }
    }

    @Test
    void testOutProcedure() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        try (CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            stmt.setInt(1, 1);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
            assertEquals(1, stmt.getInt(2), "Should return correct value");
        }
    }

    @Test
    void testOutProcedure1() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        try (CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_1)) {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, 1);
            stmt.execute();
            assertEquals(1, stmt.getInt(1), "Should return correct value");
        }
    }

    @Test
    void testOutProcedureWithConst() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        try (CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_CONST)) {
            stmt.execute();
            assertEquals("test", stmt.getString(1), "Should return correct value");
        }
    }

    @Test
    void testOutProcedureWithConstWithQuestionMark() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        try (CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE_CONST_WITH_QUESTION)) {
            stmt.execute();
            assertEquals("test?", stmt.getString(1), "Should return correct value");
        }
    }

    @Test
    void testInOutProcedure() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        try (CallableStatement stmt = con.prepareCall(EXECUTE_IN_OUT_PROCEDURE)) {
            stmt.clearParameters();
            stmt.setInt(1, 1);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            assertEquals(1, stmt.getInt(1), "Should return correct value");

            stmt.clearParameters();
            stmt.setInt(1, 2);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            assertEquals(2, stmt.getInt(1), "Should return correct value");
        }
    }

    /**
     * Test case that reproduces problem executing procedures without
     * parameters. Bug found and reported by Stanislav Bernatsky.
     */
    @Test
    void testProcedureWithoutParams() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_WITHOUT_PARAMS);

        try (CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS)) {
            assertDoesNotThrow(() -> stmt.execute());
        }
    }

    /**
     * Test case that reproduces problem executing procedures without
     * parameters but with braces in call. Reported by Ben (vmdd_tech).
     */
    @Test
    void testProcedureWithoutParams1() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_WITHOUT_PARAMS);

        try (CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_1)) {
            assertDoesNotThrow(() -> stmt.execute());
        }
    }

    /**
     * Test case that reproduces problem executing procedures without
     * parameters, with braces in call, but with space between procedure
     * name and braces. Reported by Ben (vmdd_tech).
     */
    @Test
    void testProcedureWithoutParams2() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_WITHOUT_PARAMS);

        try (CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_2)) {
            assertDoesNotThrow(() -> stmt.execute());
        }

        // and now test EXECUTE PROCEDURE syntax
        try (CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_WITHOUT_PARAMS_3)) {
            assertDoesNotThrow(() -> stmt.execute());
        }
    }

    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testBatch(String scrollableCursorPropertyValue) throws Exception {
        con.close();
        con = createConnection(scrollableCursorPropertyValue);
        con.setAutoCommit(false);
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_INSERT);
        con.setAutoCommit(true);

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT)) {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.addBatch();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.addBatch();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "HWRII");
            cstmt.setString(3, "Stevens");
            cstmt.setString(4, "Translator upgrade");
            cstmt.addBatch();
            cstmt.setInt(1, 22);
            cstmt.setString(2, "OTHER");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.addBatch();

            cstmt.executeBatch();

            try (Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM employee_project");
                rs.last();
                assertEquals(4, rs.getRow(), "Should find 4 records");

                cstmt.setInt(1, 22);
                cstmt.setString(2, "VBASE");
                cstmt.setString(3, "Stevens");
                cstmt.setString(4, "Translator upgrade");
                cstmt.addBatch();

                cstmt.setInt(1, 22);
                cstmt.setNull(2, Types.CHAR);
                cstmt.setString(3, "Roman");
                cstmt.setString(4, "Failure upgrade");
                cstmt.addBatch();

                assertThrows(SQLException.class, cstmt::executeBatch);

                rs = stmt.executeQuery("SELECT * FROM employee_project");
                rs.last();
                if (con.unwrap(FirebirdConnection.class).isUseFirebirdAutoCommit()) {
                    assertEquals(5, rs.getRow(), "Should find 5 records");
                } else {
                    assertEquals(4, rs.getRow(), "Should find 4 records");
                }
            }
        }
    }

    @Test
    void testBatchResultSet() throws Exception {
        con.setAutoCommit(false);
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_INSERT);
        con.setAutoCommit(true);

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT)) {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.execute();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "HWRII");
            cstmt.setString(3, "Stevens");
            cstmt.setString(4, "Translator upgrade");
            cstmt.execute();
            cstmt.setInt(1, 22);
            cstmt.setString(2, "OTHER");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.execute();
        }

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT)) {
            cstmt.setInt(1, 44);
            cstmt.addBatch();
            cstmt.setInt(1, 22);
            cstmt.addBatch();
            assertThrows(BatchUpdateException.class, cstmt::executeBatch);
        }
    }

    /**
     * Test Batch.  IN-OUT parameters are prohibited in batch execution.
     */
    @Test
    void testBatchInOut() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);
        try (CallableStatement stmt = con.prepareCall(EXECUTE_IN_OUT_PROCEDURE)) {
            stmt.clearParameters();
            stmt.setInt(1, 1);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.addBatch();

            BatchUpdateException exception = assertThrows(BatchUpdateException.class, stmt::executeBatch);
            assertThat(exception,
                    message(containsString("Statements executed as batch should not produce a result set")));
        }
    }

    /**
     * Test Batch.  OUT parameters are prohibited in batch execution.
     */
    @Test
    void testBatchOut() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);
        try (CallableStatement stmt = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            stmt.setInt(1, 1);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.addBatch();

            BatchUpdateException exception = assertThrows(BatchUpdateException.class, stmt::executeBatch);
            assertThat(exception,
                    message(containsString("Statements executed as batch should not produce a result set")));
        }
    }

    /**
     * Test automatic retrieval of the selectability of a procedure from the
     * RDB$PROCEDURE_TYPE field. This test is only run starting from Firebird 2.1.
     */
    @Test
    void testAutomaticSetSelectableProcedure_Selectable() throws SQLException {
        assumeTrue(getDefaultSupportInfo().hasProcedureTypeColumn(),
                "Firebird version does not support RDB$PROCEDURE_TYPE");
        executeDDL(con, CREATE_PROCEDURE);

        try (FirebirdCallableStatement cs = (FirebirdCallableStatement) con.prepareCall(CALL_SELECT_PROCEDURE)) {
            assertTrue(cs.isSelectableProcedure(), "Expected selectable procedure");
        }
    }

    /**
     * Test automatic retrieval of the selectability of a procedure from the
     * RDB$PROCEDURE_TYPE field. This test is only run starting from Firebird 2.1.
     */
    @Test
    void testAutomaticSetSelectableProcedure_Executable() throws SQLException {
        assumeTrue(getDefaultSupportInfo().hasProcedureTypeColumn(),
                "Firebird version does not support RDB$PROCEDURE_TYPE");
        con.setAutoCommit(false);
        executeDDL(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_INSERT);
        con.setAutoCommit(true);
        try (FirebirdCallableStatement cs = (FirebirdCallableStatement) con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT)) {
            assertFalse(cs.isSelectableProcedure(), "Expected executable procedure (not-selectable)");
        }
    }

    @Test
    void testAutomaticSetSelectableProcedureAfterMetaUpdate() throws SQLException {
        assumeTrue(getDefaultSupportInfo().hasProcedureTypeColumn(),
                "Firebird version does not support RDB$PROCEDURE_TYPE");
        executeDDL(con, CREATE_PROCEDURE);
        //@formatter:off
        final String CREATE_SIMPLE_PROC =
                "CREATE PROCEDURE MULT (A INTEGER, B INTEGER) RETURNS (C INTEGER)"
                + "AS BEGIN "
                + "    C = A * B;"
                + "    SUSPEND;"
                + "END";
        //@formatter:on

        con.setAutoCommit(false);
        CallableStatement callableStatement = con.prepareCall(CALL_SELECT_PROCEDURE);
        callableStatement.close();

        try (Statement stmt = con.createStatement()) {
            stmt.execute(CREATE_SIMPLE_PROC);
        }
        con.commit();

        try (FirebirdCallableStatement cs = (FirebirdCallableStatement) con.prepareCall("{call mult(?, ?)}")) {
            assertTrue(cs.isSelectableProcedure(), "Expected selectable procedure");
        }
    }

    @Test
    void testJdbc181() throws Exception {
        executeDDL(con, CREATE_PROCEDURE);

        try (CallableStatement cs = con.prepareCall("{call factorial(?, ?)}")) {
            cs.setInt(1, 5);
            cs.setInt(2, 1);
            ResultSet rs = cs.executeQuery();
            int counter = 0;
            int factorial = 1;
            while (rs.next()) {
                assertEquals(counter, rs.getInt(1));
                assertEquals(factorial, rs.getInt(2));
                counter++;
                if (counter > 0)
                    factorial *= counter;
            }
        }
    }

    /**
     * Closing a statement twice should not result in an Exception.
     */
    @Test
    void testDoubleClose() throws SQLException {
        CallableStatement stmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT);
        stmt.close();
        assertDoesNotThrow(stmt::close);
    }

    /**
     * Test if an implicit close (by fully reading the resultset) while closeOnCompletion is true, will close
     * the statement.
     * <p>
     * JDBC 4.1 feature
     * </p>
     */
    @Test
    void testCloseOnCompletion_StatementClosed_afterImplicitResultSetClose() throws SQLException {
        executeDDL(con, CREATE_PROCEDURE);

        try (FBCallableStatement stmt = (FBCallableStatement) con.prepareCall("{call factorial(?, ?)}")) {
            stmt.closeOnCompletion();
            stmt.setInt(1, 5);
            stmt.setInt(2, 1);
            stmt.execute();
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet) stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertTrue(rs.isClosed(), "Resultset should be closed (automatically closed after last result read)");
            assertTrue(stmt.isClosed(), "Statement should be closed");
        }
    }

    // Other closeOnCompletion behavior considered to be sufficiently tested in TestFBStatement

    /**
     * The method {@link java.sql.Statement#executeQuery(String)} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedExecuteQuery_String() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(() -> cs.executeQuery("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String)} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(() -> cs.executeUpdate("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String)} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedExecute_String() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(() -> cs.execute("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#addBatch(String)} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedAddBatch_String() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(() -> cs.addBatch("SELECT * FROM test_blob"));
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String, int)} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String_int() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(() -> cs.executeUpdate("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String_intArr() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(() -> cs.executeUpdate("SELECT * FROM test_blob", new int[] { 1 }));
        }
    }

    /**
     * The method {@link java.sql.Statement#executeUpdate(String, String[])} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedExecuteUpdate_String_StringArr() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(
                    () -> cs.executeUpdate("SELECT * FROM test_blob", new String[] { "col" }));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int)} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedExecute_String_int() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(() -> cs.execute("SELECT * FROM test_blob", Statement.NO_GENERATED_KEYS));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, int[])} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedExecute_String_intArr() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(() -> cs.execute("SELECT * FROM test_blob", new int[] { 1 }));
        }
    }

    /**
     * The method {@link java.sql.Statement#execute(String, String[])} should not work on CallableStatement.
     */
    @Test
    void testUnsupportedExecute_String_StringArr() throws Exception {
        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE)) {
            assertThrowsFbStatementOnlyMethod(() -> cs.execute("SELECT * FROM test_blob", new String[] { "col" }));
        }
    }

    /**
     * Basic test of {@link FBCallableStatement#getMetaData()}.
     */
    @Test
    void testGetMetaData() throws Exception {
        con.setAutoCommit(false);
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_SELECT);
        con.setAutoCommit(true);

        try (CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_EMP_SELECT)) {
            ResultSetMetaData metaData = cs.getMetaData();

            assertEquals(1, metaData.getColumnCount(), "columnCount");
            assertEquals("PROJ_ID", metaData.getColumnLabel(1), "columnLabel");
            assertEquals("PROJ_ID", metaData.getColumnName(1), "columnName");
            // Basic checking, rest should be covered by TestFBResultSetMetaData.
        }
    }

    /**
     * Calling {@link java.sql.CallableStatement#getMetaData()} on a closed statement should throw an exception.
     */
    @Test
    void testGetMetaData_statementClosed() throws Exception {
        CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_PROCEDURE);
        cs.close();

        assertThrowsFbStatementClosed(cs::getMetaData);
    }

    @Test
    void testExecuteSelectableProcedureNoParameters_call_noBraces() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_SELECT_WITHOUT_PARAMS);

        try (CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS)) {
            assertTrue(cs.execute(), "Expected ResultSet");
            ResultSet rs = cs.getResultSet();
            assertNotNull(rs, "Expected ResultSet");
            assertTrue(rs.next(), "Expected at least one row");
            assertEquals("abc", rs.getString("proj_id"));
        }
    }

    @Test
    void testExecuteSelectableProcedureNoParameters_call_emptyBraces() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_SELECT_WITHOUT_PARAMS);

        try (CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS_1)) {
            assertTrue(cs.execute(), "Expected ResultSet");
            ResultSet rs = cs.getResultSet();
            assertNotNull(rs, "Expected ResultSet");
            assertTrue(rs.next(), "Expected at least one row");
            assertEquals("abc", rs.getString("proj_id"));
        }
    }

    @Test
    void testExecuteSelectableProcedureNoParameters_call_emptyBraces_withWhitespace() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_SELECT_WITHOUT_PARAMS);

        try (CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_SELECT_WITHOUT_PARAMS_2)) {
            assertTrue(cs.execute(), "Expected ResultSet");
            ResultSet rs = cs.getResultSet();
            assertNotNull(rs, "Expected ResultSet");
            assertTrue(rs.next(), "Expected at least one row");
            assertEquals("abc", rs.getString("proj_id"));
        }
    }

    /**
     * Test if first accessing and processing the result set from an <b>executable</b> procedure,
     * and then accessing the getters works.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-350">JDBC-350</a>,
     * </p>
     * <p>
     * NOTE: This tests behavior we maintain for compatibility with previous versions; a correct implementation
     * shouldn't return a result set at all.
     * </p>
     */
    @Test
    void testExecutableProcedureAccessResultSetFirst_thenGetter_shouldWork() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        try (CallableStatement cs = con.prepareCall(EXECUTE_IN_OUT_PROCEDURE)) {
            cs.setString(1, "paramvalue");
            assertTrue(cs.execute(), "Expected ResultSet");
            ResultSet rs = cs.getResultSet();
            assertNotNull(rs, "Expected ResultSet");
            assertTrue(rs.next(), "Expected at least one row");
            assertEquals("paramvalue", rs.getString("outParam"));
            rs.close();

            assertEquals("paramvalue", cs.getString(1));
        }
    }

    /**
     * Tests if the blob result of an executable procedure can be retrieved and is non-empty.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-381">JDBC-381</a>.
     * </p>
     */
    @Test
    void testExecutableProcedureBlobResult_shouldGetNonEmptyValue() throws Exception {
        executeDDL(con, CREATE_PROCEDURE_BLOB_RESULT);

        try (CallableStatement cs = con.prepareCall(EXECUTE_PROCEDURE_BLOB_RESULT)) {
            cs.execute();
            String value = cs.getString(1);
            assertNotNull(value, "Expected non-null value");
            assertThat("Expected non-empty value", value.trim(), containsString("EXECUTE BLOCK"));
        }
    }

    @Test
    void testExecutableProcedureGetMetaDataOutParameterSpecifiedValueNotSet() throws Exception {
        executeDDL(con, CREATE_SIMPLE_OUT_PROC);

        try (CallableStatement cs = con.prepareCall(EXECUTE_SIMPLE_OUT_WITH_OUT_PARAM)) {
            cs.registerOutParameter(2, Types.VARCHAR);

            // Calling getParameterMetaData and getMetaData should not throw an exception
            assertDoesNotThrow(cs::getParameterMetaData);
            assertDoesNotThrow(cs::getMetaData);
        }
    }

    @Test
    void testIgnoreProcedureType() throws Exception {
        con.setAutoCommit(false);
        executeCreateTable(con, CREATE_EMPLOYEE_PROJECT);
        executeDDL(con, CREATE_PROCEDURE_EMP_INSERT);
        executeDDL(con, CREATE_PROCEDURE_EMP_SELECT);
        con.setAutoCommit(true);

        try (CallableStatement cstmt = con.prepareCall(EXECUTE_PROCEDURE_EMP_INSERT)) {
            cstmt.setInt(1, 44);
            cstmt.setString(2, "DGPII");
            cstmt.setString(3, "Smith");
            cstmt.setString(4, "Automap");
            cstmt.addBatch();
            cstmt.setInt(1, 44);
            cstmt.setString(2, "VBASE");
            cstmt.setString(3, "Jenner");
            cstmt.setString(4, "Video Database");
            cstmt.addBatch();
            cstmt.executeBatch();
        }

        // First verify with default connection that procedure is selectable
        try (CallableStatement cstmt = con.prepareCall("{call get_emp_proj(?)}")) {
            assertTrue(cstmt.unwrap(FirebirdCallableStatement.class).isSelectableProcedure(),
                    "Expected procedure inferred as selectable");
            cstmt.setInt(1, 44);
            try (ResultSet rs = cstmt.executeQuery()) {
                assertTrue(rs.next(), "Should have at least one row");
                assertEquals("DGPII", rs.getString(1), "First row value must be DGPII");
                assertTrue(rs.next(), "Should have at least a second row");
                assertEquals("VBASE", rs.getString(1), "Second row value must be VBASE");
            }
        }

        // Then check with ignoreProcedureType = true
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("ignoreProcedureType", "true");
        try (Connection conn2 = DriverManager.getConnection(getUrl(), props);
             CallableStatement cstmt = conn2.prepareCall("{call get_emp_proj(?)}")) {
            assertFalse(cstmt.unwrap(FirebirdCallableStatement.class).isSelectableProcedure(),
                    "Expected procedure inferred as executable");
            cstmt.setInt(1, 44);
            cstmt.execute();
            assertEquals("DGPII", cstmt.getString(1), "Value must be DGPII");

            // re-check via non-standard result set if it has a single row (executable should end after first result)
            try (ResultSet rs = cstmt.executeQuery()) {
                assertTrue(rs.next(), "Should have at least one row");
                assertEquals("DGPII", rs.getString(1), "First row value must be DGPII");
                assertFalse(rs.next(), "Should not have a second row");
            }
        }
    }

    /**
     * Tests for <a href="https://github.com/FirebirdSQL/jaybird/issues/729">jaybird#729</a>.
     */
    @Test
    void callableStatementExecuteProcedureShouldNotTrim_729() throws Exception {
        executeDDL(con, """
                create procedure char_return returns (val char(5)) as
                begin
                  val = 'A';
                end""");

        try (var cstmt = con.prepareCall("execute procedure char_return")) {
            cstmt.execute();
            ResultSet rs = cstmt.getResultSet();
            assertTrue(rs.next(), "Expected a row");
            assertAll(
                    () -> assertEquals("A    ", rs.getString(1), "Unexpected trim by rs.getString"),
                    () -> assertEquals("A    ", cstmt.getObject(1), "Unexpected trim by cstmt.getObject"),
                    () -> assertEquals("A    ", cstmt.getString(1), "Unexpected trim by cstmt.getString"));
        }
    }

    /**
     * Tests if executing a selectable procedure without rows and accessing it through {@code CallableStatement.getXXX}
     * throws an exception. This example uses {@code EXECUTE PROCEDURE}, but the default behaviour automatically uses
     * {@code select * from procedure_name()}.
     * <p>
     * Test for <a href="https://github.com/FirebirdSQL/jaybird/issues/636">jaybird#636</a>.
     * </p>
     */
    @Test
    void executeProcedureOnSelectableDefault_noDataToReturn_636() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            conn.setAutoCommit(false);
            try (var stmt = con.createStatement()) {
                stmt.execute("""
                        create or alter procedure test
                          returns (msg varchar(1000))
                        as
                        begin
                          msg = 'sentinel';
                          if (msg <> 'sentinel') then suspend;
                        end""");
            }
            conn.commit();
            try (var cstmt = conn.prepareCall("EXECUTE PROCEDURE test (?)")) {
                cstmt.registerOutParameter(1, java.sql.Types.VARCHAR);
                cstmt.execute();
                var exception = assertThrows(SQLException.class, () -> cstmt.getString("MSG"));
                assertThat(exception, message(equalTo("Current statement has no data to return")));
            }
        }
    }

    /**
     * Tests if executing a selectable procedure using {@code EXECUTE PROCEDURE} and
     * {@code ignoreProcedureType=true} will allow access through {@code getXXX}.
     * <p>
     * Test for <a href="https://github.com/FirebirdSQL/jaybird/issues/636">jaybird#636</a>
     * </p>
     */
    @Test
    void executeProcedureOnSelectable_ignoreProcedureType_null_636() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.ignoreProcedureType, "true");
        try (var conn = DriverManager.getConnection(getUrl(), props)) {
            conn.setAutoCommit(false);
            try (var stmt = con.createStatement()) {
                stmt.execute("""
                        create or alter procedure test
                          returns (msg varchar(1000))
                        as
                        begin
                          msg = 'sentinel';
                          if (msg <> 'sentinel') then suspend;
                        end""");
            }
            conn.commit();
            try (var cstmt = conn.prepareCall("EXECUTE PROCEDURE test (?)")) {
                cstmt.registerOutParameter(1, java.sql.Types.VARCHAR);
                cstmt.execute();
                assertEquals("sentinel", cstmt.getString("MSG"));
            }
        }
    }

    /**
     * Rationale: an implementation bug looked up the result set index by name, then requested the value by index on
     * the callable statement. If that index corresponded to the index of a registered OUT parameter, it would remap
     * that to a different result set column and return the value of the wrong column.
     */
    @Test
    void namedRetrievalMapping() throws Exception {
        executeDDL(con, """
                create procedure one_in_two_out(in1 varchar(5)) returns (out1 varchar(8), out2 varchar(8))
                as
                begin
                  out1 = 'out1' || in1;
                  out2 = 'out2' || in1;
                end""");

        try (var cstmt = con.prepareCall("{call one_in_two_out(?, ?, ?)}")) {
            cstmt.setString(1, "test");
            cstmt.registerOutParameter(2, Types.VARCHAR);
            cstmt.registerOutParameter(3, Types.VARCHAR);

            cstmt.execute();

            assertEquals("out1test", cstmt.getString("OUT1"), "Unexpected value for column OUT1");
            assertEquals("out2test", cstmt.getString("OUT2"), "Unexpected value for column OUT2");
        }
    }

    static Stream<String> scrollableCursorPropertyValues() {
        // We are unconditionally emitting SERVER, to check if the value behaves appropriately on versions that do
        // not support server-side scrollable cursors
        return Stream.of(PropertyConstants.SCROLLABLE_CURSOR_EMULATED, PropertyConstants.SCROLLABLE_CURSOR_SERVER);
    }

    private static Connection createConnection(String scrollableCursorPropertyValue) throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.scrollableCursor, scrollableCursorPropertyValue);
        return DriverManager.getConnection(getUrl(), props);
    }
}
