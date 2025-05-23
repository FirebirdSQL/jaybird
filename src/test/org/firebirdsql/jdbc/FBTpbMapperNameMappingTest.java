// SPDX-FileCopyrightText: Copyright 2014-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the mapping between Connection.TRANSACTION_* integer values and names in {@link org.firebirdsql.jdbc.FBTpbMapper}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class FBTpbMapperNameMappingTest {

    /**
     * Tests that the isolation level is matched to the right isolation name.
     */
    @ParameterizedTest(name = "{index}: {0} => {1}")
    @MethodSource("testData")
    void testGetTransactionIsolationName(
            Integer connectionTransactionValue, String fbTpbMapperTransactionValue) {
        assumeThat("Ignoring test as connectionTransactionValue is null", connectionTransactionValue, notNullValue());
        if (fbTpbMapperTransactionValue == null) {
            assertThrows(IllegalArgumentException.class,
                    () -> FBTpbMapper.getTransactionIsolationName(connectionTransactionValue));
        } else {
            assertEquals(fbTpbMapperTransactionValue,
                    FBTpbMapper.getTransactionIsolationName(connectionTransactionValue),
                    () -> format("Unexpected transactionIsolation name for level %d", connectionTransactionValue));
        }
    }

    /**
     * Tests that the isolation name is matched to the right isolation level.
     */
    @ParameterizedTest(name = "{index}: {1} => {0}")
    @MethodSource("testData")
    void testGetTransactionIsolationLevel(
            Integer connectionTransactionValue, String fbTpbMapperTransactionValue) {
        assumeThat("Ignoring test as fbTpbMapperTransactionValue is null", fbTpbMapperTransactionValue, notNullValue());
        if (connectionTransactionValue == null) {
            //noinspection ResultOfMethodCallIgnored
            assertThrows(IllegalArgumentException.class,
                    () -> FBTpbMapper.getTransactionIsolationLevel(fbTpbMapperTransactionValue));
        } else {
            assertEquals(connectionTransactionValue,
                    (Integer) FBTpbMapper.getTransactionIsolationLevel(fbTpbMapperTransactionValue),
                    () -> format("Unexpected transactionIsolation level for name %s", fbTpbMapperTransactionValue));
        }
    }

    static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of(Connection.TRANSACTION_NONE, FBTpbMapper.TRANSACTION_NONE),
                Arguments.of(Connection.TRANSACTION_READ_UNCOMMITTED, FBTpbMapper.TRANSACTION_READ_UNCOMMITTED),
                Arguments.of(Connection.TRANSACTION_READ_COMMITTED, FBTpbMapper.TRANSACTION_READ_COMMITTED),
                Arguments.of(Connection.TRANSACTION_REPEATABLE_READ, FBTpbMapper.TRANSACTION_REPEATABLE_READ),
                Arguments.of(Connection.TRANSACTION_SERIALIZABLE, FBTpbMapper.TRANSACTION_SERIALIZABLE),
                Arguments.of(-1, null),
                Arguments.of(null, "ABC"));
    }
}
