// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StringLiteralTokenTest {

    @ParameterizedTest
    @MethodSource("validStringLiteralsProvider")
    void valueForValidStringLiterals(String input, String expectedValue) {
        StringLiteralToken token = new StringLiteralToken(0, input);

        assertThat(token.value()).isEqualTo(expectedValue);
    }

    static Stream<Arguments> validStringLiteralsProvider() {
        return Stream.of(
                arguments("''", ""),
                arguments("'ab'", "ab"),
                arguments("'a''b'", "a'b"),
                arguments("q'{xyz}'", "xyz"),
                arguments("Q'!abc'de!xyz!'", "abc'de!xyz"),
                arguments("x''", ""),
                arguments("x'ae'", "ae")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // too short
            "", "'", "Q'", "q''", "Q'{'", "x", "X'",
            // wrong end
            "'a\"", "'ab", "q'{x}\"", "q'{x}", "x'ae\"", "x'ae",
            // wrong start
            "wrong start"
    })
    void invalidLiteralText_throwsIllegalStateException(String input) {
        StringLiteralToken token = new StringLiteralToken(0, input);

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(token::value);
    }

}