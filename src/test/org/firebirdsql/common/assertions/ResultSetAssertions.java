// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.assertions;

import org.junit.jupiter.api.function.ThrowingSupplier;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Various assertions for result set.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class ResultSetAssertions {

    private ResultSetAssertions() {
        // no instances
    }

    /**
     * Asserts that {@code rs} has a next row by checking if {@link ResultSet#next()} returned {@code true}.
     * <p>
     * Equivalent to using {@link #assertNextRow(ResultSet, String)} with message {@code "Expected a row"}.
     * </p>
     *
     * @param rs
     *         result set
     * @see #assertNextRow(ResultSet, String)
     */
    public static void assertNextRow(ResultSet rs) {
        assertNextRow(rs, "Expected a row");
    }

    /**
     * Asserts that {@code rs} has a next row by checking if {@link ResultSet#next()} returned {@code true}.
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#next()} returned {@code false}
     */
    public static void assertNextRow(ResultSet rs, String message) {
        assertTrue(assertDoesNotThrow(rs::next, "No exception expected for ResultSet.next()"), message);
    }

    /**
     * Asserts that {@code rs} has no next row ({@link ResultSet#next()} returned {@code false}).
     *
     * @param rs
     *         result set
     * @see #assertNoNextRow(ResultSet, String)
     */
    public static void assertNoNextRow(ResultSet rs) {
        assertNoNextRow(rs, "Expected no more rows");
    }

    /**
     * Asserts that {@code rs} has no next row ({@link ResultSet#next()} returned {@code false}).
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#next()} returned {@code true}
     */
    public static void assertNoNextRow(ResultSet rs, String message) {
        assertFalse(assertNoException(rs::next, "ResultSet.next()"), message);
    }

    //

    /**
     * Asserts that {@code rs} has a previous row by checking if {@link ResultSet#previous()} returned {@code true}.
     * <p>
     * Equivalent to using {@link #assertPreviousRow(ResultSet, String)} with message {@code "Expected a previous row"}.
     * </p>
     *
     * @param rs
     *         result set
     * @see #assertPreviousRow(ResultSet, String)
     */
    public static void assertPreviousRow(ResultSet rs) {
        assertPreviousRow(rs, "Expected a previous row");
    }

    /**
     * Asserts that {@code rs} has a next row by checking if {@link ResultSet#next()} returned {@code true}.
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#previous()} returned {@code false}
     */
    public static void assertPreviousRow(ResultSet rs, String message) {
        assertTrue(assertDoesNotThrow(rs::previous, "No exception expected for ResultSet.previous()"), message);
    }

    /**
     * Asserts that {@code rs} has no previous row ({@link ResultSet#previous()} returned {@code false}).
     *
     * @param rs
     *         result set
     * @see #assertNoPreviousRow(ResultSet, String)
     */
    public static void assertNoPreviousRow(ResultSet rs) {
        assertNoPreviousRow(rs, "Expected no previous row");
    }

    /**
     * Asserts that {@code rs} has no previous row ({@link ResultSet#previous()} returned {@code false}).
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#previous()} returned {@code true}
     */
    public static void assertNoPreviousRow(ResultSet rs, String message) {
        assertFalse(assertNoException(rs::previous, "ResultSet.previous()"), message);
    }

    //

    /**
     * Asserts that {@code rs} has a row at absolute position {@code absoluteRow} ({@link ResultSet#absolute(int)}
     * returned {@code true}).
     *
     * @param rs
     *         result set
     * @param absoluteRow
     *         absolute row position
     */
    public static void assertAbsoluteRow(ResultSet rs, int absoluteRow) {
        assertAbsoluteRow(rs, absoluteRow, "Expected row at absolute " + absoluteRow);
    }

    /**
     * Asserts that {@code rs} has a row at absolute position {@code absoluteRow} ({@link ResultSet#absolute(int)}
     * returned {@code true}).
     *
     * @param rs
     *         result set
     * @param absoluteRow
     *         absolute row position
     * @param message
     *         message to use for the assertion if {@link ResultSet#absolute(int)} returned {@code false}
     */
    public static void assertAbsoluteRow(ResultSet rs, int absoluteRow, String message) {
        assertTrue(assertNoException(() -> rs.absolute(absoluteRow), "ResultSet.absolute(int)"), message);
    }

    /**
     * Asserts that {@code rs} has no row at absolute position {@code absoluteRow} ({@link ResultSet#absolute(int)}
     * returned {@code false}).
     *
     * @param rs
     *         result set
     * @param absoluteRow
     *         absolute row position
     */
    public static void assertNpAbsoluteRow(ResultSet rs, int absoluteRow) {
        assertNoAbsoluteRow(rs, absoluteRow, "Expected no row at absolute " + absoluteRow);
    }

    /**
     * Asserts that {@code rs} has no row at absolute position {@code absoluteRow} ({@link ResultSet#absolute(int)}
     * returned {@code false}).
     *
     * @param rs
     *         result set
     * @param absoluteRow
     *         absolute row position
     * @param message
     *         message to use for the assertion if {@link ResultSet#absolute(int)} returned {@code true}
     */
    public static void assertNoAbsoluteRow(ResultSet rs, int absoluteRow, String message) {
        assertFalse(assertNoException(() -> rs.absolute(absoluteRow), "ResultSet.absolute(int)"), message);
    }

    /**
     * Asserts that {@code rs} is open ({@link ResultSet#isClosed()} returned {@code false})
     *
     * @param rs
     *         result set
     * @see #assertResultSetOpen(ResultSet, String)
     */
    public static void assertResultSetOpen(ResultSet rs) {
        assertResultSetOpen(rs, "Expected open result set, was closed");
    }

    /**
     * Asserts that {@code rs} is open ({@link ResultSet#isClosed()} returned {@code false})
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#isClosed()} returned {@code true}
     */
    public static void assertResultSetOpen(ResultSet rs, String message) {
        assertFalse(assertNoException(rs::isClosed, "ResultSet.isClosed()"), message);
    }

    /**
     * Asserts that {@code rs} is closed ({@link ResultSet#isClosed()} returned {@code true})
     *
     * @param rs
     *         result set
     * @see #assertResultSetClosed(ResultSet, String)
     */
    public static void assertResultSetClosed(ResultSet rs) {
        assertResultSetClosed(rs, "Expected closed result set, was open");
    }

    /**
     * Asserts that {@code rs} is closed ({@link ResultSet#isClosed()} returned {@code true})
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#isClosed()} returned {@code false}
     */
    public static void assertResultSetClosed(ResultSet rs, String message) {
        assertTrue(assertNoException(rs::isClosed, "ResultSet.isClosed()"), message);
    }

    /**
     * Asserts that {@code rs} is before first ({@link ResultSet#isBeforeFirst()} returns {@code true}).
     *
     * @param rs
     *         result set
     * @see #assertBeforeFirst(ResultSet, String)
     */
    public static void assertBeforeFirst(ResultSet rs) {
        assertBeforeFirst(rs, "Expected ResultSet.isBeforeFirst == true");
    }

    /**
     * Asserts that {@code rs} is before first ({@link ResultSet#isBeforeFirst()} returns {@code true}).
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#isBeforeFirst()} returned {@code false}
     */
    public static void assertBeforeFirst(ResultSet rs, String message) {
        assertTrue(assertNoException(rs::isBeforeFirst, "ResultSet.isBeforeFirst()"), message);
    }

    /**
     * Asserts that {@code rs} is not before first ({@link ResultSet#isBeforeFirst()} returns {@code false}).
     *
     * @param rs
     *         result set
     * @see #assertNotBeforeFirst(ResultSet, String)
     */
    public static void assertNotBeforeFirst(ResultSet rs) {
        assertNotBeforeFirst(rs, "Expected ResultSet.isBeforeFirst == false");
    }

    /**
     * Asserts that {@code rs} is not before first ({@link ResultSet#isBeforeFirst()} returns {@code false}).
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#isBeforeFirst()} returned {@code true}
     */
    public static void assertNotBeforeFirst(ResultSet rs, String message) {
        assertFalse(assertNoException(rs::isBeforeFirst, "ResultSet.isBeforeFirst()"), message);
    }

    /**
     * Asserts that {@code rs} is after last ({@link ResultSet#isAfterLast()} returns {@code true}).
     *
     * @param rs
     *         result set
     * @see #assertAfterLast(ResultSet, String)
     */
    public static void assertAfterLast(ResultSet rs) {
        assertAfterLast(rs, "Expected ResultSet.isAfterLast == true");
    }

    /**
     * Asserts that {@code rs} is after last ({@link ResultSet#isAfterLast()} returns {@code true}).
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#isAfterLast()} returned {@code false}
     */
    public static void assertAfterLast(ResultSet rs, String message) {
        assertTrue(assertNoException(rs::isAfterLast, "ResultSet.isAfterLast()"), message);
    }

    /**
     * Asserts that {@code rs} is not after last ({@link ResultSet#isAfterLast()} returns {@code false}).
     *
     * @param rs
     *         result set
     * @see #assertNotAfterLast(ResultSet, String)
     */
    public static void assertNotAfterLast(ResultSet rs) {
        assertNotAfterLast(rs, "Expected ResultSet.isAfterLast == false");
    }

    /**
     * Asserts that {@code rs} is not after last ({@link ResultSet#isAfterLast()} returns {@code false}).
     *
     * @param rs
     *         result set
     * @param message
     *         message to use for the assertion if {@link ResultSet#isAfterLast()} returned {@code true}
     */
    public static void assertNotAfterLast(ResultSet rs, String message) {
        assertFalse(assertNoException(rs::isAfterLast, "ResultSet.isAfterLast()"), message);
    }

    /**
     * Asserts that the current row of the result set matches in length and values.
     *
     * @param rs
     *         result set
     * @param expectedValues
     *         expected values
     * @see #assertRowEquals(String, ResultSet, List)
     */
    public static void assertRowEquals(ResultSet rs, Object... expectedValues) {
        assertRowEquals(rs, Arrays.asList(expectedValues));
    }

    /**
     * Asserts that the current row of the result set matches in length and values.
     *
     * @param message
     *         message to use for assertion failures
     * @param rs
     *         result set
     * @param expectedValues
     *         expected values
     * @see #assertRowEquals(String, ResultSet, List)
     */
    public static void assertRowEquals(String message, ResultSet rs, Object... expectedValues) {
        assertRowEquals(message, rs, Arrays.asList(expectedValues));
    }

    /**
     * Asserts that the current row of the result set matches in length and values.
     *
     * @param rs
     *         result set
     * @param expectedValues
     *         expected values
     * @see #assertRowEquals(String, ResultSet, List)
     */
    public static void assertRowEquals(ResultSet rs, List<Object> expectedValues) {
        assertRowEquals("Row mismatch", rs, expectedValues);
    }

    /**
     * Asserts that the current row of the result set matches in length and values.
     * <p>
     * For each non-null value in {@code expectedValue}, its class is used to call
     * {@link ResultSet#getObject(int, Class)}, for {@code byte[]}, {@link ResultSet#getBytes(int)}. For {@code null}
     * values, {@link ResultSet#getObject(int)} is called. The 0-based index of {@code expectedValues} is transformed to
     * the 1-based index of JDBC. Assertions report the 1-based index.
     * </p>
     * <p>
     * Assertion stops at the first mismatch.
     * </p>
     *
     * @param message
     *         message to use for assertion failures
     * @param rs
     *         result set
     * @param expectedValues
     *         expected values
     */
    public static void assertRowEquals(String message, ResultSet rs, List<Object> expectedValues) {
        ResultSetMetaData rsmd = assertNoException(rs::getMetaData, "ResultSet.getMetaData()");
        assertEquals(expectedValues.size(), assertNoException(rsmd::getColumnCount, "ResultSet.getColumnCount()"),
                message + ": column count differs");
        for (int idx = 0; idx < expectedValues.size(); idx++) {
            Object expectedValue = expectedValues.get(idx);
            int colIdx = idx + 1;
            if (expectedValue == null) {
                assertNull(assertNoException(() -> rs.getObject(colIdx), "ResultSet.getObject(int)"),
                        message + " at column " + colIdx + " (1-based)");
            } else if (expectedValue instanceof byte[] expectedBytes) {
                assertArrayEquals(expectedBytes,
                        assertNoException(() -> rs.getBytes(colIdx), "ResultSet.getBytes(int)"),
                        message + " at column " + colIdx + " (1-based)");
            } else {
                assertEquals(expectedValue,
                        assertNoException(() -> rs.getObject(colIdx, expectedValue.getClass()), "ResultSet.getObject(int, Class)"),
                        message + " at column " + colIdx + " (1-based)");
            }
        }
    }

    public static void assertWasNull(ResultSet rs) {
        assertWasNull(rs, "expected ResultSet.wasNull == true");
    }

    public static void assertWasNull(ResultSet rs, String message) {
        assertTrue(assertNoException(rs::wasNull, "ResultSet.wasNull()"), message);
    }

    public static void assertWasNotNull(ResultSet rs) {
        assertWasNotNull(rs, "expected ResultSet.wasNull == false");
    }

    public static void assertWasNotNull(ResultSet rs, String message) {
        assertFalse(assertNoException(rs::wasNull, "ResultSet.wasNull()"), message);
    }

    public static void assertRowUpdated(ResultSet rs) {
        assertRowUpdated(rs, "expected ResultSet.rowUpdated == true");
    }

    public static void assertRowUpdated(ResultSet rs, String message) {
        assertTrue(assertNoException(rs::rowUpdated, "ResultSet.rowUpdated()"), message);
    }

    public static void assertRowNotUpdated(ResultSet rs) {
        assertRowNotUpdated(rs, "expected ResultSet.rowUpdated == false");
    }

    public static void assertRowNotUpdated(ResultSet rs, String message) {
        assertFalse(assertNoException(rs::rowUpdated, "ResultSet.rowUpdated()"), message);
    }

    public static void assertRowInserted(ResultSet rs) {
        assertRowInserted(rs, "expected ResultSet.rowInserted == true");
    }

    public static void assertRowInserted(ResultSet rs, String message) {
        assertTrue(assertNoException(rs::rowInserted, "ResultSet.rowInserted()"), message);
    }

    public static void assertRowNotInserted(ResultSet rs) {
        assertRowNotInserted(rs, "expected ResultSet.rowInserted == false");
    }

    public static void assertRowNotInserted(ResultSet rs, String message) {
        assertFalse(assertNoException(rs::rowInserted, "ResultSet.rowInserted()"), message);
    }

    public static void assertRowDeleted(ResultSet rs) {
        assertRowDeleted(rs, "expected ResultSet.rowDeleted == true");
    }

    public static void assertRowDeleted(ResultSet rs, String message) {
        assertTrue(assertNoException(rs::rowDeleted, "ResultSet.rowDeleted()"), message);
    }

    public static void assertRowNotDeleted(ResultSet rs) {
        assertRowNotDeleted(rs, "expected ResultSet.rowDeleted == false");
    }

    public static void assertRowNotDeleted(ResultSet rs, String message) {
        assertFalse(assertNoException(rs::rowDeleted, "ResultSet.rowDeleted()"), message);
    }

    private static <T> T assertNoException(ThrowingSupplier<T> supplier, String methodName) {
        return assertDoesNotThrow(supplier, "No exception expected for " + methodName);
    }

}
