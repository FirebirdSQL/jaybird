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

import org.firebirdsql.common.StreamHelper;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.common.function.ThrowingBiConsumer;
import org.firebirdsql.gds.ng.CursorFlag;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultFbConnectionProperties;
import static org.firebirdsql.common.FBTestProperties.getDefaultTpb;
import static org.firebirdsql.common.extension.GdsTypeExtension.excludesNativeOnly;
import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.firebirdsql.common.extension.UsesDatabaseExtension.usesDatabaseForAll;
import static org.firebirdsql.jdbc.FetcherAssertions.assertAfterLast;
import static org.firebirdsql.jdbc.FetcherAssertions.assertAtRow;
import static org.firebirdsql.jdbc.FetcherAssertions.assertBeforeFirst;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FBServerScrollFetcherTest {

    private static final int FETCH_SIZE_NOT_IMPORTANT = 10;

    @RegisterExtension
    @Order(1)
    static GdsTypeExtension gdsTypeExtension = excludesNativeOnly();

    @RegisterExtension
    @Order(2)
    static RequireProtocolExtension requireProtocolExtension = requireProtocolVersion(18);

    @RegisterExtension
    static UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = usesDatabaseForAll(
            "create table scrolltest (id integer primary key)");

    private FbWireDatabase db;
    private final SimpleFetcherListener listener = new SimpleFetcherListener();

    @SuppressWarnings("resource")
    @BeforeEach
    void setup() throws SQLException {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(getDefaultFbConnectionProperties());
        gdsConnection.socketConnect();
        db = gdsConnection.identify();
        db.attach();
    }

    @AfterEach
    void tearDown() throws SQLException {
        db.close();
    }

    @ParameterizedTest(name = "[{index}] withMaxRows = {0}")
    @ValueSource(booleans = { false, true })
    void firstNoRows(boolean withMaxRows) throws Throwable {
        executeScrollTest(0, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? 5 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            assertFalse(fetcher.first(), "expected no first row");
            assertAfterLast(fetcher);
            assertRowToNull(0);
        });
    }

    @ParameterizedTest(name = "[{index}] initialRow = {0}, withMaxRows = {1}")
    @CsvSource({
            "0, false", "0, true", "1, false", "1, true", "2, false", "2, true", "5, false", "5, true",
            "10, false", "10, true"
    })
    void firstFromRow(int initialRow, boolean withMaxRows) throws Throwable {
        int numberOfRows = Math.max(5, initialRow);
        int initialCount = withMaxRows ? numberOfRows + 5 : numberOfRows;
        executeScrollTest(initialCount, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? numberOfRows : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            if (initialRow != 0) {
                assertTrue(fetcher.absolute(initialRow), "expected move to row");
                assertAtRow(fetcher, initialRow);
                listener.clearRows();
            }

            assertTrue(fetcher.first(), "expected first row");
            assertAtRow(fetcher, 1);
            assertTrue(fetcher.isFirst(), "expected first");
            assertFalse(fetcher.isLast(), "expected not last");
            assertRowValue(0, 1);
        });
    }

    @ParameterizedTest(name = "[{index}] withMaxRows = {0}")
    @ValueSource(booleans = { false, true })
    void lastNoRows(boolean withMaxRows) throws Throwable {
        executeScrollTest(0, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? 5 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            assertFalse(fetcher.last(), "expected no last row");
            assertAfterLast(fetcher);
            assertRowToNull(0);
        });
    }

    @ParameterizedTest(name = "[{index}] initialRow = {0}, withMaxRows = {1}")
    @CsvSource({
            "0, false", "0, true", "1, false", "1, true", "2, false", "2, true", "5, false", "5, true",
            "10, false", "10, true"
    })
    void lastFromRow(int initialRow, boolean withMaxRows) throws Throwable {
        int numberOfRows = Math.max(5, initialRow);
        int createdRows = withMaxRows ? numberOfRows + 5 : numberOfRows;
        executeScrollTest(createdRows, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? numberOfRows : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            if (initialRow != 0) {
                assertTrue(fetcher.absolute(initialRow), "expected move to row");
                assertAtRow(fetcher, initialRow);
                listener.clearRows();
            }

            assertTrue(fetcher.last(), "expected last row");
            assertAtRow(fetcher, numberOfRows);
            assertFalse(fetcher.isFirst(), "expected not first");
            assertTrue(fetcher.isLast(), "expected last");
            assertRowValue(0, numberOfRows);
        });
    }

    @ParameterizedTest(name = "[{index}] withMaxRows = {0}")
    @ValueSource(booleans = { false, true })
    void firstLastFirst_singleRow(boolean withMaxRows) throws Throwable {
        int initialCount = withMaxRows ? 5 : 1;
        executeScrollTest(initialCount, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? 1 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            assertTrue(fetcher.first(), "expected first row");
            assertAtRow(fetcher, 1);
            assertTrue(fetcher.isFirst(), "expected first");
            assertTrue(fetcher.isLast(), "expected last");
            assertRowValue(0, 1);

            assertTrue(fetcher.last(), "expected last row");
            assertAtRow(fetcher, 1);
            assertTrue(fetcher.isFirst(), "expected first");
            assertTrue(fetcher.isLast(), "expected last");
            assertRowValue(0, 1);

            assertTrue(fetcher.first(), "expected first row");
            assertAtRow(fetcher, 1);
            assertTrue(fetcher.isFirst(), "expected first");
            assertTrue(fetcher.isLast(), "expected last");
            assertRowValue(0, 1);
        });
    }

    @ParameterizedTest(name = "[{index}] withMaxRows = {0}")
    @ValueSource(booleans = { false, true })
    void nextNoRows(boolean withMaxRows) throws Throwable {
        executeScrollTest(0, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? 5 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            assertFalse(fetcher.next(), "expected no next row");
            assertAfterLast(fetcher);
            assertRowToNull(0);
        });
    }

    @ParameterizedTest(name = "[{index}] fetchSize = {0}, withMaxRows = {1}")
    @CsvSource({ "1, false", "1, true", "3, false", "3, true", "5, false", "5, true", "10, false", "10, true" })
    void nextToEnd(int fetchSize, boolean withMaxRows) throws Throwable {
        int initialCount = withMaxRows ? 10 : 5;
        executeScrollTest(initialCount, fetchSize, withMaxRows ? 5 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            IntStream.rangeClosed(1, 5)
                    .forEach(row -> {
                        try {
                            assertTrue(fetcher.next(), "expected next row " + row);
                            assertAtRow(fetcher, row);
                            assertEquals(row == 1, fetcher.isFirst(), "expected first for row = 1, row: " + row);
                            assertEquals(row == 5, fetcher.isLast(), "expected last for row = 5, row: " + row);
                            assertRowValue(row - 1, row);
                        } catch (SQLException e) {
                            throw new RuntimeException("exception during test", e);
                        }
                    });

            assertFalse(fetcher.next(), "expected no more rows");
            assertAfterLast(fetcher);
            assertRowToNull(5);
        });
    }

    /**
     * Rationale: during implementation a logic error surfaced which caused {@code next()} as the first operation
     * to misbehave when max rows was non-zero. The "normal" tests didn't find this because {@code assertBeforeFirst}
     * hid this problem.
     */
    @Test
    void nextOnlyWithMaxRows_findsRow() throws Throwable {
        executeScrollTest(5, 5, 2, (stmt, fetcher) -> {
            assertTrue(fetcher.next(), "expected row 1");
            assertAtRow(fetcher, 1);
            assertTrue(fetcher.isFirst(), "expected first for row = 1");
            assertFalse(fetcher.isLast(), "expected not last for row = 1");
            assertRowValue(0, 1);

            assertTrue(fetcher.next(), "expected row 2");
            assertAtRow(fetcher, 2);
            assertFalse(fetcher.isFirst(), "expected not first for row = 2");
            assertTrue(fetcher.isLast(), "expected last for row = 2");
            assertRowValue(1, 2);

            assertFalse(fetcher.next(), "expected no more rows");
            assertAfterLast(fetcher);
            assertRowToNull(2);
        });
    }

    @ParameterizedTest(name = "[{index}] withMaxRows = {0}")
    @ValueSource(booleans = { false, true })
    void previousFromEnd_noRows(boolean withMaxRows) throws Throwable {
        executeScrollTest(0, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? 5 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            fetcher.afterLast();
            assertAfterLast(fetcher);
            assertRowToNull(0);

            assertFalse(fetcher.previous(), "expected no previous row");
            assertBeforeFirst(fetcher);
            assertRowToNull(1);
        });
    }

    @ParameterizedTest(name = "[{index}] fetchSize = {0}, withMaxRows = {1}")
    @CsvSource({ "1, false", "1, true", "3, false", "3, true", "5, false", "5, true", "10, false", "10, true" })
    void previousFromEndToStart(int fetchSize, boolean withMaxRows) throws Throwable {
        int initialCount = withMaxRows ? 10 : 5;
        executeScrollTest(initialCount, fetchSize, withMaxRows ? 5 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            fetcher.afterLast();
            assertAfterLast(fetcher);
            assertRowToNull(0);
            listener.clearRows();

            StreamHelper.reverseClosedRange(1, 5)
                    .forEach(row -> {
                        try {
                            assertTrue(fetcher.previous(), "expected next row " + row);
                            assertAtRow(fetcher, row);
                            assertEquals(row == 1, fetcher.isFirst(), "expected first for row = 1, row: " + row);
                            assertEquals(row == 5, fetcher.isLast(), "expected last for row = 5, row: " + row);
                            assertRowValue(5 - row, row);
                        } catch (SQLException e) {
                            throw new RuntimeException("exception during test", e);
                        }
                    });

            assertFalse(fetcher.previous(), "expected no more rows");
            assertBeforeFirst(fetcher);
            assertRowToNull(5);
        });
    }

    @ParameterizedTest(name = "[{index}] fetchSize = {0}, withMaxRows = {1}")
    @CsvSource({
            "1, false", "1, true", "3, false", "3, true", "5, false", "5, true", "7, false", "7, true",
            "10, false", "10, true", "20, false", "20, true"
    })
    void nextPreviousCombinations(int fetchSize, boolean withMaxRows) throws Throwable {
        int initialCount = withMaxRows ? 30 : 20;
        executeScrollTest(initialCount, fetchSize, withMaxRows ? 20 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            int receivedRow = -1;
            int row = 0;
            do {
                row++;
                assertTrue(fetcher.next(), "expected next row");
                assertAtRow(fetcher, row);
                assertRowValue(++receivedRow, row);
            } while (row < 7);

            do {
                row--;
                assertTrue(fetcher.previous(), "expected previous row");
                assertAtRow(fetcher, row);
                assertRowValue(++receivedRow, row);
            } while (row > 3);

            do {
                row++;
                assertTrue(fetcher.next(), "expected next row");
                assertAtRow(fetcher, row);
                assertRowValue(++receivedRow, row);
            } while (row < 20);

            do {
                row--;
                assertTrue(fetcher.previous(), "expected previous row");
                assertAtRow(fetcher, row);
                assertRowValue(++receivedRow, row);
            } while (row > 1);

            do {
                row++;
                assertTrue(fetcher.next(), "expected next row");
                assertAtRow(fetcher, row);
                assertRowValue(++receivedRow, row);
            } while (row < 20);
        });
    }

    @ParameterizedTest(name = "[{index}] withMaxRows = {0}")
    @ValueSource(booleans = { false, true })
    void absolute_variousPositions(boolean withMaxRows) throws Throwable {
        int initialCount = withMaxRows ? 20 : 10;
        executeScrollTest(initialCount, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? 10 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            assertTrue(fetcher.absolute(5), "expected absolute row 5");
            assertAtRow(fetcher, 5);
            int receivedRow;
            assertRowValue(receivedRow = 0, 5);

            assertTrue(fetcher.absolute(2), "expected absolute row 2");
            assertAtRow(fetcher, 2);
            assertRowValue(++receivedRow, 2);

            assertTrue(fetcher.absolute(-7), "expected absolute row -7 (== 4)");
            assertAtRow(fetcher, 4);
            assertRowValue(++receivedRow, 4);

            assertFalse(fetcher.absolute(0), "expected no row 0 (before-first)");
            assertBeforeFirst(fetcher);
            assertRowToNull(++receivedRow);

            assertFalse(fetcher.absolute(11), "expected no row 11 (after-last)");
            assertAfterLast(fetcher);
            assertRowToNull(++receivedRow);

            assertFalse(fetcher.absolute(-11), "expected no row -11 (before-first)");
            assertBeforeFirst(fetcher);
            assertRowToNull(++receivedRow);
        });
    }

    @ParameterizedTest(name = "[{index}] withMaxRows = {0}")
    @ValueSource(booleans = { false, true })
    void relative_variousPositions(boolean withMaxRows) throws Throwable {
        int initialCount = withMaxRows ? 20 : 10;
        executeScrollTest(initialCount, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? 10 : 0, (stmt, fetcher) -> {
            assertBeforeFirst(fetcher);

            fetcher.relative(0);
            assertBeforeFirst(fetcher);
            int receivedRow;
            assertRowToNull(receivedRow = 0);

            fetcher.relative(5);
            assertAtRow(fetcher, 5);
            assertRowValue(++receivedRow, 5);

            fetcher.relative(-1);
            assertAtRow(fetcher, 4);
            assertRowValue(++receivedRow, 4);

            fetcher.relative(3);
            assertAtRow(fetcher, 7);
            assertRowValue(++receivedRow, 7);

            fetcher.relative(-6);
            assertAtRow(fetcher, 1);
            assertRowValue(++receivedRow, 1);

            fetcher.relative(-1);
            assertBeforeFirst(fetcher);
            assertRowToNull(++receivedRow);

            fetcher.relative(10);
            assertAtRow(fetcher, 10);
            assertRowValue(++receivedRow, 10);

            fetcher.relative(1);
            assertAfterLast(fetcher);
            assertRowToNull(++receivedRow);

            fetcher.relative(-2);
            assertAtRow(fetcher, 9);
            assertRowValue(++receivedRow, 9);
        });
    }

    @ParameterizedTest(name = "[{index}] rowCount = {0}, withMaxRows = {1}")
    @CsvSource({ "0, false", "0, true", "5, false", "5, true" })
    void beforeFirst(int rowCount, boolean withMaxRows) throws Throwable {
        int initialCount = rowCount + (withMaxRows && rowCount != 0 ? 5 : 0);
        executeScrollTest(initialCount, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? Math.max(1, rowCount) : 0,
                (stmt, fetcher) -> {
                    assertBeforeFirst(fetcher);

                    fetcher.next();
                    assertFalse(fetcher.isBeforeFirst(), "should not be before-first");

                    fetcher.beforeFirst();
                    assertBeforeFirst(fetcher);
                });
    }

    @ParameterizedTest(name = "[{index}] rowCount = {0}, withMaxRows = {1}")
    @CsvSource({ "0, false", "0, true", "5, false", "5, true" })
    void afterLast(int rowCount, boolean withMaxRows) throws Throwable {
        int initialCount = rowCount + (withMaxRows && rowCount != 0 ? 5 : 0);
        executeScrollTest(initialCount, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? Math.max(1, rowCount) : 0,
                (stmt, fetcher) -> {
                    assertBeforeFirst(fetcher);

                    fetcher.afterLast();
                    assertAfterLast(fetcher);

                    fetcher.previous();
                    assertFalse(fetcher.isAfterLast(), "should not be after-last");
                });
    }

    @ParameterizedTest(name = "[{index}] withMaxRows = {0}")
    @ValueSource(booleans = { false, true })
    void isEmpty_noRows(boolean withMaxRows) throws Throwable {
        executeScrollTest(0, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? 5 : 0, (stmt, fetcher) -> {
                assertBeforeFirst(fetcher);
                assertTrue(fetcher.isEmpty(), "expected empty");
        });
    }

    @ParameterizedTest(name = "[{index}] rowCount = {0}, withMaxRows = {1}")
    @CsvSource({ "1, false", "1, true", "10, false", "10, true" })
    void isEmpty_withRows(int rowCount, boolean withMaxRows) throws Throwable {
        int initialCount = withMaxRows ? rowCount + 5 : rowCount;
        executeScrollTest(initialCount, FETCH_SIZE_NOT_IMPORTANT, withMaxRows ? rowCount : 0, (stmt, fetcher) ->
                assertFalse(fetcher.isEmpty(), "expected non-empty"));
    }

    // NOTE: Methods like isBeforeFirst(), isAfterLast() and getRowNum() are tested indirectly through the assertions
    // using these methods

    // insertRow, deleteRow and updateRow will be handled by FBUpdatableFetcher

    private void executeScrollTest(int numberOfRecords, int fetchSize, int maxRows,
            ThrowingBiConsumer<FbStatement, FBServerScrollFetcher> exceptionalConsumer) throws Throwable {
        setupTableForScrollTest(numberOfRecords);
        FbTransaction tr = getTransaction();
        try (FbStatement stmt = db.createStatement(tr)) {
            stmt.prepare("select id from scrolltest order by id");
            stmt.setCursorFlag(CursorFlag.CURSOR_TYPE_SCROLLABLE);
            stmt.execute(RowValue.EMPTY_ROW_VALUE);
            FBServerScrollFetcher fetcher = new FBServerScrollFetcher(fetchSize, maxRows, stmt, listener);

            exceptionalConsumer.accept(stmt, fetcher);
        } finally {
            tr.commit();
        }
    }

    private void assertRowValue(int listenerRow, int expectedValue) {
        listener.assertRow(listenerRow,
                rowValue -> assertEquals(expectedValue, extractId(rowValue), "unexpected row"));
    }

    private void assertRowToNull(int listenerRow) {
        listener.assertRow(listenerRow, rowValue -> assertNull(rowValue, "expected row change to null"));
    }

    private FbTransaction getTransaction() throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }

    private void setupTableForScrollTest(int numberOfRecords) throws SQLException {
        try (Connection connection = getConnectionViaDriverManager()) {
            try (PreparedStatement pstmt = connection.prepareStatement(
                    // @formatter:off
                    "execute block (records INTEGER = ?)\n" +
                    "as\n" +
                    "  declare id integer = 1;\n" +
                    "begin\n" +
                    "  delete from scrolltest;\n" +
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

    private int extractId(RowValue rowValue) {
        return db.getDatatypeCoder().decodeInt(rowValue.getFieldData(0));
    }

}