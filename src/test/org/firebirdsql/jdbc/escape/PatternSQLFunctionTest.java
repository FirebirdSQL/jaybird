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
