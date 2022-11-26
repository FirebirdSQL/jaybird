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
package org.firebirdsql.jdbc.parser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class QuotedIdentifierTokenTest {

    private final String input;
    private final String expectedName;

    public QuotedIdentifierTokenTest(String input, String expectedName) {
        this.input = input;
        this.expectedName = expectedName;
    }

    @Test
    public void quotedIdentifier() {
        QuotedIdentifierToken token = new QuotedIdentifierToken(0, input);

        assertEquals("text", input, token.text());
        assertEquals("name", expectedName, token.name());
        assertTrue("validIdentifier", token.isValidIdentifier());
    }

    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(
                testCase("\"name\"", "name"),
                testCase("\"with\"\"double\"", "with\"double"),
                testCase("\"with\"\"multiple\"\"double\"", "with\"multiple\"double")
        );
    }

    private static Object[] testCase(String input, String expectedName) {
        return new Object[] { input, expectedName };
    }
}