// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
