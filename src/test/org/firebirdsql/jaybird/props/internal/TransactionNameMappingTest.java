// SPDX-FileCopyrightText: Copyright 2021 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.props.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class TransactionNameMappingTest {

    @ParameterizedTest
    @MethodSource
    void toIsolationLevel(String isolationLevelName, int expectedIsolationLevel) {
        assertThat(TransactionNameMapping.toIsolationLevel(isolationLevelName)).isEqualTo(expectedIsolationLevel);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> toIsolationLevel() {
        return Stream.of(
                Arguments.of("TRANSACTION_NONE", Connection.TRANSACTION_NONE),
                Arguments.of("0", Connection.TRANSACTION_NONE),
                Arguments.of("TRANSACTION_READ_UNCOMMITTED", Connection.TRANSACTION_READ_UNCOMMITTED),
                Arguments.of("1", Connection.TRANSACTION_READ_UNCOMMITTED),
                Arguments.of("TRANSACTION_READ_COMMITTED", Connection.TRANSACTION_READ_COMMITTED),
                Arguments.of("2", Connection.TRANSACTION_READ_COMMITTED),
                Arguments.of("TRANSACTION_REPEATABLE_READ", Connection.TRANSACTION_REPEATABLE_READ),
                Arguments.of("4", Connection.TRANSACTION_REPEATABLE_READ),
                Arguments.of("TRANSACTION_SERIALIZABLE", Connection.TRANSACTION_SERIALIZABLE),
                Arguments.of("8", Connection.TRANSACTION_SERIALIZABLE)
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toIsolationLevel_unknownName_throwsIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> TransactionNameMapping.toIsolationLevel("unknown"));
    }

    @ParameterizedTest
    @MethodSource
    void toIsolationLevelName(int isolationLevel, String expectedIsolationLevelName) {
        assertThat(TransactionNameMapping.toIsolationLevelName(isolationLevel)).isEqualTo(expectedIsolationLevelName);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> toIsolationLevelName() {
        return Stream.of(
                Arguments.of(Connection.TRANSACTION_NONE, "TRANSACTION_NONE"),
                Arguments.of(Connection.TRANSACTION_READ_UNCOMMITTED, "TRANSACTION_READ_UNCOMMITTED"),
                Arguments.of(Connection.TRANSACTION_READ_COMMITTED, "TRANSACTION_READ_COMMITTED"),
                Arguments.of(Connection.TRANSACTION_REPEATABLE_READ, "TRANSACTION_REPEATABLE_READ"),
                Arguments.of(Connection.TRANSACTION_SERIALIZABLE, "TRANSACTION_SERIALIZABLE")
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toIsolationLevelName_unknownLevel_throwsIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> TransactionNameMapping.toIsolationLevelName(132));
    }

}