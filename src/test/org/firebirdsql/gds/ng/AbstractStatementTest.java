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
package org.firebirdsql.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.jna.JnaStatement;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.*;
import java.util.List;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FbAssumptions.assumeFeature;
import static org.firebirdsql.common.FbAssumptions.assumeFeatureMissing;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Generic tests for FbStatement.
 * <p>
 * This abstract class is subclassed by the tests for specific FbStatement implementations.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractStatementTest {

    private static final String CREATE_EXECUTABLE_STORED_PROCEDURE = """
            CREATE PROCEDURE increment (intvalue INTEGER)
              RETURNS (outvalue INTEGER)
            AS
            BEGIN
              outvalue = intvalue + 1;
            END""";

    protected static final String EXECUTE_EXECUTABLE_STORED_PROCEDURE = "EXECUTE PROCEDURE INCREMENT(?)";

    private static final String CREATE_SELECTABLE_STORED_PROCEDURE = """
            CREATE PROCEDURE range (startvalue INTEGER, rowcount INTEGER)
              RETURNS (outvalue INTEGER)
            AS
            DECLARE VARIABLE actualcount INTEGER;
            BEGIN
              actualcount = 0;
              WHILE (actualcount < rowcount) DO
              BEGIN
                outvalue = startvalue + actualcount;
                suspend;
                actualcount = actualcount + 1;
              END
            END""";

    protected static final String EXECUTE_SELECTABLE_STORED_PROCEDURE = "SELECT OUTVALUE FROM RANGE(?, ?)";

    private static final String CREATE_KEY_VALUE_TABLE = """
            CREATE TABLE keyvalue (
              thekey INTEGER PRIMARY KEY,
              thevalue VARCHAR(5),
              theUTFVarcharValue VARCHAR(5) CHARACTER SET UTF8,
              theUTFCharValue CHAR(5) CHARACTER SET UTF8
            )""";

    protected static final String INSERT_RETURNING_KEY_VALUE =
            "INSERT INTO keyvalue (thevalue) VALUES (?) RETURNING thekey";

    protected static final String INSERT_THEUTFVALUE =
            "INSERT INTO keyvalue (thekey, theUTFVarcharValue, theUTFCharValue) VALUES (?, ?, ?)";

    protected static final String SELECT_THEUTFVALUE =
            "SELECT theUTFVarcharValue, theUTFCharValue FROM keyvalue WHERE thekey = ?";

    protected static final String SELECT_FROM_RDB$DATABASE = """
            SELECT RDB$DESCRIPTION AS "Description", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME
            FROM RDB$DATABASE""";

    protected static final String SELECT_CHARSET_BY_ID_OR_SIZE = """
            SELECT a.RDB$CHARACTER_SET_NAME
            FROM RDB$CHARACTER_SETS a
            WHERE a.RDB$CHARACTER_SET_ID = ? OR a.RDB$BYTES_PER_CHARACTER = ?""";

    protected final SimpleStatementListener listener = new SimpleStatementListener();
    protected FbDatabase db;
    private FbTransaction transaction;
    protected FbStatement statement;
    protected final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    @RegisterExtension
    public static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_EXECUTABLE_STORED_PROCEDURE,
            CREATE_SELECTABLE_STORED_PROCEDURE,
            CREATE_KEY_VALUE_TABLE);

    protected abstract Class<? extends FbDatabase> getExpectedDatabaseType();

    @BeforeEach
    public final void setUp() throws Exception {
        try (var connection = getConnectionViaDriverManager();
             var stmt = connection.createStatement()) {
            stmt.execute("delete from keyvalue");
        }
        db = createDatabase();
        assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbDatabase implementation");

        db.attach();
    }

    protected abstract FbDatabase createDatabase() throws SQLException;

    @Test
    public void testSelect_NoParameters_Describe() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        assertEquals(StatementType.SELECT, statement.getType(), "Unexpected StatementType");

        final RowDescriptor fields = statement.getRowDescriptor();
        assertNotNull(fields, "Fields");
        final FirebirdSupportInfo supportInfo = supportInfoFor(db);
        final int metadataCharSetId = supportInfo.reportedMetadataCharacterSetId();
        var expectedFields = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_BLOB | 1, 1,
                        supportInfo.reportsBlobCharSetInDescriptor() ? metadataCharSetId : 0, 8, "Description", null,
                        "RDB$DESCRIPTION", "RDB$DATABASE", "SYSDBA"),
                new FieldDescriptor(1, db.getDatatypeCoder(), ISCConstants.SQL_SHORT | 1, 0, 0, 2,
                        "RDB$RELATION_ID", null, "RDB$RELATION_ID", "RDB$DATABASE", "SYSDBA"),
                new FieldDescriptor(2, db.getDatatypeCoder(), ISCConstants.SQL_TEXT | 1, metadataCharSetId, 0,
                        supportInfo.maxReportedIdentifierLengthBytes(), "RDB$SECURITY_CLASS", null,
                        "RDB$SECURITY_CLASS", "RDB$DATABASE", "SYSDBA"),
                new FieldDescriptor(3, db.getDatatypeCoder(), ISCConstants.SQL_TEXT | 1, metadataCharSetId, 0,
                        supportInfo.maxReportedIdentifierLengthBytes(), "RDB$CHARACTER_SET_NAME", null,
                        "RDB$CHARACTER_SET_NAME", "RDB$DATABASE", "SYSDBA")
        );
        assertEquals(expectedFields, fields.getFieldDescriptors(), "Unexpected values for fields");
        assertNotNull(statement.getParameterDescriptor(), "Parameters");
        assertEquals(0, statement.getParameterDescriptor().getCount(), "Unexpected parameter count");
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    public void testFetchRows_nonPositiveFetchSize_throwsException(int fetchSize) throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        var exception = assertThrows(SQLException.class, () -> statement.fetchRows(fetchSize));
        assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_invalidFetchSize));
    }

    @Test
    public void testSelect_NoParameters_Execute_and_Fetch() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, listener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, listener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");

        statement.fetchRows(10);

        assertEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, listener.getRows().size(), "Expected a single row to have been fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected a single row to have been fetched");
    }

    @Test
    public void testSelect_WithParameters_Describe() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_CHARSET_BY_ID_OR_SIZE);

        assertEquals(StatementType.SELECT, statement.getType(), "Unexpected StatementType");

        final RowDescriptor fields = statement.getRowDescriptor();
        assertNotNull(fields, "Fields");
        final FirebirdSupportInfo supportInfo = supportInfoFor(db);
        final boolean supportsTableAlias = supportInfo.supportsTableAlias();
        final int metadataCharSetId = supportInfo.reportedMetadataCharacterSetId();

        var expectedFields = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_TEXT | 1, metadataCharSetId, 0,
                        supportInfo.maxReportedIdentifierLengthBytes(), "RDB$CHARACTER_SET_NAME",
                        supportsTableAlias ? "A" : null, "RDB$CHARACTER_SET_NAME", "RDB$CHARACTER_SETS", "SYSDBA")
        );
        assertEquals(expectedFields, fields.getFieldDescriptors(), "Unexpected values for fields");

        final RowDescriptor parameters = statement.getParameterDescriptor();
        assertNotNull(parameters, "Parameters");
        var expectedParameters = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_SHORT | 1, 0, 0, 2,
                        null, null, null, null, null),
                new FieldDescriptor(1, db.getDatatypeCoder(), ISCConstants.SQL_SHORT | 1, 0, 0, 2,
                        null, null, null, null, null)
        );
        assertEquals(expectedParameters, parameters.getFieldDescriptors(), "Unexpected values for parameters");
    }

    @Test
    public void testSelect_WithParameters_Execute_and_Fetch() throws Exception {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare(SELECT_CHARSET_BY_ID_OR_SIZE);

        final DatatypeCoder coder = db.getDatatypeCoder();
        var rowValue = RowValue.of(
                coder.encodeShort(3),  // smallint = 3 (id of UNICODE_FSS)
                coder.encodeShort(1)); // smallint = 1 (single byte character sets)

        statement.execute(rowValue);

        assertEquals(Boolean.TRUE, listener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, listener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");
        assertNull(listener.getSqlCounts(), "Expected no SQL counts yet");

        // 100 should be sufficient to fetch all character sets
        statement.fetchRows(100);

        assertEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to be set to true");
        // Number is database dependent (unicode_fss + all single byte character sets)
        assertTrue(listener.getRows().size() > 2, "Expected more than two rows");
        assertTrue(listener.getLastFetchCount() > 2, "Expected more than two rows");

        assertNull(listener.getSqlCounts(), "expected no SQL counts immediately after retrieving all rows");

        statement.getSqlCounts();

        assertNotNull(listener.getSqlCounts(), "Expected SQL counts");
        assertEquals(listener.getRows().size(), listener.getSqlCounts().selectCount(), "Unexpected select count");
    }

    @Test
    public void test_PrepareExecutableStoredProcedure() throws Exception {
        allocateStatement();
        statement.prepare(EXECUTE_EXECUTABLE_STORED_PROCEDURE);

        assertEquals(StatementType.STORED_PROCEDURE, statement.getType(), "Unexpected StatementType");

        final RowDescriptor fields = statement.getRowDescriptor();
        assertNotNull(fields, "Fields");
        var expectedFields = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_LONG | 1, 0, 0, 4, "OUTVALUE", null,
                        "OUTVALUE", "INCREMENT", "SYSDBA")
        );
        assertEquals(expectedFields, fields.getFieldDescriptors(), "Unexpected values for fields");

        final RowDescriptor parameters = statement.getParameterDescriptor();
        assertNotNull(parameters, "Parameters");
        var expectedParameters = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_LONG | 1, 0, 0, 4, null, null, null,
                        null, null)
        );
        assertEquals(expectedParameters, parameters.getFieldDescriptors(), "Unexpected values for parameters");
    }

    @Test
    public void test_ExecuteExecutableStoredProcedure() throws Exception {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare(EXECUTE_EXECUTABLE_STORED_PROCEDURE);

        var rowValue = RowValue.of(
                db.getDatatypeCoder().encodeInt(1)); // Byte representation of 1
        statement.execute(rowValue);

        assertTrue(listener.hasSingletonResult(), "Expected singleton result for executable stored procedure");
        assertFalse(listener.hasResultSet(), "Expected no result set for executable stored procedure");
        assertTrue(listener.isAfterLast(), "Expected all rows to have been fetched");
        assertEquals(1, listener.getRows().size(), "Expected 1 row");
        assertNull(listener.getLastFetchCount(), "Expected no fetch count for EXECUTE PROCEDURE");
        RowValue fieldValues = listener.getRows().get(0);
        assertEquals(1, fieldValues.getCount(), "Expected one field");
        assertEquals(2, db.getDatatypeCoder().decodeInt(fieldValues.getFieldData(0)),
                "Expected byte representation of 2");
    }

    @Test
    public void test_PrepareSelectableStoredProcedure() throws Exception {
        allocateStatement();
        statement.prepare(EXECUTE_SELECTABLE_STORED_PROCEDURE);

        assertEquals(StatementType.SELECT, statement.getType(), "Unexpected StatementType");

        final RowDescriptor fields = statement.getRowDescriptor();
        assertNotNull(fields, "Fields");
        var expectedFields = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_LONG | 1, 0, 0, 4, "OUTVALUE", null,
                        "OUTVALUE", "RANGE", "SYSDBA")
        );
        assertEquals(expectedFields, fields.getFieldDescriptors(), "Unexpected values for fields");

        final RowDescriptor parameters = statement.getParameterDescriptor();
        assertNotNull(parameters, "Parameters");
        var expectedParameters = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_LONG | 1, 0, 0, 4, null, null, null,
                        null, null),
                new FieldDescriptor(1, db.getDatatypeCoder(), ISCConstants.SQL_LONG | 1, 0, 0, 4, null, null, null,
                        null, null)
        );
        assertEquals(expectedParameters, parameters.getFieldDescriptors(), "Unexpected values for parameters");
    }

    @Test
    public void test_PrepareInsertReturning() throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsInsertReturning, "Test requires INSERT .. RETURNING ... support");

        allocateStatement();
        statement.prepare(INSERT_RETURNING_KEY_VALUE);

        // DML {INSERT, UPDATE, DELETE} ... RETURNING is described as a stored procedure!
        assertEquals(StatementType.STORED_PROCEDURE, statement.getType(), "Unexpected StatementType");

        final RowDescriptor fields = statement.getRowDescriptor();
        assertNotNull(fields, "Fields");
        var expectedFields = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_LONG | 1, 0, 0, 4, "THEKEY", null,
                        "THEKEY", "KEYVALUE", "SYSDBA")
        );
        assertEquals(expectedFields, fields.getFieldDescriptors(), "Unexpected values for fields");

        final RowDescriptor parameters = statement.getParameterDescriptor();
        assertNotNull(parameters, "Parameters");
        var expectedParameters = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_VARYING | 1, 0, 0, 5, null, null,
                        null, null, null)
        );
        assertEquals(expectedParameters, parameters.getFieldDescriptors(), "Unexpected values for parameters");
    }

    @Test
    public void test_GetExecutionPlan_withStatementPrepared() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        String executionPlan = statement.getExecutionPlan();

        assertEquals("PLAN (RDB$DATABASE NATURAL)", executionPlan, "Unexpected plan for prepared statement");
    }

    @Test
    public void test_GetExecutionPlan_noStatementPrepared() throws Exception {
        allocateStatement();

        var exception = assertThrows(SQLNonTransientException.class, statement::getExecutionPlan);
        assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_stmtNotAllocated));
    }

    @Test
    public void test_GetExecutionPlan_StatementClosed() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);
        statement.close();

        var exception = assertThrows(SQLNonTransientException.class, statement::getExecutionPlan);
        assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_stmtClosed));
    }

    @Test
    public void test_GetExplainedExecutionPlan_unsupportedVersion() throws Exception {
        assumeFeatureMissing(FirebirdSupportInfo::supportsExplainedExecutionPlan,
                "Test expects explained execution plan not supported");

        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        var exception = assertThrows(SQLFeatureNotSupportedException.class, statement::getExplainedExecutionPlan);
        assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_explainedExecutionPlanNotSupported));
    }

    @Test
    public void test_GetExplainedExecutionPlan_withStatementPrepared() throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsExplainedExecutionPlan,
                "Test requires explained execution plan support");

        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        String executionPlan = statement.getExplainedExecutionPlan();

        assertEquals("""
                Select Expression
                    -> Table "RDB$DATABASE" Full Scan""", executionPlan, "Unexpected plan for prepared statement");
    }

    @Test
    public void test_GetExplainedExecutionPlan_noStatementPrepared() throws Exception {
        allocateStatement();

        var exception = assertThrows(SQLNonTransientException.class, statement::getExplainedExecutionPlan);
        assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_stmtNotAllocated));
    }

    @Test
    public void test_GetExplainedExecutionPlan_StatementClosed() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);
        statement.close();

        var exception = assertThrows(SQLNonTransientException.class, statement::getExplainedExecutionPlan);
        assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_stmtClosed));
    }

    @Test
    public void test_ExecuteInsert() throws Exception {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare("INSERT INTO keyvalue (thekey, thevalue) VALUES (?, ?)");

        var rowValue = RowValue.of(
                db.getDatatypeCoder().encodeInt(4096),
                db.getEncoding().encodeToCharset("test"));

        statement.execute(rowValue);

        assertNull(listener.getSqlCounts(), "expected no SQL counts immediately after execute");

        statement.getSqlCounts();

        assertNotNull(listener.getSqlCounts(), "Expected SQL counts on listener");
        assertEquals(1, listener.getSqlCounts().insertCount(), "Expected one row to have been inserted");
    }

    /**
     * Test calling {@link org.firebirdsql.gds.ng.FbStatement#closeCursor()} on statement with state NEW,
     * expectation: no error, state unchanged
     */
    @Test
    public void test_CloseCursor_State_NEW() throws Exception {
        statement = db.createStatement(null);
        assumeThat("Statement state", statement.getState(), equalTo(StatementState.NEW));

        statement.closeCursor();
        assertEquals(StatementState.NEW, statement.getState());
    }

    /**
     * Test calling {@link org.firebirdsql.gds.ng.FbStatement#closeCursor()} on statement with state PREPARED,
     * expectation: no error, state unchanged
     */
    @Test
    public void test_CloseCursor_State_PREPARED() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);
        assumeThat("Statement state", statement.getState(), equalTo(StatementState.PREPARED));

        statement.closeCursor();
        assertEquals(StatementState.PREPARED, statement.getState());
    }

    /**
     * Test calling {@link org.firebirdsql.gds.ng.FbStatement#closeCursor()} on statement with state CURSOR_OPEN,
     * expectation: no error, state PREPARED
     */
    @Test
    public void test_CloseCursor_State_CURSOR_OPEN() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        assumeThat("Statement state", statement.getState(), equalTo(StatementState.CURSOR_OPEN));

        statement.closeCursor();
        assertEquals(StatementState.PREPARED, statement.getState());
    }

    /**
     * Test calling {@link org.firebirdsql.gds.ng.FbStatement#closeCursor()} on statement with state CLOSED,
     * expectation: no error, state unchanged
     */
    @Test
    public void test_CloseCursor_State_CLOSED() throws Exception {
        statement = db.createStatement(null);
        statement.close();
        assumeThat("Statement state", statement.getState(), equalTo(StatementState.CLOSED));

        statement.closeCursor();
        assertEquals(StatementState.CLOSED, statement.getState());
    }

    @Test
    public void testMultipleExecute() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, listener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, listener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");

        statement.fetchRows(10);

        assertEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, listener.getRows().size(), "Expected a single row to have been fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected a single row to have been fetched");

        statement.closeCursor();

        listener.clear();
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, listener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, listener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");

        statement.fetchRows(10);

        assertEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, listener.getRows().size(), "Expected a single row to have been fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected a single row to have been fetched");
    }

    @Test
    public void testMultiplePrepare() throws Exception {
        allocateStatement();
        statement.prepare(SELECT_FROM_RDB$DATABASE);

        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, listener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, listener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");

        statement.fetchRows(10);

        assertEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, listener.getRows().size(), "Expected a single row to have been fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected a single row to have been fetched");

        statement.closeCursor();

        statement.prepare(SELECT_FROM_RDB$DATABASE);

        listener.clear();
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals(Boolean.TRUE, listener.hasResultSet(), "Expected hasResultSet to be set to true");
        assertEquals(Boolean.FALSE, listener.hasSingletonResult(), "Expected hasSingletonResult to be set to false");
        assertNotEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast not set yet");
        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");

        statement.fetchRows(10);

        assertEquals(Boolean.TRUE, listener.isAfterLast(), "Expected afterLast to be set to true");
        assertEquals(1, listener.getRows().size(), "Expected a single row to have been fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected a single row to have been fetched");
    }

    @Test
    public void testSetCursorName() throws Exception {
        allocateStatement();

        statement.prepare(SELECT_FROM_RDB$DATABASE);

        assertDoesNotThrow(() -> statement.setCursorName("abc1"));

        // TODO: Add/check existing tests using cursorName.
    }

    @Test
    public void testInsertSelectUTF8Value() throws Exception {
        allocateStatement();

        // Insert UTF8 columns
        statement.prepare(INSERT_THEUTFVALUE);
        final Encoding utf8Encoding = db.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        final String aEuro = "a\u20AC";
        final byte[] insertFieldData = utf8Encoding.encodeToCharset(aEuro);
        final RowDescriptor parametersInsert = statement.getParameterDescriptor();
        final var parameterValuesInsert = RowValue.of(parametersInsert,
                db.getDatatypeCoder().encodeInt(1),
                insertFieldData,
                insertFieldData);
        statement.execute(parameterValuesInsert);

        // Retrieve the just inserted UTF8 values from the database for comparison
        statement.prepare(SELECT_THEUTFVALUE);
        final RowDescriptor parametersSelect = statement.getParameterDescriptor();
        final var parameterValuesSelect = RowValue.of(parametersSelect,
                db.getDatatypeCoder().encodeInt(1));
        statement.addStatementListener(listener);
        statement.execute(parameterValuesSelect);
        statement.fetchRows(1);

        final List<RowValue> rows = listener.getRows();
        assertEquals(1, rows.size(), "Expected a row");
        assertEquals(1, listener.getLastFetchCount(), "Expected a row");
        final RowValue selectResult = rows.get(0);
        final byte[] selectVarcharFieldData = selectResult.getFieldData(0);
        final byte[] selectCharFieldData = selectResult.getFieldData(1);
        assertNotNull(selectVarcharFieldData);
        assertEquals(4, selectVarcharFieldData.length, "Length of selected varchar field data");
        assertNotNull(selectCharFieldData);
        assertEquals(20, selectCharFieldData.length, "Length of selected char field data");

        String decodedVarchar = utf8Encoding.decodeFromCharset(selectVarcharFieldData);
        String decodedChar = utf8Encoding.decodeFromCharset(selectCharFieldData);

        assertEquals(aEuro, decodedVarchar, "Unexpected value for varchar");
        assertEquals(aEuro, decodedChar.trim(), "Unexpected value for trimmed char");
        // Note artificial result from the way UTF8 is handled
        assertEquals(18, decodedChar.length(), "Unexpected length for char");
        assertEquals(" ".repeat(16), decodedChar.substring(2), "Unexpected trailing characters for char");
    }

    @Test
    public void testStatementExecuteAfterExecuteError() throws Exception {
        allocateStatement();
        statement.prepare("INSERT INTO keyvalue (thekey, thevalue) VALUES (?, ?)");

        var rowValue = RowValue.of(
                db.getDatatypeCoder().encodeInt(4096),
                db.getEncoding().encodeToCharset("test"));

        // Insert value
        statement.execute(rowValue);
        // Insert value again
        assertThrows(SQLException.class, () -> statement.execute(rowValue));

        statement.addStatementListener(listener);

        // Insert another value
        var differentRowValue = RowValue.of(
                db.getDatatypeCoder().encodeInt(4097),
                db.getEncoding().encodeToCharset("test"));
        statement.execute(differentRowValue);

        assertNull(listener.getSqlCounts(), "expected no SQL counts immediately after execute");

        statement.getSqlCounts();

        assertNotNull(listener.getSqlCounts(), "Expected SQL counts on listener");
        assertEquals(1, listener.getSqlCounts().insertCount(), "Expected one row to have been inserted");
    }

    @Test
    public void testStatementPrepareAfterExecuteError() throws Exception {
        allocateStatement();
        statement.prepare("INSERT INTO keyvalue (thekey, thevalue) VALUES (?, ?)");

        var rowValue = RowValue.of(
                db.getDatatypeCoder().encodeInt(4096),
                db.getEncoding().encodeToCharset("test"));

        // Insert value
        statement.execute(rowValue);
        // Insert value again
        assertThrows(SQLException.class, () -> statement.execute(rowValue));

        statement.prepare("INSERT INTO keyvalue (thekey, theUTFVarcharValue) VALUES (?, ?)");
        var differentRowValue = RowValue.of(
                db.getDatatypeCoder().encodeInt(4097),
                db.getEncoding().encodeToCharset("test"));
        statement.execute(differentRowValue);
    }

    @Test
    public void testStatementPrepareAfterPrepareError() throws Exception {
        allocateStatement();
        // Prepare statement with typo
        assertThrows(SQLException.class,
                () -> statement.prepare("INSRT INTO keyvalue (thekey, thevalue) VALUES (?, ?)"));

        statement.prepare("INSERT INTO keyvalue (thekey, thevalue) VALUES (?, ?)");
    }

    @Test
    public void testStatementPrepareLongObjectNames() throws Exception {
        assumeThat("Test requires 63 character identifier support",
                getDefaultSupportInfo().maxIdentifierLengthCharacters(), not(lessThan(63)));

        String tableName = "A".repeat(63);
        String column1 = "B".repeat(63);
        String column2 = "C".repeat(63);
        try (Connection con = getConnectionViaDriverManager()) {
            executeCreateTable(con,
                    "create table " + tableName + " (" + column1 + " varchar(10) character set UTF8, " + column2 + " varchar(20) character set UTF8)");
        }

        allocateStatement();
        statement.prepare(
                "SELECT " + column1 + ", " + column2 + " FROM " + tableName + " where " + column1 + " = ?");

        assertEquals(StatementType.SELECT, statement.getType(), "Unexpected StatementType");

        final RowDescriptor fields = statement.getRowDescriptor();
        assertNotNull(fields, "Fields");
        var expectedFields = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_VARYING | 1, 4,
                        0, 40, column1, null, column1, tableName, "SYSDBA"),
                new FieldDescriptor(1, db.getDatatypeCoder(), ISCConstants.SQL_VARYING | 1, 4,
                        0, 80, column2, null, column2, tableName, "SYSDBA")
        );
        assertEquals(expectedFields, fields.getFieldDescriptors(), "Unexpected values for fields");
        RowDescriptor parameters = statement.getParameterDescriptor();
        assertNotNull(parameters, "Parameters");

        var expectedParameters = List.of(
                new FieldDescriptor(0, db.getDatatypeCoder(), ISCConstants.SQL_VARYING | 1, 4, 0, 40, null, null, null,
                        null, null)
        );
        assertEquals(expectedParameters, parameters.getFieldDescriptors(), "Unexpected values for parameters");
    }

    @Test
    public void setTimeout_nonZeroThenZero() throws Exception {
        allocateStatement();

        statement.setTimeout(1);
        assertEquals(1, statement.getTimeout());

        statement.setTimeout(0);
        assertEquals(0, statement.getTimeout());
    }

    /**
     * Even though the maximum supported timeout (in Firebird 4) is 4294967295 (2^32), the setter allows full range.
     */
    @Test
    public void setTimeout_max_long_allowed() throws Exception {
        allocateStatement();

        statement.setTimeout(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, statement.getTimeout());
    }

    @Test
    public void setTimeout_negativeValue_throwsException() throws Exception {
        allocateStatement();

        var exception = assertThrows(SQLNonTransientException.class, () -> statement.setTimeout(-1));
        assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_invalidTimeout));
    }

    @Test
    public void getTimeout_defaultZero() throws Exception {
        allocateStatement();

        assertEquals(0, statement.getTimeout());
    }

    @Test
    public void testVerifyUnprepare() throws Exception {
        assumeFeature(FirebirdSupportInfo::supportsStatementUnprepare, "Test requires support for statement unprepare");

        allocateStatement();
        String statementText = "SELECT * FROM RDB$DATABASE";
        statement.prepare(statementText);

        try (var connection = getConnectionViaDriverManager();
             var pstmt = connection.prepareStatement("""
                             select count(*) from mon$statements
                             where mon$attachment_id <> current_connection
                             and mon$sql_text = cast(? as varchar(50))""")) {
            pstmt.setString(1, statementText);
            try (var rs = pstmt.executeQuery()) {
                assertNextRow(rs);
                assertEquals(1, rs.getInt(1), "Expected prepared statement");
            }

            statement.unprepare();

            if (statement instanceof JnaStatement) {
                // force free packet to be sent (fbclient delays sending free packet until other network operation)
                db.getDatabaseInfo(new byte[] { ISCConstants.isc_info_end }, 10);
            }

            try (var rs = pstmt.executeQuery()) {
                assertNextRow(rs);
                assertEquals(0, rs.getInt(1), "Expected no prepared statement");
            }
        }
    }

    private FbTransaction getTransaction() throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }

    protected FbTransaction getOrCreateTransaction() throws SQLException {
        if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
            transaction = getTransaction();
        }
        return transaction;
    }

    protected void allocateStatement() throws SQLException {
        if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
            transaction = getTransaction();
        }
        statement = db.createStatement(transaction);
    }

    @AfterEach
    public final void tearDown() throws Exception {
        // ensures that transaction is committed if still active, and that statement and db are closed
        try (var ignored1 = db;
             var ignored2 = statement) {
            if (transaction != null && transaction.getState() == TransactionState.ACTIVE) {
                transaction.commit();
            }
        }
    }

}
