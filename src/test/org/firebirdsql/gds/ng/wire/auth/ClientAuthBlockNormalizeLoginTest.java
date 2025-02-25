// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.auth;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientAuthBlockNormalizeLoginTest {

    static Stream<Arguments> testData() {
        return Stream.of(
                testCase("sysdba", "SYSDBA"),
                testCase("s", "S"),
                testCase("\"CaseSensitive\"", "CaseSensitive"),
                testCase("\"s\"", "s"),
                testCase("\"With\"\"EscapedQuote\"", "With\"EscapedQuote"),
                // NOTE: This is the behaviour as also done by Firebird before 3.0.10 / 4.0.2
                testCase("\"Invalid\"Escape\"", "Invalid"),
                testCase("\"DanglingInvalidEscape\"\"", "DanglingInvalidEscape"),
                testCase("\"EscapedQuoteAtEnd\"\"\"", "EscapedQuoteAtEnd\""),
                testCase("\"StartNoEndQuote", "\"STARTNOENDQUOTE"),
                testCase("\"\"", "\"\""),
                testCase("", ""),
                testCase(null, null));
    }

    @ParameterizedTest(name = "{0} => {1}")
    @MethodSource("testData")
    void testNormalizeLogin(String login, String expectedNormalizedLogin) {
        assertEquals(expectedNormalizedLogin, ClientAuthBlock.normalizeLogin(login));
    }

    private static Arguments testCase(String login, String normalizedLogin) {
        return Arguments.of(login, normalizedLogin);
    }

}
