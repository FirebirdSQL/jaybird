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

public class LocateFunctionTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final LocateFunction function = new LocateFunction();

    @Test
    public void testTwoArgument() throws Exception {
        assertEquals("POSITION(a,b)", function.apply("a", "b"));
    }

    @Test
    public void testThreeArgument() throws Exception {
        assertEquals("POSITION(a,b,c)", function.apply("a", "b", "c"));
    }

    @Test
    public void testSingleArgument_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage(
                "Expected 2 or 3 parameters for LOCATE, received 1");

        function.apply("a");
    }

    @Test
    public void testFourArgument_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage(
                "Expected 2 or 3 parameters for LOCATE, received 4");

        function.apply("a", "b", "c", "d");
    }
}
