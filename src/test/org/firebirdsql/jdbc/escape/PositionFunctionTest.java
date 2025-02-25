// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.Test;

import static org.firebirdsql.jdbc.escape.EscapeFunctionAsserts.assertParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PositionFunctionTest {

    private static final PositionFunction function = new PositionFunction();

    @Test
    void testSingleParameter() throws Exception {
        assertEquals("POSITION('abc' in 'defabc')", function.apply("'abc' in 'defabc'"));
    }

    @Test
    void testTwoParameters_CHARACTERS() throws Exception {
        assertEquals("POSITION('abc' in 'defabc')", function.apply("'abc' in 'defabc'", "CHARACTERS"));
    }

    @Test
    void testTwoParameters_characters() throws Exception {
        assertEquals("POSITION('abc' in 'defabc')", function.apply("'abc' in 'defabc'", "characters"));
    }

    @Test
    void testTwoParameters_characters_whitespace() throws Exception {
        assertEquals("POSITION('abc' in 'defabc')", function.apply("'abc' in 'defabc'", " characters "));
    }


    @Test
    void testTwoParameters_OCTETS_returnsNullForPassthrough() throws Exception {
        assertNull(function.apply("'abc' in 'defabc'", "OCTETS"));
    }

    @Test
    void testTwoParameters_invalid_throwsException() {
        assertParseException(() -> function.apply("'abc' in 'defabc'", "invalid"),
                "Second parameter for POSITION must be OCTETS or CHARACTERS, was invalid");
    }

    @Test
    void testZeroParameters_throwsException() {
        assertParseException(function::apply, "Expected 1 or 2 parameters for POSITION, received 0");
    }

    @Test
    void testThreeParameters_throwsException() {
        assertParseException(() -> function.apply("abc", "defabc", "CHARACTERS"),
                "Expected 1 or 2 parameters for POSITION, received 3");
    }

}
