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

public class LengthFunctionTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final LengthFunction function = new LengthFunction();

    @Test
    public void testSingleParameter_rendersChar_Length() throws Exception {
        assertEquals("CHAR_LENGTH(TRIM(TRAILING FROM 'abc'))", function.apply("'abc'"));
    }

    @Test
    public void testSecondParameter_CHARACTERS_rendersChar_Length() throws Exception {
        assertEquals("CHAR_LENGTH(TRIM(TRAILING FROM 'abc'))", function.apply("'abc'", "CHARACTERS"));
    }

    @Test
    public void testSecondParameter_characters_rendersChar_Length() throws Exception {
        assertEquals("CHAR_LENGTH(TRIM(TRAILING FROM 'abc'))", function.apply("'abc'", "characters"));
    }

    @Test
    public void testSecondParameter_characters_whitespace_rendersChar_Length() throws Exception {
        assertEquals("CHAR_LENGTH(TRIM(TRAILING FROM 'abc'))", function.apply("'abc'", " characters "));
    }

    @Test
    public void testSecondParameter_OCTETS_rendersOctet_Length() throws Exception {
        assertEquals("OCTET_LENGTH(TRIM(TRAILING FROM 'abc'))", function.apply("'abc'", "OCTETS"));
    }

    @Test
    public void testSecondParameter_octets_rendersChar_Length() throws Exception {
        assertEquals("OCTET_LENGTH(TRIM(TRAILING FROM 'abc'))", function.apply("'abc'", "octets"));
    }

    @Test
    public void testSecondParameter_octets_whitespace_rendersChar_Length() throws Exception {
        assertEquals("OCTET_LENGTH(TRIM(TRAILING FROM 'abc'))", function.apply("'abc'", " octets "));
    }

    @Test
    public void testSecondParameter_wrongValue_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage(
                "Second parameter for CHAR(ACTER)_LENGTH must be OCTETS or CHARACTERS, was invalid");

        function.apply("'abc'", "invalid");
    }

    @Test
    public void testZeroParameters_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage(
                "Expected 1 or 2 parameters for LENGTH, received 0");

        function.apply();
    }

    @Test
    public void testThreeParameters_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage(
                "Expected 1 or 2 parameters for LENGTH, received 3");

        function.apply("'abc'", "invalid", "xyz");
    }

}
