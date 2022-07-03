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
import org.firebirdsql.common.function.ThrowingTriConsumer;
import org.firebirdsql.gds.ng.CursorFlag;
import org.firebirdsql.gds.ng.DatatypeCoder;
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
import java.util.Objects;
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

class FBUpdatableFetcherTest {

    @RegisterExtension
    @Order(1)
    static GdsTypeExtension gdsTypeExtension = excludesNativeOnly();

    @RegisterExtension
    @Order(2)
    static RequireProtocolExtension requireProtocolExtension = requireProtocolVersion(18);

    @RegisterExtension
    static UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = usesDatabaseForAll(
            "create table scrolltest (id integer primary key, colval varchar(50))");

    private FbWireDatabase db;
    private final SimpleFetcherListener listener = new SimpleFetcherListener();
    private final RowValue deletedRowMarker = RowValue.deletedRowMarker(2);

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

    @Test
    void firstNoRows() throws Throwable {
        executeTest(0, (stmt, innerFetcher, fetcher) -> {
            assertFalse(fetcher.first(), "expected no first row");
            assertAfterLast(fetcher);
            assertRowToNull(0);
        });
    }

    @ParameterizedTest(name = "[{index}] rowCount = {0}")
    @ValueSource(ints = { 0, 1 })
    void firstVariableInitial_oneInserted(int rowCount) throws Throwable {
        executeTest(rowCount, (stmt, innerFetcher, fetcher) -> {
            insertRows(fetcher, rowCount, 1);

            assertTrue(fetcher.first(), "expected first row");
            assertAtRow(fetcher, 1);
            assertRowValue(0, TestValue.of(1));
        });
    }

    @ParameterizedTest(name = "[{index}] initialRow = {0}")
    @ValueSource(booleans = { true, false })
    void firstUpdatedRow(boolean initialRow) throws Throwable {
        executeTest(initialRow ? 1 : 0, (stmt, innerFetcher, fetcher) -> {
            if (!initialRow) {
                insertRows(fetcher, 0, 1);
            }
            assertTrue(fetcher.first(), "expected first");
            assertAtRow(fetcher, 1);
            assertRowValue(0, TestValue.of(1));

            TestValue updatedValue = new TestValue(1, "updated");
            fetcher.updateRow(toRowValue(updatedValue));

            assertTrue(fetcher.first(), "expected first row");
            assertAtRow(fetcher, 1);
            assertRowValue(1, updatedValue);
        });
    }

    @Test
    void lastNoRows() throws Throwable {
        executeTest(0, (stmt, innerFetcher, fetcher) -> {
            assertFalse(fetcher.last(), "expected no last row");
            assertAfterLast(fetcher);
            assertRowToNull(0);
        });
    }

    @ParameterizedTest(name = "[{index}] insertCount = {0}")
    @ValueSource(ints = { 0, 1, 2 })
    void lastOneInitial_variableInserted(int insertCount) throws Throwable {
        executeTest(1, (stmt, inner, fetcher) -> {
            int numberOfRows = insertRows(fetcher, 1, insertCount);

            assertTrue(fetcher.last(), "expected last row");
            assertAtRow(fetcher, numberOfRows);
            assertEquals(insertCount == 0, fetcher.isFirst(), "expected first only when insertCount == 0");
            assertTrue(fetcher.isLast(), "expected last");
            assertRowValue(0, TestValue.of(numberOfRows));
        });
    }

    @ParameterizedTest(name = "[{index}] initialCount = {0}, insertCount = {1}")
    @CsvSource({ "0, 0", "1, 0", "2, 0", "1, 1", "2, 1", "2, 2", "1, 2", "0, 2", "0, 1" })
    void nextVariableInitial_variableInserted(int initialCount, int insertCount) throws Throwable {
        executeTest(initialCount, (stmt, innerFetcher, fetcher) -> {
            int numberOfRows = insertRows(fetcher, initialCount, insertCount);

            IntStream.rangeClosed(1, numberOfRows)
                    .forEach(row -> {
                        try {
                            assertTrue(fetcher.next(), "expected next row " + row);
                            assertAtRow(fetcher, row);
                            assertEquals(row == 1, fetcher.isFirst(), "expected first for row = 1, row: " + row);
                            assertEquals(row == numberOfRows, fetcher.isLast(), "expected last for row = " + numberOfRows + ", row: " + row);
                            assertRowValue(row - 1, TestValue.of(row));
                        } catch (SQLException e) {
                            throw new RuntimeException("exception during test", e);
                        }
                    });

            assertFalse(fetcher.next(), "expected no more rows");
            assertAfterLast(fetcher);
            assertRowToNull(numberOfRows);
        });
    }

