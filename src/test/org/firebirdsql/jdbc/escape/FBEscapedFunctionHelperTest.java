// SPDX-FileCopyrightText: Copyright 2003-2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2012-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FBEscapedFunctionHelperTest {

    /**
     * Test function call that contains quoted identifiers as well as commas and double quotes in string literals.
     */
    private static final String ESCAPED_FUNCTION_CALL = "test(\"arg1\", 12, ',\"')";
    private static final String ESCAPED_FUNCTION_NAME = "test";
    private static final List<String> ESCAPED_FUNCTION_PARAMS = Arrays.asList("\"arg1\"", "12", "',\"'");
    private static final String UCASE_FUNCTION_CALL = "{fn ucase(some_identifier)}";
    private static final String UCASE_FUNCTION_TEST = "UPPER(some_identifier)";

    @Test
    void testParseArguments() throws SQLException {
        List<String> parsedParams = FBEscapedFunctionHelper.parseArguments(ESCAPED_FUNCTION_CALL);

        assertEquals(ESCAPED_FUNCTION_PARAMS, parsedParams, "Parsed params should be equal to the test ones");
    }

    /**
     * Test if function name is parsed correctly.
     */
    @Test
    void testParseName() throws SQLException {
        String name = FBEscapedFunctionHelper.parseFunction(ESCAPED_FUNCTION_CALL);

        assertEquals(ESCAPED_FUNCTION_NAME, name, "Parsed function name should be equal to the test one");
    }

    @Test
    void testEscapedFunctionCall() throws SQLException {
        String ucaseTest = FBEscapedParser.toNativeSql(UCASE_FUNCTION_CALL);

        assertEquals(UCASE_FUNCTION_TEST, ucaseTest, "ucase function parsing should be correct");
    }
}
