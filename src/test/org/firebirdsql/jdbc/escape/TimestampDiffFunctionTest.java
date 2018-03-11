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

/**
 * Tests for {@link TimestampDiffFunction}.
 * <p>
 * See also {@link TimestampDiffFunctionParameterizedTest}
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TimestampDiffFunctionTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final TimestampDiffFunction function = new TimestampDiffFunction();

    // Happy path tested in TimestampAddFunctionParameterizedTest

    @Test
    public void testZeroParameters_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage("Expected 3 parameters for TIMESTAMPDIFF, received 0");

        function.apply();
    }

    @Test
    public void testOneParameter_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage("Expected 3 parameters for TIMESTAMPDIFF, received 1");

        function.apply("SQL_TSI_MINUTE");
    }

    @Test
    public void testTwoParameter_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage("Expected 3 parameters for TIMESTAMPDIFF, received 2");

        function.apply("SQL_TSI_MINUTE", "STAMP");
    }

    @Test
    public void testFourParameter_throwsException() throws Exception {
        expectedException.expect(FBSQLParseException.class);
        expectedException.expectMessage("Expected 3 parameters for TIMESTAMPDIFF, received 4");

        function.apply("SQL_TSI_MINUTE", "STAMP", "CURRENT_TIMESTAMP", "extra");
    }

}
