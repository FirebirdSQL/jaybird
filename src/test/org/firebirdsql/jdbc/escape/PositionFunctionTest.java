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
package org.firebirdsql.jdbc.escape;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PositionFunctionTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final PositionFunction function = new PositionFunction();

    @Test
    public void testSingleParameter() throws Exception {
        assertEquals("POSITION('abc' in 'defabc')", function.apply("'abc' in 'defabc'"));
    }

    @Test
    public void testTwoParameters_CHARACTERS() throws Exception {
        assertEquals("POSITION('abc' in 'defabc')", function.apply("'abc' in 'defabc'", "CHARACTERS"));
    }

    @Test
    public void testTwoParameters_characters() throws Exception {
        assertEquals("POSITION('abc' in 'defabc')", function.apply("'abc' in 'defabc'", "characters"));
    }

    @Test
    public void testTwoParameters_characters_whitespace() throws Exception {
        assertEquals("POSITION('abc' in 'defabc')", function.apply("'abc' in 'defabc'", " characters "));
    }


    @Test
    public void testTwoParameters_OCTETS_returnsNullForPassthrough() throws Exception {
        assertNull(function.apply("'abc' in 'defabc'", "OCTETS"));
    }

    @Test
    public void testTwoParameters_invalid_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage("Second parameter for POSITION must be OCTETS or CHARACTERS, was invalid");

        function.apply("'abc' in 'defabc'", "invalid");
    }

    @Test
    public void testZeroParameters_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage("Expected 1 or 2 parameters for POSITION, received 0");

        function.apply();
    }

    @Test
    public void testThreeParameters_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage("Expected 1 or 2 parameters for POSITION, received 3");

        function.apply("abc", "defabc", "CHARACTERS");
    }
}
