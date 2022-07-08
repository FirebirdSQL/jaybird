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
