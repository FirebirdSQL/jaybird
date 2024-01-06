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
