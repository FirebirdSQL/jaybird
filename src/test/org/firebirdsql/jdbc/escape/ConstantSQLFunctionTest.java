// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.Test;

import static org.firebirdsql.jdbc.escape.EscapeFunctionAsserts.assertParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstantSQLFunctionTest {

    private static final ConstantSQLFunction function = new ConstantSQLFunction("name");

    @Test
    void testWithoutArguments() throws Exception {
        assertEquals("name", function.apply());
    }

    @Test
    void testWithArguments() {
        assertParseException(() -> assertEquals("name", function.apply("argument")),
                "Invalid number of arguments, expected no arguments, received 1");
    }
}
