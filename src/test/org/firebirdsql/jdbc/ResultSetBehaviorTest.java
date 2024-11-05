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

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link ResultSetBehavior}.
 */
class ResultSetBehaviorTest {

    @ParameterizedTest
    @MethodSource("testCases")
    void testType(int type, int concurrency, int holdability, int expectedType) throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedType, behavior.type(), "type");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testIsForwardOnly(int type, int concurrency, int holdability, int expectedType) throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedType == TYPE_FORWARD_ONLY, behavior.isForwardOnly(), "isForwardOnly");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testIsScrollable(int type, int concurrency, int holdability, int expectedType) throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedType != TYPE_FORWARD_ONLY, behavior.isScrollable(), "isScrollable");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testIsInsensitive(int type, int concurrency, int holdability, int expectedType) throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedType != TYPE_SCROLL_SENSITIVE, behavior.isInsensitive(), "isInsensitive");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testIsSensitive(int type, int concurrency, int holdability, int expectedType) throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedType == TYPE_SCROLL_SENSITIVE, behavior.isSensitive(), "isSensitive");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testConcurrency(int type, int concurrency, int holdability, int ignored1, int expectedConcurrency)
            throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedConcurrency, behavior.concurrency(), "concurrency");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testIsReadOnly(int type, int concurrency, int holdability, int ignored1, int expectedConcurrency)
            throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedConcurrency == CONCUR_READ_ONLY, behavior.isReadOnly(), "isReadOnly");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testIsUpdatable(int type, int concurrency, int holdability, int ignored1, int expectedConcurrency)
            throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedConcurrency == CONCUR_UPDATABLE, behavior.isUpdatable(), "isUpdatable");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testHoldability(int type, int concurrency, int holdability, int ignored1, int ignored2,
            int expectedHoldability) throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedHoldability, behavior.holdability(), "holdability");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testIsCloseCursorsAtCommit(int type, int concurrency, int holdability, int ignored1, int ignored2,
            int expectedHoldability) throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedHoldability == CLOSE_CURSORS_AT_COMMIT, behavior.isCloseCursorsAtCommit(),
                "isCloseCursorsAtCommit");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testIsHoldCursorsOverCommit(int type, int concurrency, int holdability, int ignored1, int ignored2,
            int expectedHoldability) throws SQLException {
        var behavior = ResultSetBehavior.of(type, concurrency, holdability);
        assertEquals(expectedHoldability == HOLD_CURSORS_OVER_COMMIT, behavior.isHoldCursorsOverCommit(),
                "isHoldCursorsOverCommit");
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testWarningOnUpgradeOrDowngrade(int type, int concurrency, int holdability, int ignored1, int ignored2,
            int ignored3, Integer warningCode) throws SQLException {
        var warningCaptor = new WarningCaptor();
        ResultSetBehavior.of(type, concurrency, holdability, warningCaptor);
        if (warningCode != null) {
            assertThat(warningCaptor.warning, allOf(
                    notNullValue(),
                    errorCodeEquals(warningCode),
                    fbMessageStartsWith(warningCode)));
        } else {
            assertNull(warningCaptor.warning, "expected no warning");
        }
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testWithReadOnly(int type, int concurrency, int holdability) throws SQLException {
        var initialBehaviour = ResultSetBehavior.of(type, concurrency, holdability);
        ResultSetBehavior readOnlyBehaviour = initialBehaviour.withReadOnly();

        assertEquals(initialBehaviour.type(), readOnlyBehaviour.type(), "type");
        assertEquals(CONCUR_READ_ONLY, readOnlyBehaviour.concurrency(), "concurrency");
        assertEquals(initialBehaviour.holdability(), readOnlyBehaviour.holdability(), "holdability");
    }

    static Stream<Arguments> testCases() {
        return Stream.of(
                testCaseBasic(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY, CLOSE_CURSORS_AT_COMMIT),
                testCaseBasic(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY, HOLD_CURSORS_OVER_COMMIT),
                testCaseBasic(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE, CLOSE_CURSORS_AT_COMMIT),
                testCaseBasic(TYPE_FORWARD_ONLY, CONCUR_UPDATABLE, HOLD_CURSORS_OVER_COMMIT),
                testCaseBasic(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY, CLOSE_CURSORS_AT_COMMIT),
                testCaseBasic(TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY, HOLD_CURSORS_OVER_COMMIT),
                testCaseBasic(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE, CLOSE_CURSORS_AT_COMMIT),
                testCaseBasic(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE, HOLD_CURSORS_OVER_COMMIT),
                testCaseTypeChange(TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY, CLOSE_CURSORS_AT_COMMIT,
                        TYPE_SCROLL_INSENSITIVE, JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive),
                testCaseTypeChange(TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY, HOLD_CURSORS_OVER_COMMIT,
                        TYPE_SCROLL_INSENSITIVE, JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive),
                testCaseTypeChange(TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE, CLOSE_CURSORS_AT_COMMIT,
                        TYPE_SCROLL_INSENSITIVE, JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive),
                testCaseTypeChange(TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE, HOLD_CURSORS_OVER_COMMIT,
                        TYPE_SCROLL_INSENSITIVE, JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive));
    }

    @Test
    void defaultResultSetBehavior() {
        var defaultBehavior = ResultSetBehavior.of();
        assertEquals(TYPE_FORWARD_ONLY, defaultBehavior.type(), "type");
        assertEquals(CONCUR_READ_ONLY, defaultBehavior.concurrency(), "concurrency");
        assertEquals(CLOSE_CURSORS_AT_COMMIT, defaultBehavior.holdability(), "holdability");
    }

    private static Arguments testCaseBasic(int type, int concurrency, int holdability) {
        return testCaseTypeChange(type, concurrency, holdability, type, null);
    }

    private static Arguments testCaseTypeChange(int type, int concurrency, int holdability, int expectedType,
            Integer warningCode) {
        return Arguments.of(type, concurrency, holdability, expectedType, concurrency, holdability, warningCode);
    }

    private static class WarningCaptor implements Consumer<SQLWarning> {

        SQLWarning warning;

        @Override
        public void accept(SQLWarning warning) {
            this.warning = warning;
        }

    }

}