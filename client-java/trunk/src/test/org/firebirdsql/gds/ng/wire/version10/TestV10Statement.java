/*
 * $Id$
 *
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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSImpl;
import org.firebirdsql.gds.impl.jni.NativeGDSImpl;
import org.firebirdsql.gds.impl.wire.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10Statement extends FBJUnit4TestBase {

    private static final String CREATE_EXECUTABLE_STORED_PROCEDURE =
            "CREATE PROCEDURE increment " +
                    " (intvalue INTEGER) " +
                    "RETURNS " +
                    " (outvalue INTEGER) " +
                    "AS " +
                    "BEGIN " +
                    " outvalue = intvalue + 1; " +
                    "END";

    private static final String EXECUTE_EXECUTABLE_STORED_PROCEDURE =
            "EXECUTE PROCEDURE INCREMENT(?)";

    private static final String CREATE_SELECTABLE_STORED_PROCEDURE =
            "CREATE PROCEDURE range " +
                    " (startvalue INTEGER, rowcount INTEGER) " +
                    "RETURNS " +
                    " (outvalue INTEGER) " +
                    "AS " +
                    "DECLARE VARIABLE actualcount INTEGER; " +
                    "BEGIN " +
                    "  actualcount = 0; " +
                    "  WHILE (actualcount < rowcount) DO " +
                    "  BEGIN " +
                    "    outvalue = startvalue + actualcount; " +
                    "    suspend; " +
                    "    actualcount = actualcount + 1; " +
                    "  END " +
                    "END";

    private static final String EXECUTE_SELECTABLE_STORED_PROCEDURE =
            "SELECT OUTVALUE FROM RANGE(?, ?)";

    private static final String CREATE_KEY_VALUE_TABLE =
            "CREATE TABLE keyvalue ( " +
                    " thekey INTEGER, " +
                    " thevalue VARCHAR(5)" +
                    ")";

    private static final String INSERT_RETURNING_KEY_VALUE =
            "INSERT INTO keyvalue (thevalue) VALUES (?) RETURNING thekey";

    private final FbConnectionProperties connectionInfo;
    private final SimpleStatementListener listener = new SimpleStatementListener();
    private FbWireDatabase db;
    private FbTransaction transaction;
    private FbStatement statement;

    {
        connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        connectionInfo.setEncoding("NONE");
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void verifyTestType() {
        // Test irrelevant for embedded
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(EmbeddedGDSImpl.EMBEDDED_TYPE_NAME));
        // Test irrelevant for native
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(NativeGDSImpl.NATIVE_TYPE_NAME));
    }

    @Before
    public void setUp() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            DdlHelper.executeDDL(con, CREATE_EXECUTABLE_STORED_PROCEDURE);
            DdlHelper.executeDDL(con, CREATE_SELECTABLE_STORED_PROCEDURE);
            DdlHelper.executeCreateTable(con, CREATE_KEY_VALUE_TABLE);
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }

        WireConnection gdsConnection = new WireConnection(connectionInfo, EncodingFactory.getDefaultInstance(), ProtocolCollection.create(new Version10Descriptor()));
        gdsConnection.socketConnect();
        db = gdsConnection.identify();
        assertEquals("Unexpected FbWireDatabase implementation", V10Database.class, db.getClass());

        db.attach();
    }

    @Test
    public void testSelect_NoParameters_Describe() throws Exception {
        allocateStatement();
        statement.prepare(
                "SELECT RDB$DESCRIPTION AS \"Description\", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$DATABASE");

        assertEquals("Unexpected StatementType", StatementType.SELECT, statement.getType());

        final RowDescriptor fields = statement.getFieldDescriptor();
        assertNotNull("Fields", fields);
        // Note that in the V10 protocol we don't have support for the table alias, so it is always null
        final FirebirdSupportInfo supportInfo = supportInfoFor(db);
        List<FieldDescriptor> expectedFields =
                Arrays.asList(
                        new FieldDescriptor(ISCConstants.SQL_BLOB | 1, 1, supportInfo.reportsBlobCharSetInDescriptor() ? 3 : 0, 8, "Description", null, "RDB$DESCRIPTION", "RDB$DATABASE", "SYSDBA"),
                        new FieldDescriptor(ISCConstants.SQL_SHORT | 1, 0, 0, 2, "RDB$RELATION_ID", null, "RDB$RELATION_ID", "RDB$DATABASE", "SYSDBA"),
                        new FieldDescriptor(ISCConstants.SQL_TEXT | 1, 3, 0, supportInfo.reportsByteLengthInDescriptor() ? 93 : 31, "RDB$SECURITY_CLASS", null, "RDB$SECURITY_CLASS", "RDB$DATABASE", "SYSDBA"),
                        new FieldDescriptor(ISCConstants.SQL_TEXT | 1, 3, 0, supportInfo.reportsByteLengthInDescriptor() ? 93 : 31, "RDB$CHARACTER_SET_NAME", null, "RDB$CHARACTER_SET_NAME", "RDB$DATABASE", "SYSDBA")
                );
        assertEquals("Unexpected values for fields", expectedFields, fields.getFieldDescriptors());
        assertNotNull("Parameters", statement.getParameterDescriptor());
        assertEquals("Unexpected parameter count", 0, statement.getParameterDescriptor().getCount());
    }

    @Test
    public void testSelect_NoParameters_Execute_and_Fetch() throws Exception {
        allocateStatement();
        statement.prepare(
                "SELECT RDB$DESCRIPTION AS \"Description\", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$DATABASE");

        final SimpleStatementListener statementListener = new SimpleStatementListener();
        statement.addStatementListener(statementListener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals("Expected hasResultSet to be set to true", Boolean.TRUE, statementListener.hasResultSet());
        assertEquals("Expected hasSingletonResult to be set to false", Boolean.FALSE, statementListener.hasSingletonResult());
        assertNull("Expected allRowsFetched not set yet", statementListener.isAllRowsFetched());
        assertEquals("Expected no rows to be fetched yet", 0, statementListener.getRows().size());

        statement.fetchRows(10);

        assertEquals("Expected allRowsFetched to be set to true", Boolean.TRUE, statementListener.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener.getRows().size());
    }

    @Test
    public void testSelect_WithParameters_Describe() throws Exception {
        allocateStatement();
        statement.prepare(
                "SELECT a.RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$CHARACTER_SETS a " +
                        "WHERE a.RDB$CHARACTER_SET_ID = ? OR a.RDB$BYTES_PER_CHARACTER = ?");

        assertEquals("Unexpected StatementType", StatementType.SELECT, statement.getType());

        final RowDescriptor fields = statement.getFieldDescriptor();
        assertNotNull("Fields", fields);
        // Note that in the V10 protocol we don't have support for the table alias, so it is always null
        final boolean unicodeFssLengthReported = supportInfoFor(db).reportsByteLengthInDescriptor();
        List<FieldDescriptor> expectedFields =
                Arrays.asList(
                        new FieldDescriptor(ISCConstants.SQL_TEXT | 1, 3, 0, unicodeFssLengthReported ? 93 : 31, "RDB$CHARACTER_SET_NAME", null, "RDB$CHARACTER_SET_NAME", "RDB$CHARACTER_SETS", "SYSDBA")
                );
        assertEquals("Unexpected values for fields", expectedFields, fields.getFieldDescriptors());

        final RowDescriptor parameters = statement.getParameterDescriptor();
        assertNotNull("Parameters", parameters);
        List<FieldDescriptor> expectedParameters =
                Arrays.asList(
                        new FieldDescriptor(ISCConstants.SQL_SHORT | 1, 0, 0, 2, null, null, null, null, null),
                        new FieldDescriptor(ISCConstants.SQL_SHORT | 1, 0, 0, 2, null, null, null, null, null)
                );
        assertEquals("Unexpected values for parameters", expectedParameters, parameters.getFieldDescriptors());
    }

    @Test
    public void testSelect_WithParameters_Execute_and_Fetch() throws Exception {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare(
                "SELECT a.RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$CHARACTER_SETS a " +
                        "WHERE a.RDB$CHARACTER_SET_ID = ? OR a.RDB$BYTES_PER_CHARACTER = ?");

        RowDescriptor descriptor = statement.getParameterDescriptor();
        FieldValue param1 = new FieldValue(descriptor.getFieldDescriptor(0), new byte[]{ 0, 0, 0, 3 }); // int = 3 (id of UNICODE_FSS)
        FieldValue param2 = new FieldValue(descriptor.getFieldDescriptor(1), new byte[]{ 0, 0, 0, 1 }); // int = 1 (single byte character sets)

        statement.execute(RowValue.of(param1, param2));

        assertEquals("Expected hasResultSet to be set to true", Boolean.TRUE, listener.hasResultSet());
        assertEquals("Expected hasSingletonResult to be set to false", Boolean.FALSE, listener.hasSingletonResult());
        assertNull("Expected allRowsFetched not set yet", listener.isAllRowsFetched());
        assertEquals("Expected no rows to be fetched yet", 0, listener.getRows().size());
        assertNull("Expected no SQL counts yet", listener.getSqlCounts());

        // 100 should be sufficient to fetch all character sets
        statement.fetchRows(100);

        assertEquals("Expected allRowsFetched to be set to true", Boolean.TRUE, listener.isAllRowsFetched());
        // Number is database dependent (unicode_fss + all single byte character sets)
        assertTrue("Expected more than two rows", listener.getRows().size() > 2);

        assertNotNull("Expected SQL counts", listener.getSqlCounts());
        assertEquals("Unexpected select count", listener.getRows().size(), listener.getSqlCounts().getLongSelectCount());
    }

    // TODO Test with executable stored procedure

    @Test
    public void testAllocate_NotNew() throws Exception {
        statement = db.createStatement(null);

        statement.allocateStatement();

        expectedException.expect(SQLNonTransientException.class);
        expectedException.expectMessage("allocateOldStatement only allowed when current state is NEW");
        statement.allocateStatement();
    }

    @Test
    public void test_PrepareExecutableStoredProcedure() throws Exception {
        allocateStatement();
        statement.prepare(EXECUTE_EXECUTABLE_STORED_PROCEDURE);

        assertEquals("Unexpected StatementType", StatementType.STORED_PROCEDURE, statement.getType());

        final RowDescriptor fields = statement.getFieldDescriptor();
        assertNotNull("Fields", fields);
        List<FieldDescriptor> expectedFields =
                Arrays.asList(
                        new FieldDescriptor(ISCConstants.SQL_LONG | 1, 0, 0, 4, "OUTVALUE", null, "OUTVALUE", "INCREMENT", "SYSDBA")
                );
        assertEquals("Unexpected values for fields", expectedFields, fields.getFieldDescriptors());

        final RowDescriptor parameters = statement.getParameterDescriptor();
        assertNotNull("Parameters", parameters);
        List<FieldDescriptor> expectedParameters =
                Arrays.asList(
                        new FieldDescriptor(ISCConstants.SQL_LONG | 1, 0, 0, 4, null, null, null, null, null)
                );
        assertEquals("Unexpected values for parameters", expectedParameters, parameters.getFieldDescriptors());
    }

    @Test
    public void test_ExecuteExecutableStoredProcedure() throws Exception {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare(EXECUTE_EXECUTABLE_STORED_PROCEDURE);

        FieldValue parameter1 = statement.getParameterDescriptor().getFieldDescriptor(0).createDefaultFieldValue();
        parameter1.setFieldData(new byte[]{ 0, 0, 0, 1 }); // Byte representation of 1

        statement.execute(RowValue.of(parameter1));

        assertTrue("Expected singleton result for executable stored procedure", listener.hasSingletonResult());
        assertFalse("Expected no result set for executable stored procedure", listener.hasResultSet());
        assertTrue("Expected all rows to have been fetched", listener.isAllRowsFetched());
        assertEquals("Expected 1 row", 1, listener.getRows().size());
        RowValue fieldValues = listener.getRows().get(0);
        assertEquals("Expected one field", 1, fieldValues.getCount());
        FieldValue value = fieldValues.getFieldValue(0);
        assertArrayEquals("Expected byte representation of 2", new byte[]{ 0, 0, 0, 2 }, value.getFieldData());
    }

    @Test
    public void test_PrepareSelectableStoredProcedure() throws Exception {
        allocateStatement();
        statement.prepare(EXECUTE_SELECTABLE_STORED_PROCEDURE);

        assertEquals("Unexpected StatementType", StatementType.SELECT, statement.getType());

        final RowDescriptor fields = statement.getFieldDescriptor();
        assertNotNull("Fields", fields);
        List<FieldDescriptor> expectedFields =
                Arrays.asList(
                        new FieldDescriptor(ISCConstants.SQL_LONG | 1, 0, 0, 4, "OUTVALUE", null, "OUTVALUE", "RANGE", "SYSDBA")
                );
        assertEquals("Unexpected values for fields", expectedFields, fields.getFieldDescriptors());

        final RowDescriptor parameters = statement.getParameterDescriptor();
        assertNotNull("Parameters", parameters);
        List<FieldDescriptor> expectedParameters =
                Arrays.asList(
                        new FieldDescriptor(ISCConstants.SQL_LONG | 1, 0, 0, 4, null, null, null, null, null),
                        new FieldDescriptor(ISCConstants.SQL_LONG | 1, 0, 0, 4, null, null, null, null, null)
                );
        assertEquals("Unexpected values for parameters", expectedParameters, parameters.getFieldDescriptors());
    }

    @Test
    public void test_PrepareInsertReturning() throws Exception {
        assumeTrue("Test requires INSERT .. RETURNING ... support", supportInfoFor(db).supportsInsertReturning());

        allocateStatement();
        statement.prepare(INSERT_RETURNING_KEY_VALUE);

        // DML {INSERT, UPDATE, DELETE} ... RETURNING is described as a stored procedure!
        assertEquals("Unexpected StatementType", StatementType.STORED_PROCEDURE, statement.getType());

        final RowDescriptor fields = statement.getFieldDescriptor();
        assertNotNull("Fields", fields);
        List<FieldDescriptor> expectedFields =
                Arrays.asList(
                        new FieldDescriptor(ISCConstants.SQL_LONG | 1, 0, 0, 4, "THEKEY", null, "THEKEY", "KEYVALUE", "SYSDBA")
                );
        assertEquals("Unexpected values for fields", expectedFields, fields.getFieldDescriptors());

        final RowDescriptor parameters = statement.getParameterDescriptor();
        assertNotNull("Parameters", parameters);
        List<FieldDescriptor> expectedParameters =
                Arrays.asList(
                        new FieldDescriptor(ISCConstants.SQL_VARYING | 1, 0, 0, 5, null, null, null, null, null)
                );
        assertEquals("Unexpected values for parameters", expectedParameters, parameters.getFieldDescriptors());
    }

    @Test
    public void test_GetExecutionPlan_withStatementPrepared() throws Exception {
        allocateStatement();
        statement.prepare(
                "SELECT RDB$DESCRIPTION AS \"Description\", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$DATABASE");

        String executionPlan = statement.getExecutionPlan();

        assertEquals("Unexpected plan for prepared statement", "PLAN (RDB$DATABASE NATURAL)", executionPlan);
    }

    @Test
    public void test_GetExecutionPlan_noStatementPrepared() throws Exception {
        allocateStatement();

        String executionPlan = statement.getExecutionPlan();
        // TODO: Behavior is different for Firebird 3: throws an exception "Attempt to execute an unprepared dynamic SQL statement."
        assertEquals("Unexpected plan for allocated statement (not prepared)", "", executionPlan);
    }

    @Test
    public void test_GetExecutionPlan_notAllocated() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expectMessage("Statement not yet allocated");
        statement = db.createStatement(null);

        statement.getExecutionPlan();
    }

    @Test
    public void test_GetExecutionPlan_StatementClosed() throws Exception {
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expectMessage("Statement closed");
        allocateStatement();
        statement.prepare(
                "SELECT RDB$DESCRIPTION AS \"Description\", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$DATABASE");
        statement.close();

        statement.getExecutionPlan();
    }

    @Test
    public void test_ExecuteInsert() throws Exception {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare("INSERT INTO keyvalue (thekey, thevalue) VALUES (?, ?)");

        FieldValue parameter1 = statement.getParameterDescriptor().getFieldDescriptor(0).createDefaultFieldValue();
        FieldValue parameter2 = statement.getParameterDescriptor().getFieldDescriptor(1).createDefaultFieldValue();
        parameter1.setFieldData(new byte[]{ 1, 0, 0, 0 });
        parameter2.setFieldData(db.getEncoding().encodeToCharset("test"));

        statement.execute(RowValue.of(parameter1, parameter2));

        assertNotNull("Expected SQL counts on listener", listener.getSqlCounts());
        assertEquals("Expected one row to have been inserted", 1, listener.getSqlCounts().getLongInsertCount());
    }

    /**
     * Test calling {@link org.firebirdsql.gds.ng.FbStatement#closeCursor()} on statement with state NEW,
     * expectation: no error, state unchanged
     */
    @Test
    public void test_CloseCursor_State_NEW() throws Exception {
        statement = db.createStatement(null);
        assumeThat(statement.getState(), equalTo(StatementState.NEW));

        statement.closeCursor();
        assertEquals(StatementState.NEW, statement.getState());
    }

    /**
     * Test calling {@link org.firebirdsql.gds.ng.FbStatement#closeCursor()} on statement with state ALLOCATED,
     * expectation: no error, state unchanged
     */
    @Test
    public void test_CloseCursor_State_ALLOCATED() throws Exception {
        allocateStatement();
        assumeThat(statement.getState(), equalTo(StatementState.ALLOCATED));

        statement.closeCursor();
        assertEquals(StatementState.ALLOCATED, statement.getState());
    }

    /**
     * Test calling {@link org.firebirdsql.gds.ng.FbStatement#closeCursor()} on statement with state PREPARED,
     * expectation: no error, state unchanged
     */
    @Test
    public void test_CloseCursor_State_PREPARED() throws Exception {
        allocateStatement();
        statement.prepare("SELECT * FROM RDB$DATABASE");
        assumeThat(statement.getState(), equalTo(StatementState.PREPARED));

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
        statement.prepare("SELECT * FROM RDB$DATABASE");
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        assumeThat(statement.getState(), equalTo(StatementState.CURSOR_OPEN));

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
        assumeThat(statement.getState(), equalTo(StatementState.CLOSED));

        statement.closeCursor();
        assertEquals(StatementState.CLOSED, statement.getState());
    }

    private FbTransaction getTransaction() throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);
        return db.startTransaction(tpb);
    }

    private void allocateStatement() throws SQLException {
        if (transaction == null || transaction.getState() != TransactionState.ACTIVE) {
            transaction = getTransaction();
        }
        statement = db.createStatement(transaction);
        statement.allocateStatement();
    }

    @After
    public void tearDown() throws Exception {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ex) {
                log.debug("Exception on statement close", ex);
            }
        }
        if (transaction != null) {
            try {
                transaction.commit();
            } catch (SQLException ex) {
                log.debug("Exception on transaction commit", ex);
            }
        }
        if (db != null) {
            try {
                db.detach();
            } catch (SQLException ex) {
                log.debug("Exception on detach", ex);
            }
        }
    }
}
