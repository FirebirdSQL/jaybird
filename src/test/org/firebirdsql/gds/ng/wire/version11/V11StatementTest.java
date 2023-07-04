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
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.OperationMonitor;
import org.firebirdsql.gds.ng.TestOperationAware;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.monitor.Operation;
import org.firebirdsql.gds.ng.wire.version10.V10StatementTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version11.V11Statement}, reuses test for V10.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V11StatementTest extends V10StatementTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(11);

    protected V11CommonConnectionInfo commonConnectionInfo() {
        return new V11CommonConnectionInfo();
    }

    @Test
    public void testAsyncFetchRows_happyPath() throws Exception {
        allocateStatement();
        statement.prepare("select RDB$CHARACTER_SET_NAME from RDB$CHARACTER_SETS order by RDB$CHARACTER_SET_NAME");
        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        statement.asyncFetchRows(10);

        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");
        assertNull(listener.getLastFetchCount(), "Expected no rows to be fetched yet");

        // Should complete the previous async fetch of 10 rows instead of fetching 1 row
        statement.fetchRows(1);

        assertEquals(10, listener.getRows().size(), "Expected 10 rows to be fetched");
        assertEquals(10, listener.getLastFetchCount(), "Expected 10 rows to be fetched");
    }

    @Test
    public void testAsyncFetchRows_noAsyncFetchSingleRow() throws Exception {
        allocateStatement();
        statement.prepare("select RDB$CHARACTER_SET_NAME from RDB$CHARACTER_SETS order by RDB$CHARACTER_SET_NAME");
        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        // this will be ignored
        statement.asyncFetchRows(1);

        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");
        assertNull(listener.getLastFetchCount(), "Expected no rows to be fetched yet");

        // Should fetch 10 rows, as the request to fetch 1 row asynchronously was ignored
        statement.fetchRows(10);

        assertEquals(10, listener.getRows().size(), "Expected 10 rows to be fetched");
        assertEquals(10, listener.getLastFetchCount(), "Expected 10 rows to be fetched");
    }

    @Test
    public void testAsyncFetchRows_secondAsyncFetchIsIgnored() throws Exception {
        allocateStatement();
        statement.prepare("select RDB$CHARACTER_SET_NAME from RDB$CHARACTER_SETS order by RDB$CHARACTER_SET_NAME");
        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        statement.asyncFetchRows(2);
        // this will be ignored because previous async fetch is pending
        statement.asyncFetchRows(3);

        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");
        assertNull(listener.getLastFetchCount(), "Expected no rows to be fetched yet");

        // Should complete the first async fetch of 2 rows instead of fetching 1 row
        statement.fetchRows(1);

        assertEquals(2, listener.getRows().size(), "Expected 2 rows to be fetched");
        assertEquals(2, listener.getLastFetchCount(), "Expected 2 rows to be fetched");

        // There is no async fetch pending, so this should fetch 1 row
        statement.fetchRows(1);

        assertEquals(3, listener.getRows().size(), "Expected 3 rows to be fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected 1 row to be fetched");
    }

    @Test
    public void testAsyncFetchRows_noAsyncFetchIfCursorNameIsSet() throws Exception {
        allocateStatement();
        statement.prepare("select RDB$CHARACTER_SET_NAME from RDB$CHARACTER_SETS order by RDB$CHARACTER_SET_NAME");
        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);
        statement.setCursorName("TEST");

        // this will be ignored as a cursor name is set
        statement.asyncFetchRows(10);

        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");
        assertNull(listener.getLastFetchCount(), "Expected no rows to be fetched yet");

        // Should fetch 1 rows, as the request to fetch 10 rows asynchronously was ignored
        statement.fetchRows(1);

        assertEquals(1, listener.getRows().size(), "Expected 1 rows to be fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected 1 rows to be fetched");
    }

    @Test
    public void testAsyncFetchRows_allowCancel() throws Exception {
        allocateStatement();
        statement.prepare("select RDB$CHARACTER_SET_NAME from RDB$CHARACTER_SETS order by RDB$CHARACTER_SET_NAME");
        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);
        TestOperationAware syncCancelOperationAware = new TestOperationAware() {
            @Override
            public void startOperation(Operation operation) {
                super.startOperation(operation);
                if (operation.getType() == Operation.Type.STATEMENT_ASYNC_FETCH_START) {
                    try {
                        operation.cancel();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        try {
            OperationMonitor.initOperationAware(syncCancelOperationAware);
            var exception = assertThrows(SQLException.class, () -> statement.asyncFetchRows(10));
            assertThat(exception, errorCodeEquals(ISCConstants.isc_cancelled));
        } finally {
            OperationMonitor.initOperationAware(null);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1} )
    public void testAsyncFetchRows_rejectNonPositiveFetchSize(int fetchSize) throws Exception {
        allocateStatement();
        statement.prepare("select RDB$CHARACTER_SET_NAME from RDB$CHARACTER_SETS order by RDB$CHARACTER_SET_NAME");
        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        var exception = assertThrows(SQLException.class, () -> statement.asyncFetchRows(fetchSize));
        assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_invalidFetchSize));
    }

}
