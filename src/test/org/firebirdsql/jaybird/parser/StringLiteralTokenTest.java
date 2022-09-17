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