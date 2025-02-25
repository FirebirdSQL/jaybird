// SPDX-FileCopyrightText: Copyright 2018-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.Test;

import static org.firebirdsql.jdbc.escape.EscapeFunctionAsserts.assertParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocateFunctionTest {

    private static final LocateFunction function = new LocateFunction();

    @Test
    void testTwoArgument() throws Exception {
        assertEquals("POSITION(a,b)", function.apply("a", "b"));
    }

    @Test
    void testThreeArgument() throws Exception {
        assertEquals("POSITION(a,b,c)", function.apply("a", "b", "c"));
    }

    @Test
    void testSingleArgument_throwsException() {
        assertParseException(() -> function.apply("a"), "Expected 2 or 3 parameters for LOCATE, received 1");
    }

    @Test
    void testFourArgument_throwsException() {
        assertParseException(() -> function.apply("a", "b", "c", "d"),
                "Expected 2 or 3 parameters for LOCATE, received 4");
    }

}
