// SPDX-FileCopyrightText: Copyright 2021-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.CursorFlag;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FetchType;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.gds.ng.wire.version16.V16StatementTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.gds.ISCConstants.INF_RECORD_COUNT;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link V18Statement} in the V18 protocol, reuses test for V16.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class V18StatementTest extends V16StatementTest {

    protected static final String CURSOR_NAME = "SOME_CURSOR";

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(18);

    protected V18CommonConnectionInfo commonConnectionInfo() {
        return new V18CommonConnectionInfo();
    }

    @Test
    public void testGetCursorInfo_notAllowedBeforeFetch() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        SQLException exception = assertThrows(SQLException.class, stmtInfo::getCursorRecordCount);
        assertThat(exception, errorCodeEquals(ISCConstants.isc_cursor_not_open));
    }

    @Test
    public void testGetCursorInfo_allowedAfterFetch() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchRows(1);

        assertEquals(5, stmtInfo.getCursorRecordCount());
    }

    @Test
    public void testGetCursorInfo_minusOne_forNonScrollable() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5, EnumSet.noneOf(ScrollTestFeature.class));

        statement.fetchRows(1);

        assertEquals(-1, stmtInfo.getCursorRecordCount());
    }

    @Test
    public void testFetchScroll_next() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.NEXT, 1, -1);

        stmtInfo.assertRowsReceived(1);
        assertEquals(1, stmtInfo.getInt(0, 0), "expected row 1");
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.NEXT, 4, -1);
        stmtInfo.assertRowsReceived(4);
        assertEquals(5, stmtInfo.getInt(3, 0), "expected row 5");
        stmtInfo.clearReceivedRows();

        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not yet signalled");

        statement.fetchScroll(FetchType.NEXT, 1, -1);
        stmtInfo.assertRowsReceived(0);
        assertEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last signalled");
    }

    @Test
    public void testFetchScroll_nextAfterReachingBeforeFirstOfCursor() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.NEXT, 1, -1);
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.PRIOR, 1, -1);
        stmtInfo.assertRowsReceived(0);
        assertEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first signalled");

        statement.fetchScroll(FetchType.NEXT, 1, -1);
        assertEquals(1, stmtInfo.getInt(0, 0), "expected row 1");
        // NOTE: This partially tests SimpleStatementListener
        assertEquals(Boolean.FALSE, stmtInfo.isBeforeFirst(), "expected before first no longer signalled");
        assertEquals(Boolean.FALSE, stmtInfo.isAfterLast(), "expected after last not signalled");
    }

    @Test
    public void testFetchScroll_nextForUpdate_fetchesOneRowIgnoringFetchSize() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5, ScrollTestFeature.SCROLLABLE, ScrollTestFeature.FOR_UPDATE);

        statement.fetchScroll(FetchType.NEXT, 5, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(1, stmtInfo.getInt(0, 0), "expected row 1");
    }

    @Test
    public void testFetchScroll_prior() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 5);
        stmtInfo.assertRowsReceived(1);
        assertEquals(5, stmtInfo.getInt(0, 0), "expected row 5");
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.PRIOR, 3, -1);
        stmtInfo.assertRowsReceived(3);
        assertEquals(Arrays.asList(4, 3, 2), stmtInfo.extractRows(rowValue -> stmtInfo.extractInt(rowValue, 0)));
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.PRIOR, 2, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(1, stmtInfo.getInt(0, 0), "expected row 1");

        assertEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
    }

    @Test
    public void testFetchScroll_priorAfterReachingAfterLastOfCursor() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.NEXT, 6, -1);
        assertEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last fetched signalled");
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.PRIOR, 1, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(5, stmtInfo.getInt(0, 0), "expected row 5");
        // NOTE: Technically tests SimpleStatementListener
        assertEquals(Boolean.FALSE, stmtInfo.isAfterLast(), "expected after last no longer signalled");
    }

    @Test
    public void testFetchScroll_priorForUpdate_fetchesOneRowIgnoringFetchSize() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5, ScrollTestFeature.SCROLLABLE, ScrollTestFeature.FOR_UPDATE);

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 6);
        stmtInfo.assertRowsReceived(0);

        statement.fetchScroll(FetchType.PRIOR, 5, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(5, stmtInfo.getInt(0, 0), "expected row 5");
    }

    // TODO Consider splitting this in multiple tests
    @Test
    public void testFetchScroll_absolute() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 3);
        stmtInfo.assertRowsReceived(1);
        assertEquals(3, stmtInfo.getInt(0, 0), "expected row 3");
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 6);
        stmtInfo.assertRowsReceived(0);
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last signalled");

        // Negative absolute positions are calculated from end of cursor
        statement.fetchScroll(FetchType.ABSOLUTE, -1, -5);
        stmtInfo.assertRowsReceived(1);
        assertEquals(1, stmtInfo.getInt(0, 0), "expected row 1");
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 0);
        stmtInfo.assertRowsReceived(0);
        assertEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
    }

    // TODO Consider splitting this in multiple tests
    @Test
    public void testFetchScroll_relative() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 3);
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.RELATIVE, -1, 1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(4, stmtInfo.getInt(0, 0), "expected row 4");
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.RELATIVE, -1, -3);
        stmtInfo.assertRowsReceived(1);
        assertEquals(1, stmtInfo.getInt(0, 0), "expected row 1");
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.RELATIVE, -1, -1);
        stmtInfo.assertRowsReceived(0);
        assertEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");

        statement.fetchScroll(FetchType.RELATIVE, -1, 6);
        stmtInfo.assertRowsReceived(0);
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last signalled");
    }

    @Test
    public void testFetchScroll_relative_zero() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.RELATIVE, -1, 0);
        stmtInfo.assertRowsReceived(0);

        int recordCount = stmtInfo.getCursorRecordCount();
        assertEquals(5, recordCount, "unexpected record count");

        statement.fetchScroll(FetchType.RELATIVE, -1, 1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(1, stmtInfo.getInt(0, 0), "expected row 1");
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
    }

    @Test
    public void testFetchScroll_first() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.FIRST, -1, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(1, stmtInfo.getInt(0, 0), "expected row 1");
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
    }

    @Test
    public void testFetchScroll_first_noRowsPositionsAfterLast() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(0);

        statement.fetchScroll(FetchType.FIRST, -1, -1);
        stmtInfo.assertRowsReceived(0);
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last signalled");
    }

    @Test
    public void testFetchScroll_last() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.LAST, -1, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(5, stmtInfo.getInt(0, 0), "expected row 5");
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertNotEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last not signalled");
    }

    @Test
    public void testFetchScroll_last_noRowsPositionsAfterLast() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(0);

        statement.fetchScroll(FetchType.LAST, -1, -1);
        stmtInfo.assertRowsReceived(0);
        assertNotEquals(Boolean.TRUE, stmtInfo.isBeforeFirst(), "expected before first not signalled");
        assertEquals(Boolean.TRUE, stmtInfo.isAfterLast(), "expected after last signalled");
    }

    // TODO Check if other tests cover this and remove
    @Test
    public void testScrollBasic() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(500);

        statement.fetchScroll(FetchType.ABSOLUTE, 1, 10);
        stmtInfo.assertRowsReceived(1);
        assertEquals(10, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        int recordCount = stmtInfo.getCursorRecordCount();
        assertEquals(500, recordCount, "unexpected record count");

        statement.fetchScroll(FetchType.RELATIVE, 1, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(9, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.ABSOLUTE, 1, 10);
        stmtInfo.assertRowsReceived(1);
        assertEquals(10, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.LAST, 1, 1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(500, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.FIRST, 1, 1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(1, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.ABSOLUTE, 1, 10);
        stmtInfo.assertRowsReceived(1);
        assertEquals(10, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.PRIOR, 2, 1);
        assertEquals(2, stmtInfo.receivedRowsSize(), "expected 1 row");
        assertEquals(Arrays.asList(9, 8), stmtInfo.extractRows(rowValue -> stmtInfo.extractInt(rowValue, 0)));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.ABSOLUTE, 1, 20);
        stmtInfo.assertRowsReceived(1);
        assertEquals(20, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.RELATIVE, 1, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(19, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.ABSOLUTE, 1, 10);
        stmtInfo.assertRowsReceived(1);
        assertEquals(10, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.NEXT, 5, 0);
        stmtInfo.assertRowsReceived(5);
        assertEquals(Arrays.asList(11, 12, 13, 14, 15),
                stmtInfo.extractRows(rowValue -> stmtInfo.extractInt(rowValue, 0)));
        stmtInfo.clearReceivedRows();

        IntStream.range(0, 10).forEach(i -> {
            try {
                statement.fetchScroll(FetchType.RELATIVE, 1, -1);
            } catch (SQLException e) {
                fail("could not fetch");
            }
        });
        stmtInfo.assertRowsReceived(10);
        assertEquals(Arrays.asList(14, 13, 12, 11, 10, 9, 8, 7, 6, 5),
                stmtInfo.extractRows(rowValue -> stmtInfo.extractInt(rowValue, 0)));
        stmtInfo.clearReceivedRows();


        statement.fetchScroll(FetchType.ABSOLUTE, 1, 1);
        stmtInfo.assertRowsReceived(1);
        assertEquals(1, stmtInfo.getInt(0, 0));

        IntStream.range(1, 50).forEach(i -> {
            stmtInfo.clearReceivedRows();
            try {
                statement.fetchScroll(FetchType.LAST, 1, 1);
//                System.out.println(stmtInfo.getInt(0, 0));
            } catch (SQLException e) {
                fail("could not fetch");
            }
        });
        stmtInfo.assertRowsReceived(1);
        assertEquals(500, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.ABSOLUTE, 1, 10);
        stmtInfo.assertRowsReceived(1);
        assertEquals(10, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.RELATIVE, 0, 5);
//        stmtInfo.assertRowsReceived(0);
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.NEXT, 5, 0);
        stmtInfo.assertRowsReceived(5);
        assertEquals(Arrays.asList(16, 17, 18, 19, 20),
                stmtInfo.extractRows(rowValue -> stmtInfo.extractInt(rowValue, 0)));
        stmtInfo.clearReceivedRows();
    }

    @Test
    public void testAsyncFetchRows_noAsyncFetchIfScrollable() throws Exception {
        allocateStatement();
        statement.setCursorFlag(CursorFlag.CURSOR_TYPE_SCROLLABLE);
        statement.prepare("select RDB$CHARACTER_SET_NAME from RDB$CHARACTER_SETS order by RDB$CHARACTER_SET_NAME");
        statement.addStatementListener(listener);

        statement.execute(RowValue.EMPTY_ROW_VALUE);

        // async fetch is ignored for scrollable cursor
        statement.asyncFetchRows(10);

        assertEquals(0, listener.getRows().size(), "Expected no rows to be fetched yet");
        assertNull(listener.getLastFetchCount(), "Expected no rows to be fetched yet");

        // There is no async fetch pending, so this should fetch 1 row
        statement.fetchRows(1);

        assertEquals(1, listener.getRows().size(), "Expected 1 row to be fetched");
        assertEquals(1, listener.getLastFetchCount(), "Expected 1 row to be fetched");
    }

    /**
     * Calls {@link #prepareScrollTest(int, Set)} with only {@link ScrollTestFeature#SCROLLABLE}.
     */
    protected StatementInfo prepareScrollTest(int numberOfRecords) throws SQLException {
        return prepareScrollTest(numberOfRecords, EnumSet.of(ScrollTestFeature.SCROLLABLE));
    }

    /**
     * Prepares data for a scroll fetch test with {@code numberOfRecords}, and executes a select for those records.
     *
     * @param numberOfRecords Number of records to generate
     * @param scrollTestFeatures configuration for test
     */
    @SuppressWarnings("SameParameterValue")
    protected StatementInfo prepareScrollTest(int numberOfRecords, ScrollTestFeature... scrollTestFeatures) throws SQLException {
        EnumSet<ScrollTestFeature> features = EnumSet.noneOf(ScrollTestFeature.class);
        features.addAll(Arrays.asList(scrollTestFeatures));
        return prepareScrollTest(numberOfRecords, features);
    }

    /**
     * Prepares data for a scroll fetch test with {@code numberOfRecords}, and executes a select for those records.
     *
     * @param numberOfRecords Number of records to generate
     * @param scrollTestFeatures configuration for test
     */
    protected StatementInfo prepareScrollTest(int numberOfRecords, Set<ScrollTestFeature> scrollTestFeatures)
            throws SQLException {
        setupTableForScrollTest(numberOfRecords);
        allocateStatement();
        if (scrollTestFeatures.contains(ScrollTestFeature.FOR_UPDATE)) {
            statement.prepare("select id from scrolltest order by id for update");
            statement.setCursorName(CURSOR_NAME);
        } else {
            statement.prepare("select id from scrolltest order by id");
        }
        if (scrollTestFeatures.contains(ScrollTestFeature.SCROLLABLE)) {
            statement.setCursorFlag(CursorFlag.CURSOR_TYPE_SCROLLABLE);
        }
        StatementInfo stmtInfo = new StatementInfo(statement);
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        return stmtInfo;
    }

    private void setupTableForScrollTest(int numberOfRecords) throws SQLException {
        try (Connection connection = getConnectionViaDriverManager()) {
            DdlHelper.executeDDL(connection, "recreate table scrolltest (id integer primary key)");
            if (numberOfRecords <= 0) return;
            try (PreparedStatement pstmt = connection.prepareStatement("""
                    execute block (records INTEGER = ?)
                    as
                      declare id integer = 1;
                    begin
                      while (id <= records) do
                      begin
                        insert into scrolltest (id) values (:id);
                        id = id + 1;
                      end
                    end"""
            )) {
                pstmt.setInt(1, numberOfRecords);
                pstmt.execute();
            }
        }
    }

    protected enum ScrollTestFeature {
        SCROLLABLE,
        FOR_UPDATE
    }

    /**
     * Holds information and data on a statement.
     */
    // TODO Consider if it makes sense to move this up in the StatementTest hierarchy for reuse in other tests
    protected static final class StatementInfo {

        private final FbStatement statement;
        private final SimpleStatementListener statementListener = new SimpleStatementListener();
        private final List<RowValue> receivedRows = statementListener.getRows();

        public StatementInfo(FbStatement statement) {
            this.statement = statement;
            statement.addStatementListener(statementListener);
        }

        public int receivedRowsSize() {
            return receivedRows.size();
        }

        public void assertRowsReceived(int expectedRowCount) {
            assertEquals(expectedRowCount, receivedRowsSize(), () -> format("expected %d rows", expectedRowCount));
        }

        public void clearReceivedRows() {
            receivedRows.clear();
        }

        public RowValue getRow(int rowIndex) {
            return receivedRows.get(rowIndex);
        }

        public int getInt(int rowIndex, int columnIndex) {
            RowValue row = getRow(rowIndex);
            return extractInt(row, columnIndex);
        }

        public int extractInt(RowValue rowValue, int fieldIndex) {
            return statement.getDatabase().getDatatypeCoder().decodeInt(rowValue.getFieldData(fieldIndex));
        }

        public <T> List<T> extractRows(Function<RowValue, T> rowMapper) {
            return receivedRows.stream().map(rowMapper).collect(toList());
        }

        public Boolean isBeforeFirst() {
            return statementListener.isBeforeFirst();
        }

        public Boolean isAfterLast() {
            return statementListener.isAfterLast();
        }

        public int getCursorRecordCount() throws SQLException {
            return statement.getCursorInfo(new byte[] { (byte) INF_RECORD_COUNT, isc_info_end }, 10, buffer -> {
                if (buffer[0] != INF_RECORD_COUNT) {
                    throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_infoResponseEmpty)
                            .messageParameter("cursor")
                            .toSQLException();
                }
                int length = iscVaxInteger2(buffer, 1);
                return iscVaxInteger(buffer, 3, length);
            });
        }

    }

}
