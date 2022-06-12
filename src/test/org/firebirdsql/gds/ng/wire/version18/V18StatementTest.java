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
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.CursorFlag;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FetchType;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.gds.ng.wire.version16.TestV16Statement;
import org.junit.ClassRule;
import org.junit.Test;

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
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;
import static org.firebirdsql.gds.ISCConstants.INF_RECORD_COUNT;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link V18Statement} in the V18 protocol, reuses test for V16.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public class V18StatementTest extends TestV16Statement {

    protected static final String CURSOR_NAME = "SOME_CURSOR";

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(18);

    public V18StatementTest() {
        this(new V18CommonConnectionInfo());
    }

    protected V18StatementTest(V18CommonConnectionInfo commonConnectionInfo) {
        super(commonConnectionInfo);
    }

    @Test
    public void testGetCursorInfo_notAllowedBeforeFetch() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        expectedException.expect(SQLException.class);
        expectedException.expect(errorCodeEquals(ISCConstants.isc_cursor_not_open));

        stmtInfo.getCursorRecordCount();
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
        assertEquals("expected row 1", 1, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.NEXT, 4, -1);
        stmtInfo.assertRowsReceived(4);
        assertEquals("expected row 5", 5, stmtInfo.getInt(3, 0));
        stmtInfo.clearReceivedRows();

        assertNotEquals("expected after last not yet signalled", Boolean.TRUE, stmtInfo.isAfterLast());

        statement.fetchScroll(FetchType.NEXT, 1, -1);
        stmtInfo.assertRowsReceived(0);
        assertEquals("expected after last signalled", Boolean.TRUE, stmtInfo.isAfterLast());
    }

    @Test
    public void testFetchScroll_nextAfterReachingBeforeFirstOfCursor() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.NEXT, 1, -1);
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.PRIOR, 1, -1);
        stmtInfo.assertRowsReceived(0);
        assertEquals("expected before first signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());

        statement.fetchScroll(FetchType.NEXT, 1, -1);
        assertEquals("expected row 1", 1, stmtInfo.getInt(0, 0));
        // NOTE: This partially tests SimpleStatementListener
        assertEquals("expected before first no longer signalled", Boolean.FALSE, stmtInfo.isBeforeFirst());
        assertEquals("expected after last not signalled", Boolean.FALSE, stmtInfo.isAfterLast());
    }

    @Test
    public void testFetchScroll_nextForUpdate_fetchesOneRowIgnoringFetchSize() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5, ScrollTestFeature.SCROLLABLE, ScrollTestFeature.FOR_UPDATE);

        statement.fetchScroll(FetchType.NEXT, 5, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 1", 1, stmtInfo.getInt(0, 0));
    }

    @Test
    public void testFetchScroll_prior() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 5);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 5", 5, stmtInfo.getInt(0, 0));
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.PRIOR, 3, -1);
        stmtInfo.assertRowsReceived(3);
        assertEquals(Arrays.asList(4, 3, 2), stmtInfo.extractRows(rowValue -> stmtInfo.extractInt(rowValue, 0)));
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.PRIOR, 2, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 1", 1, stmtInfo.getInt(0, 0));

        assertEquals("expected before first signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
    }

    @Test
    public void testFetchScroll_priorAfterReachingAfterLastOfCursor() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.NEXT, 6, -1);
        assertEquals("expected after last fetched signalled", Boolean.TRUE, stmtInfo.isAfterLast());
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.PRIOR, 1, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 5", 5, stmtInfo.getInt(0, 0));
        // NOTE: Technically tests SimpleStatementListener
        assertEquals("expected after last no longer signalled", Boolean.FALSE, stmtInfo.isAfterLast());
    }

    @Test
    public void testFetchScroll_priorForUpdate_fetchesOneRowIgnoringFetchSize() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5, ScrollTestFeature.SCROLLABLE, ScrollTestFeature.FOR_UPDATE);

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 6);
        stmtInfo.assertRowsReceived(0);

        statement.fetchScroll(FetchType.PRIOR, 5, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 5", 5, stmtInfo.getInt(0, 0));
    }

    // TODO Consider splitting this in multiple tests
    @Test
    public void testFetchScroll_absolute() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 3);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 3", 3, stmtInfo.getInt(0, 0));
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 6);
        stmtInfo.assertRowsReceived(0);
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertEquals("expected after last signalled", Boolean.TRUE, stmtInfo.isAfterLast());

        // Negative absolute positions are calculated from end of cursor
        statement.fetchScroll(FetchType.ABSOLUTE, -1, -5);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 1", 1, stmtInfo.getInt(0, 0));
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 0);
        stmtInfo.assertRowsReceived(0);
        assertEquals("expected before first signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
    }

    // TODO Consider splitting this in multiple tests
    @Test
    public void testFetchScroll_relative() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.ABSOLUTE, -1, 3);
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.RELATIVE, -1, 1);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 4", 4, stmtInfo.getInt(0, 0));
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.RELATIVE, -1, -3);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 1", 1, stmtInfo.getInt(0, 0));
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
        stmtInfo.clearReceivedRows();

        statement.fetchScroll(FetchType.RELATIVE, -1, -1);
        stmtInfo.assertRowsReceived(0);
        assertEquals("expected before first signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());

        statement.fetchScroll(FetchType.RELATIVE, -1, 6);
        stmtInfo.assertRowsReceived(0);
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertEquals("expected after last signalled", Boolean.TRUE, stmtInfo.isAfterLast());
    }

    @Test
    public void testFetchScroll_relative_zero() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.RELATIVE, -1, 0);
        stmtInfo.assertRowsReceived(0);

        int recordCount = stmtInfo.getCursorRecordCount();
        assertEquals("unexpected record count", 5, recordCount);

        statement.fetchScroll(FetchType.RELATIVE, -1, 1);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 1", 1, stmtInfo.getInt(0, 0));
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
    }

    @Test
    public void testFetchScroll_first() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.FIRST, -1, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 1", 1, stmtInfo.getInt(0, 0));
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
    }

    @Test
    public void testFetchScroll_first_noRowsPositionsAfterLast() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(0);

        statement.fetchScroll(FetchType.FIRST, -1, -1);
        stmtInfo.assertRowsReceived(0);
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertEquals("expected after last signalled", Boolean.TRUE, stmtInfo.isAfterLast());
    }

    @Test
    public void testFetchScroll_last() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(5);

        statement.fetchScroll(FetchType.LAST, -1, -1);
        stmtInfo.assertRowsReceived(1);
        assertEquals("expected row 5", 5, stmtInfo.getInt(0, 0));
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertNotEquals("expected after last not signalled", Boolean.TRUE, stmtInfo.isAfterLast());
    }

    @Test
    public void testFetchScroll_last_noRowsPositionsAfterLast() throws Exception {
        StatementInfo stmtInfo = prepareScrollTest(0);

        statement.fetchScroll(FetchType.LAST, -1, -1);
        stmtInfo.assertRowsReceived(0);
        assertNotEquals("expected before first not signalled", Boolean.TRUE, stmtInfo.isBeforeFirst());
        assertEquals("expected after last signalled", Boolean.TRUE, stmtInfo.isAfterLast());
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
        assertEquals("unexpected record count", 500, recordCount);

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
        assertEquals("expected 1 row", 2, stmtInfo.receivedRowsSize());
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
            DdlHelper.executeDDL(connection, "create table scrolltest (id integer primary key)");
            if (numberOfRecords <= 0) return;
            try (PreparedStatement pstmt = connection.prepareStatement(
                    // @formatter:off
                    "execute block (records INTEGER = ?)\n" +
                    "as\n" +
                    "  declare id integer = 1;\n" +
                    "begin\n" +
                    "  while (id <= records) do\n" +
                    "  begin\n" +
                    "    insert into scrolltest (id) values (:id);\n" +
                    "    id = id + 1;\n" +
                    "  end\n" +
                    "end"
                    // @formatter:on
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
     *
     * TODO Consider if it makes sense to move this up in the StatementTest hierarchy for reuse in other tests
     */
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
            assertEquals(format("expected %d rows", expectedRowCount), expectedRowCount, receivedRowsSize());
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
                    throw new SQLException("Unexpected response buffer");
                }
                int length = iscVaxInteger2(buffer, 1);
                return iscVaxInteger(buffer, 3, length);
            });
        }

    }

}
