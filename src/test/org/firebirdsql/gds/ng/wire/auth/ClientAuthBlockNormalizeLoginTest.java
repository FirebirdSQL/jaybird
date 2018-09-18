/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ClientAuthBlockNormalizeLoginTest {

    private final String login;
    private final String expectedNormalizedLogin;

    public ClientAuthBlockNormalizeLoginTest(String login, String expectedNormalizedLogin) {
        this.login = login;
        this.expectedNormalizedLogin = expectedNormalizedLogin;
    }

    @Parameterized.Parameters(name = "{0} => {1}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(
                testCase("sysdba", "SYSDBA"),
                testCase("s", "S"),
                testCase("\"CaseSensitive\"", "CaseSensitive"),
                testCase("\"s\"", "s"),
                testCase("\"With\"\"EscapedQuote\"", "With\"EscapedQuote"),
                testCase("\"Invalid\"Escape\"", "Invalid"),
                testCase("\"DanglingInvalidEscape\"\"", "DanglingInvalidEscape"),
                testCase("\"EscapedQuoteAtEnd\"\"\"", "EscapedQuoteAtEnd\""),
                testCase("\"StartNoEndQuote", "\"STARTNOENDQUOTE"),
                testCase("\"\"", "\"\""),
                testCase("", ""),
                testCase(null, null));
    }

    @Test
    public void testNormalizeLogin() {
        assertEquals(expectedNormalizedLogin, ClientAuthBlock.normalizeLogin(login));
    }

    private static Object[] testCase(String login, String normalizedLogin) {
        return new Object[] { login, normalizedLogin };
    }

}
