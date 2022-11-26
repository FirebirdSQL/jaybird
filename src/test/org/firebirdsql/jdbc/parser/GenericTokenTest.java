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
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class GenericTokenTest {

    private final String tokenText;
    private final boolean expectedValidIdentifier;

    public GenericTokenTest(String tokenText, boolean expectedValidIdentifier) {
        this.tokenText = tokenText;
        this.expectedValidIdentifier = expectedValidIdentifier;
    }

    @Test
    public void isValidIdentifier() {
        GenericToken genericToken = new GenericToken(0, tokenText);

        assertEquals(expectedValidIdentifier, genericToken.isValidIdentifier());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(
                testCase("a", true),
                testCase("A", true),
                testCase("AB", true),
                testCase("abc", true),
                testCase("A3", true),
                testCase("a3a", true),
                testCase("A$", true),
                testCase("a$a", true),
                testCase("A_", true),
                testCase("a_A", true),
                testCase("RDB$RELATION", true),
                testCase("$", false),
                testCase("$A", false),
                testCase("_", false),
                testCase("_A", false),
                testCase("A\u00e8", false),
                testCase("3a", false),
                // Would not normally occur as GenericToken
                testCase("3", false)
        );
    }
    
    private static Object[] testCase(String tokenText, boolean expectedValidIdentifier) {
        return new Object[] { tokenText, expectedValidIdentifier };
    }
}