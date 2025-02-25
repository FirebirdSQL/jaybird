// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatternSQLFunctionTest {

    private static final PatternSQLFunction function = new PatternSQLFunction("func({0}, {1})");

    @Test
    void testParameterCountEqualToArgumentCount() {
        assertEquals("func(?, 'abc')", function.apply("?", "'abc'"));
    }

    @Test
    void testParameterCountLessThanArgumentCount() {
        assertEquals("func(xyz, {1})", function.apply("xyz"));
    }

    @Test
    void testParameterCountGreaterThanArgumentCount() {
        assertEquals("func(?, 'abc')", function.apply("?", "'abc'", "xyz"));
    }
}
