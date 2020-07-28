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
package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.JaybirdErrorCodes;

import org.firebirdsql.gds.ng.AbstractStatementTest;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.junit.Assert.*;

/**
 * Tests for OO API statement.
 * {@link org.firebirdsql.nativeoo.gds.ng.IStatementImpl}.
 *
 * @since 4.0
 */
public class IStatementImplTest extends AbstractStatementTest {

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supportsFBOONativeOnly();

    private AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    @Override
    protected Class<? extends FbDatabase> getExpectedDatabaseType() {
        return IDatabaseImpl.class;
    }

    @Override
    protected FbDatabase createDatabase() throws SQLException {
        return factory.connect(connectionInfo);
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

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertEquals("Expected allRowsFetched to haven't been called yet", null, statementListener.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener.getRows().size());

        statement.fetchRows(1);

        assertEquals("Expected allRowsFetched to be set to true", Boolean.TRUE, statementListener.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener.getRows().size());
    }

    @Test
    public void testMultipleExecute() throws Exception {
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

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertEquals("Expected allRowsFetched to haven't been called yet", null, statementListener.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener.getRows().size());

        statement.fetchRows(1);

        assertEquals("Expected allRowsFetched to be set to true", Boolean.TRUE, statementListener.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener.getRows().size());

        statement.closeCursor();
        final SimpleStatementListener statementListener2 = new SimpleStatementListener();
        statement.addStatementListener(statementListener2);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals("Expected hasResultSet to be set to true", Boolean.TRUE, statementListener2.hasResultSet());
        assertEquals("Expected hasSingletonResult to be set to false", Boolean.FALSE, statementListener2.hasSingletonResult());
        assertNull("Expected allRowsFetched not set yet", statementListener2.isAllRowsFetched());
        assertEquals("Expected no rows to be fetched yet", 0, statementListener2.getRows().size());

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertEquals("Expected allRowsFetched to haven't been called yet", null, statementListener2.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener2.getRows().size());

        statement.fetchRows(1);

        assertEquals("Expected allRowsFetched to be set to true", Boolean.TRUE, statementListener2.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener2.getRows().size());
    }

    @Test
    public void testMultiplePrepare() throws Exception {
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

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertEquals("Expected allRowsFetched to haven't been called yet", null, statementListener.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener.getRows().size());

        statement.fetchRows(1);

        assertEquals("Expected allRowsFetched to be set to true", Boolean.TRUE, statementListener.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener.getRows().size());

        statement.closeCursor();

        statement.prepare(
                "SELECT RDB$DESCRIPTION AS \"Description\", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$DATABASE");

        final SimpleStatementListener statementListener2 = new SimpleStatementListener();
        statement.addStatementListener(statementListener2);
        statement.execute(RowValue.EMPTY_ROW_VALUE);

        assertEquals("Expected hasResultSet to be set to true", Boolean.TRUE, statementListener2.hasResultSet());
        assertEquals("Expected hasSingletonResult to be set to false", Boolean.FALSE, statementListener2.hasSingletonResult());
        assertNull("Expected allRowsFetched not set yet", statementListener2.isAllRowsFetched());
        assertEquals("Expected no rows to be fetched yet", 0, statementListener2.getRows().size());

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(10);

        assertEquals("Expected allRowsFetched to haven't been called yet", null, statementListener2.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener2.getRows().size());

        statement.fetchRows(1);

        assertEquals("Expected allRowsFetched to be set to true", Boolean.TRUE, statementListener2.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, statementListener2.getRows().size());
    }

    @Test
    public void testSelect_WithParameters_Execute_and_Fetch() throws Exception {
        allocateStatement();
        statement.addStatementListener(listener);
        statement.prepare(
                "SELECT a.RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$CHARACTER_SETS a " +
                        "WHERE a.RDB$CHARACTER_SET_ID = ? OR a.RDB$BYTES_PER_CHARACTER = ?");

        final DatatypeCoder coder = db.getDatatypeCoder();
        RowValue rowValue = RowValue.of(
                coder.encodeShort(3), // smallint = 3 (id of UNICODE_FSS)
                coder.encodeShort(1)); // smallint = 1 (single byte character sets)

        statement.execute(rowValue);

        assertEquals("Expected hasResultSet to be set to true", Boolean.TRUE, listener.hasResultSet());
        assertEquals("Expected hasSingletonResult to be set to false", Boolean.FALSE, listener.hasSingletonResult());
        assertNull("Expected allRowsFetched not set yet", listener.isAllRowsFetched());
        assertEquals("Expected no rows to be fetched yet", 0, listener.getRows().size());
        assertNull("Expected no SQL counts yet", listener.getSqlCounts());

        // JNAStatement only executes a single fetch to prevent problems with positioned updates,
        // so this doesn't get all rows fetched immediately
        statement.fetchRows(100);

        assertEquals("Expected allRowsFetched to haven't been called yet", null, listener.isAllRowsFetched());
        assertEquals("Expected a single row to have been fetched", 1, listener.getRows().size());

        // 100 should be sufficient to fetch all character sets; limit to prevent infinite loop with bugs in fetchRows
        int count = 0;
        while(listener.isAllRowsFetched() != Boolean.TRUE && count < 100) {
            statement.fetchRows(1);
            count++;
        }

        assertEquals("Expected allRowsFetched to be set to true", Boolean.TRUE, listener.isAllRowsFetched());
        // Number is database dependent (unicode_fss + all single byte character sets)
        assertTrue("Expected more than two rows", listener.getRows().size() > 2);

        assertNull("expected no SQL counts immediately after retrieving all rows", listener.getSqlCounts());

        statement.getSqlCounts();

        assertNotNull("Expected SQL counts", listener.getSqlCounts());
        assertEquals("Unexpected select count", listener.getRows().size(), listener.getSqlCounts().getLongSelectCount());
    }

    @Test
    public void test_GetExecutionPlan_noStatementPrepared() throws Exception {
        allocateStatement();
        expectedException.expect(SQLNonTransientException.class);
        expectedException.expectMessage("Statement not yet allocated");

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
    public void setTimeout_nonZeroThenZero() throws Exception {
        allocateStatement();

        statement.prepare(
                "SELECT RDB$DESCRIPTION AS \"Description\", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$DATABASE");

        statement.setTimeout(1);
        assertEquals(1, statement.getTimeout());

        statement.setTimeout(0);
        assertEquals(0, statement.getTimeout());
    }

    @Test
    public void setTimeout_max_long_allowed() throws Exception {
        allocateStatement();

        statement.prepare(
                "SELECT RDB$DESCRIPTION AS \"Description\", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$DATABASE");

        statement.setTimeout(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, statement.getTimeout());
    }

    @Test
    public void setTimeout_negativeValue_throwsException() throws Exception {
        allocateStatement();

        statement.prepare(
                "SELECT RDB$DESCRIPTION AS \"Description\", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$DATABASE");

        expectedException.expect(SQLNonTransientException.class);
        expectedException.expect(errorCodeEquals(JaybirdErrorCodes.jb_invalidTimeout));

        statement.setTimeout(-1);
    }

    @Test
    public void getTimeout_defaultZero() throws Exception {
        allocateStatement();

        statement.prepare(
                "SELECT RDB$DESCRIPTION AS \"Description\", RDB$RELATION_ID, RDB$SECURITY_CLASS, RDB$CHARACTER_SET_NAME " +
                        "FROM RDB$DATABASE");

        assertEquals(0, statement.getTimeout());
    }
}