    @ParameterizedTest(name = "[{index}] initialCount = {0}, insertCount = {1}")
    @CsvSource({ "0, 0", "1, 0", "2, 0", "1, 1", "2, 1", "2, 2", "1, 2", "0, 2", "0, 1" })
    void previousVariableInitial_variableInserted(int initialCount, int insertCount) throws Throwable {
        executeTest(initialCount, (stmt, innerFetcher, fetcher) -> {
            int numberOfRows = insertRows(fetcher, initialCount, insertCount);

            fetcher.afterLast();
            assertAfterLast(fetcher);
            assertRowToNull(0);
            listener.clearRows();

            StreamHelper.reverseClosedRange(1, numberOfRows)
                    .forEach(row -> {
                        try {
                            assertTrue(fetcher.previous(), "expected previous row " + row);
                            assertAtRow(fetcher, row);
                            assertEquals(row == 1, fetcher.isFirst(), "expected first for row = 1, row: " + row);
                            assertEquals(row == numberOfRows, fetcher.isLast(), "expected last for row = " + numberOfRows + ", row: " + row);
                            assertRowValue(numberOfRows - row, TestValue.of(row));
                        } catch (SQLException e) {
                            throw new RuntimeException("exception during test", e);
                        }
                    });

            assertFalse(fetcher.previous(), "expected no more rows");
            assertBeforeFirst(fetcher);
            assertRowToNull(numberOfRows);
        });
    }

    @ParameterizedTest(name = "[{index}] insertCount = {0}")
    @ValueSource(ints = { 0, 1, 2, 4, 8, 10 })
    void absoluteVariousPositions_variableDistributionInitialVsInserted(int insertCount) throws Throwable {
        int initialCount = Math.max(0, 10 - insertCount);
        executeTest(initialCount, (stmt, innerFetcher, fetcher) -> {
            insertRows(fetcher, initialCount, insertCount);
            int row = 0;

            fetcher.absolute(11);
            assertAfterLast(fetcher);
            assertRowToNull(row++);

            fetcher.absolute(0);
            assertBeforeFirst(fetcher);
            assertRowToNull(row++);

            fetcher.absolute(5);
            assertAtRow(fetcher, 5);
            assertRowValue(row++, TestValue.of(5));
            RowValue updated5 = toRowValue(new TestValue(5, "updated"));
            fetcher.updateRow(updated5);
            assertRowValue(row++, updated5);

            fetcher.absolute(-1);
            assertAtRow(fetcher, 10);
            assertRowValue(row++, TestValue.of(10));

            fetcher.absolute(-6);
            assertAtRow(fetcher, 5);
            assertRowValue(row++, new TestValue(5, "updated"));

            fetcher.absolute(-9);
            assertAtRow(fetcher, 2);
            assertRowValue(row++, TestValue.of(2));
            fetcher.deleteRow();
            assertRowValue(row++, deletedRowMarker);

            fetcher.absolute(15);
            assertAfterLast(fetcher);
            assertRowToNull(row++);

            fetcher.absolute(2);
            assertAtRow(fetcher, 2);
            assertRowValue(row++, deletedRowMarker);

            fetcher.absolute(-15);
            assertBeforeFirst(fetcher);
            assertRowToNull(row);
        });
    }

    @ParameterizedTest(name = "[{index}] insertCount = {0}")
    @ValueSource(ints = { 0, 1, 2, 4, 8, 10 })
    void relativeVariousPositions_variableDistributionInitialVsInserted(int insertCount) throws Throwable {
        int initialCount = Math.max(0, 10 - insertCount);
        executeTest(initialCount, (stmt, innerFetcher, fetcher) -> {
            insertRows(fetcher, initialCount, insertCount);
            int row = 0;

            fetcher.relative(11);
            assertAfterLast(fetcher);
            assertRowToNull(row++);

            fetcher.relative(-11);
            assertBeforeFirst(fetcher);
            assertRowToNull(row++);

            fetcher.relative(5);
            assertAtRow(fetcher, 5);
            assertRowValue(row++, TestValue.of(5));
            RowValue updated5 = toRowValue(new TestValue(5, "updated"));
            fetcher.updateRow(updated5);
            assertRowValue(row++, updated5);

            fetcher.relative(5);
            assertAtRow(fetcher, 10);
            assertRowValue(row++, TestValue.of(10));

            fetcher.relative(-5);
            assertAtRow(fetcher, 5);
            assertRowValue(row++, new TestValue(5, "updated"));

            fetcher.relative(-3);
            assertAtRow(fetcher, 2);
            assertRowValue(row++, TestValue.of(2));
            fetcher.deleteRow();
            assertRowValue(row++, deletedRowMarker);

            fetcher.relative(15);
            assertAfterLast(fetcher);
            assertRowToNull(row++);

            fetcher.relative(-9);
            assertAtRow(fetcher, 2);
            assertRowValue(row++, deletedRowMarker);

            fetcher.relative(-15);
            assertBeforeFirst(fetcher);
            assertRowToNull(row);
        });
    }

