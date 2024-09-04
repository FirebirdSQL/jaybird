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
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.ResultSet;
import java.sql.SQLNonTransientException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageContains;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link FetchConfig}.
 */
class FetchConfigTest {

    private static final FetchConfig DEFAULT_INSTANCE = new FetchConfig(ResultSetBehavior.of());

    @Test
    void defaultValues() {
        assertEquals(0, DEFAULT_INSTANCE.fetchSize(), "fetchSize");
        assertEquals(0, DEFAULT_INSTANCE.maxRows(), "maxRows");
        assertEquals(ResultSet.FETCH_FORWARD, DEFAULT_INSTANCE.direction(), "direction");
    }

    @Test
    void resultSetBehaviour_null_throwsException() {
        assertThrows(NullPointerException.class, () -> new FetchConfig(null));
    }

    @Test
    void withFetchSize_positiveValue() throws Exception {
        FetchConfig withFetchSize = DEFAULT_INSTANCE.withFetchSize(25);
        assertEquals(25, withFetchSize.fetchSize(), "fetchSize");
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, -2, Integer.MIN_VALUE })
    void withFetchSize_negativeValues_throwsSQLNonTransient(int fetchSize) {
        var e = assertThrows(SQLNonTransientException.class, () -> DEFAULT_INSTANCE.withFetchSize(fetchSize));
        assertThat(e, message(allOf(containsString("fetchSize"), containsString(String.valueOf(fetchSize)))));
    }

    @Test
    void withMaxRows_positiveValue() throws Exception {
        FetchConfig withMaxRows = DEFAULT_INSTANCE.withMaxRows(35);
        assertEquals(35, withMaxRows.maxRows(), "maxRows");
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, -2, Integer.MIN_VALUE })
    void withMaxRows_negativeValues_throwsSQLNonTransient(int maxRows) {
        var e = assertThrows(SQLNonTransientException.class, () -> DEFAULT_INSTANCE.withMaxRows(maxRows));
        assertThat(e, message(allOf(containsString("maxRows"), containsString(String.valueOf(maxRows)))));
    }

    @ParameterizedTest
    @ValueSource(ints = { ResultSet.FETCH_FORWARD, ResultSet.FETCH_REVERSE, ResultSet.FETCH_UNKNOWN })
    void withDirection_acceptsDirectionValues(int direction) throws Exception {
        FetchConfig withDirection = DEFAULT_INSTANCE.withDirection(direction);
        assertEquals(direction, withDirection.direction(), "direction");
    }

    @ParameterizedTest
    @ValueSource(ints = { 9999 /* FETCH_FORWARD - 1 */, 1003 /* FETCH_UNKNOWN + 1 */})
    void withDirection_nonDirectionValues_throwsSQLNonTransient(int direction) {
        var e = assertThrows(SQLNonTransientException.class, () -> DEFAULT_INSTANCE.withDirection(direction));
        assertThat(e, fbMessageContains(JaybirdErrorCodes.jb_invalidFetchDirection, String.valueOf(direction)));
    }

    @Test
    void fetchSizeOr_useDefaultValueIfZero() throws Exception {
        assertEquals(100, DEFAULT_INSTANCE.withFetchSize(0).fetchSizeOr(100));
    }

    @Test
    void fetchSizeOr_useActualValueIfNonZero() throws Exception {
        assertEquals(50, DEFAULT_INSTANCE.withFetchSize(50).fetchSizeOr(100));
    }

    @Test
    void withReadOnly_alreadyReadOnly() {
        FetchConfig withReadOnly = DEFAULT_INSTANCE.withReadOnly();
        assertTrue(withReadOnly.resultSetBehavior().isReadOnly(), "resultSetBehaviour.readOnly");
    }
    
    @Test
    void withReadOnly_changesResultSetBehaviourToReadOnly() throws Exception {
        var initial = new FetchConfig(ResultSetBehavior.of(
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE, ResultSet.CLOSE_CURSORS_AT_COMMIT));
        assertFalse(initial.resultSetBehavior().isReadOnly(), "initial.resultSetBehaviour.readOnly");
        FetchConfig withReadOnly = initial.withReadOnly();
        assertTrue(withReadOnly.resultSetBehavior().isReadOnly(), "withReadOnly.resultSetBehaviour.readOnly");
    }

}