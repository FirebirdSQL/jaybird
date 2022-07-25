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
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.Test;

import static org.firebirdsql.jdbc.escape.EscapeFunctionAsserts.assertParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterLengthFunctionTest {

    private static final CharacterLengthFunction function = new CharacterLengthFunction();

    @Test
    void testSingleParameter_rendersChar_Length() throws Exception {
        assertEquals("CHAR_LENGTH('abc')", function.apply("'abc'"));
    }

    @Test
    void testSecondParameter_CHARACTERS_rendersChar_Length() throws Exception {
        assertEquals("CHAR_LENGTH('abc')", function.apply("'abc'", "CHARACTERS"));
    }

    @Test
    void testSecondParameter_characters_rendersChar_Length() throws Exception {
        assertEquals("CHAR_LENGTH('abc')", function.apply("'abc'", "characters"));
    }

    @Test
    void testSecondParameter_characters_whitespace_rendersChar_Length() throws Exception {
        assertEquals("CHAR_LENGTH('abc')", function.apply("'abc'", " characters "));
    }

    @Test
    void testSecondParameter_OCTETS_rendersOctet_Length() throws Exception {
        assertEquals("OCTET_LENGTH('abc')", function.apply("'abc'", "OCTETS"));
    }

    @Test
    void testSecondParameter_octets_rendersChar_Length() throws Exception {
        assertEquals("OCTET_LENGTH('abc')", function.apply("'abc'", "octets"));
    }

    @Test
    void testSecondParameter_octets_whitespace_rendersChar_Length() throws Exception {
        assertEquals("OCTET_LENGTH('abc')", function.apply("'abc'", " octets "));
    }

    @Test
    void testSecondParameter_wrongValue_throwsException() {
        assertParseException(() -> function.apply("'abc'", "invalid"),
                "Second parameter for CHAR(ACTER)_LENGTH must be OCTETS or CHARACTERS, was invalid");
    }

    @Test
    void testZeroParameters_throwsException() {
        assertParseException(function::apply, "Expected 1 or 2 parameters for CHAR(ACTER)_LENGTH, received 0");
    }

    @Test
    void testThreeParameters_throwsException() {
        assertParseException(() ->function.apply("'abc'", "invalid", "xyz"),
                "Expected 1 or 2 parameters for CHAR(ACTER)_LENGTH, received 3");
    }
}