    @ParameterizedTest(name = "[{index}] insertCount = {0}")
    @ValueSource(ints = { 0, 1 })
    void beforeFirst(int insertCount) throws Throwable {
        int initialCount = Math.max(0, 1 - insertCount);
        executeTest(initialCount, (stmt, innerFetcher, fetcher) -> {
            insertRows(fetcher, initialCount, insertCount);

            fetcher.first();
            assertAtRow(fetcher, 1);
            assertRowValue(0, TestValue.of(1));

            fetcher.beforeFirst();
            assertBeforeFirst(fetcher);
            assertRowToNull(1);
        });
    }

    @ParameterizedTest(name = "[{index}] insertCount = {0}")
    @ValueSource(ints = { 0, 1 })
    void afterLast(int insertCount) throws Throwable {
        int initialCount = Math.max(0, 1 - insertCount);
        executeTest(initialCount, (stmt, innerFetcher, fetcher) -> {
            insertRows(fetcher, initialCount, insertCount);

            fetcher.first();
            assertAtRow(fetcher, 1);
            assertRowValue(0, TestValue.of(1));

            fetcher.afterLast();
            assertAfterLast(fetcher);
            assertRowToNull(1);
        });
    }

    private void executeTest(int numberOfRecords,
            ThrowingTriConsumer<FbStatement, FBServerScrollFetcher, FBUpdatableFetcher> exceptionalConsumer)
            throws Throwable {
        setupTableForTest(numberOfRecords);
        FbTransaction tr = getTransaction();
        try (FbStatement stmt = db.createStatement(tr)) {
            stmt.prepare("select id, colval from scrolltest order by id");
            stmt.setCursorFlag(CursorFlag.CURSOR_TYPE_SCROLLABLE);
            stmt.execute(RowValue.EMPTY_ROW_VALUE);
            FBServerScrollFetcher fetcher = new FBServerScrollFetcher(1, 0, stmt, () -> db, null);
            FBUpdatableFetcher updatableFetcher = new FBUpdatableFetcher(fetcher, listener, deletedRowMarker);
            assertBeforeFirst(updatableFetcher);

            exceptionalConsumer.accept(stmt, fetcher, updatableFetcher);
        } finally {
            tr.commit();
        }
    }

    private int insertRows(FBFetcher fetcher, int previousValue, int count) throws SQLException {
        int endValue = previousValue + count;
        for (int idValue = previousValue + 1; idValue <= endValue; idValue++) {
            fetcher.insertRow(toRowValue(TestValue.of(idValue)));
        }
        listener.clearRows();
        return endValue;
    }

    private void assertRowValue(int listenerRow, TestValue expectedValue) {
        listener.assertRow(listenerRow,
                rowValue -> assertEquals(expectedValue, extractTestValue(rowValue), "unexpected row"));
    }

    private void assertRowValue(int listenerRow, RowValue expectedValue) {
        listener.assertRow(listenerRow,
                rowValue -> assertEquals(expectedValue, rowValue, "unexpected row"));
    }

    private void assertRowToNull(int listenerRow) {
        listener.assertRow(listenerRow, rowValue -> assertNull(rowValue, "expected row change to null"));
    }

    private FbTransaction getTransaction() throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }

    private void setupTableForTest(int numberOfRecords) throws SQLException {
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
                    "    insert into scrolltest (id, colval) values (:id, :id);\n" +
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

    private TestValue extractTestValue(RowValue rowValue) {
        DatatypeCoder datatypeCoder = db.getDatatypeCoder();
        byte[] fieldId = rowValue.getFieldData(0);
        Integer id = fieldId != null ? datatypeCoder.decodeInt(fieldId) : null;
        byte[] fieldColval = rowValue.getFieldData(1);
        String colval = fieldColval != null ? datatypeCoder.decodeString(fieldColval) : null;
        return new TestValue(id, colval);
    }

    private RowValue toRowValue(TestValue testValue) {
        DatatypeCoder datatypeCoder = db.getDatatypeCoder();
        byte[] fieldId = testValue.id != null ? datatypeCoder.encodeInt(testValue.id) : null;
        byte[] fieldColval = testValue.colval != null ? datatypeCoder.encodeString(testValue.colval) : null;
        return RowValue.of(fieldId, fieldColval);
    }

    private static class TestValue {
        final Integer id;
        final String colval;

        TestValue(Integer id, String colval) {
            this.id = id;
            this.colval = colval;
        }

        static TestValue of(int id) {
            return new TestValue(id, String.valueOf(id));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestValue testValue = (TestValue) o;
            return Objects.equals(id, testValue.id) && Objects.equals(colval, testValue.colval);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, colval);
        }

        @Override
        public String toString() {
            return "TestValue{" +
                    "id=" + id +
                    ", colval='" + colval + '\'' +
                    '}';
        }
    }

}