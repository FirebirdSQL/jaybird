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

import org.firebirdsql.jdbc.escape.FBEscapedParser.EscapeParserMode;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FBEscapedFunctionHelperTest {

    /**
     * Test function call that contains quoted identifiers as well as
     * commas and double quotes in string literals.
     */
    private static final String ESCAPED_FUNCTION_CALL = "test(\"arg1\", 12, ',\"')";
    private static final String ESCAPED_FUNCTION_NAME = "test";
    private static final List<String> ESCAPED_FUNCTION_PARAMS = Arrays.asList("\"arg1\"", "12", "',\"'");
    private static final String LCASE_FUNCTION_CALL = "{fn lcase('some name')}";
    private static final String LCASE_FUNCTION_TEST = "LOWER('some name')";
    private static final String UCASE_FUNCTION_CALL = "{fn ucase(some_identifier)}";
    private static final String UCASE_FUNCTION_TEST = "UPPER(some_identifier)";

    @Test
    public void testParseArguments() throws SQLException {
        List<String> parsedParams = FBEscapedFunctionHelper.parseArguments(ESCAPED_FUNCTION_CALL);

        assertEquals("Parsed params should be equal to the test ones.",
                ESCAPED_FUNCTION_PARAMS, parsedParams);
    }

    /**
     * Test if function name is parsed correctly.
     */
    @Test
    public void testParseName() throws SQLException {
        String name = FBEscapedFunctionHelper.parseFunction(ESCAPED_FUNCTION_CALL);

        assertEquals("Parsed function name should be equal to the test one.",
                ESCAPED_FUNCTION_NAME, name);
    }

    @Test
    public void testEscapedFunctionCall() throws SQLException {
        FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);

        String ucaseTest = parser.parse(UCASE_FUNCTION_CALL);
        assertEquals("ucase function parsing should be correct",
                UCASE_FUNCTION_TEST, ucaseTest);
    }

    @Test
    public void testUseStandardUdf() throws SQLException {
        FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_STANDARD_UDF);

        String lcaseTest = parser.parse(LCASE_FUNCTION_CALL);
        assertEquals("lcase function parsing should be correct",
                LCASE_FUNCTION_TEST, lcaseTest);
    }
}
