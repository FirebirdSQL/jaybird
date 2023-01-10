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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.firebirdsql.jdbc.escape.EscapeFunctionAsserts.assertParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link FBEscapedParser}.
 *
 * @author Mark Rotteveel
 */
class FBEscapedParserTest {

    @Test
    void testStringWithoutEscapes() throws Exception {
        final String input = "SELECT * FROM some_table WHERE x = 'xyz'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(input, parseResult, "Expected output identical to input for string without escapes");
    }

    @ParameterizedTest
    @ValueSource(strings = { "escape", "ESCAPE" })
    void testEscapeEscape(String escape) throws Exception {
        final String input = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' {" + escape + " '&'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' ESCAPE '&'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {escape ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "fn", "FN" })
    void testFunctionEscape(String fn) throws Exception {
        final String input = "SELECT * FROM some_table WHERE {" + fn + " abs(x)} = ?";
        final String expectedOutput = "SELECT * FROM some_table WHERE abs(x) = ?";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {fn ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "d", "D" })
    void testDateEscape(String d) throws Exception {
        final String input = "SELECT * FROM some_table WHERE x = {" + d + " '2012-12-28'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x = DATE '2012-12-28'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {d ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "t", "T" })
    void testTimeEscape(String t) throws Exception {
        final String input = "SELECT * FROM some_table WHERE x = {" + t + " '22:15:28'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x = TIME '22:15:28'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {t ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "ts", "TS" })
    void testTimestampEscape(String ts) throws Exception {
        final String input = "SELECT * FROM some_table WHERE x = {" + ts + " '2012-12-28 22:15:28'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x = TIMESTAMP '2012-12-28 22:15:28'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {ts ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "oj", "OJ" })
    void testOuterjoinEscape(String oj) throws Exception {
        final String input = "SELECT * FROM {" + oj + " some_table FULL OUTER JOIN some_other_table ON some_table.x = some_other_table.x}";
        final String expectedOutput = "SELECT * FROM some_table FULL OUTER JOIN some_other_table ON some_table.x = some_other_table.x";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {oj ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "limit", "LIMIT" })
    void testSimpleLimitEscape(String limit) throws Exception {
        final String input = "SELECT * FROM some_table {" + limit + " 10}";
        final String expectedOutput = "SELECT * FROM some_table ROWS 10";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {limit ..}");
    }

    @ParameterizedTest
    @CsvSource({
            "limit, offset",
            "LIMIT, offset",
            "limit, OFFSET",
    })
    void testExtendedLimitEscape(String limit, String offset) throws Exception {
        final String input = "SELECT * FROM some_table {" + limit + " 10 " + offset + " 15}";
        final String expectedOutput = "SELECT * FROM some_table ROWS 15 TO 15+10";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {limit ..}");
    }

    @Test
    void testSimpleLimitEscapeWithParameter() throws Exception {
        final String input = "SELECT * FROM some_table {limit ?}";
        final String expectedOutput = "SELECT * FROM some_table ROWS ?";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {limit ..}");
    }

    @Test
    void testExtendedLimitEscapeRowsParameter() throws Exception {
        final String input = "SELECT * FROM some_table {limit ? offset 15}";
        final String expectedOutput = "SELECT * FROM some_table ROWS 15 TO 15+?";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {limit ..}");
    }

    /**
     * Test of limit escape with the rows and row_offset parameter with a literal value for rows and a parameter for offset_rows.
     * <p>
     * Expects exception, as parameter for offset_rows is not supported by driver implementation.
     * </p>
     */
    @Test
    void testExtendedLimitEscapeOffsetParameter() {
        final String input = "SELECT * FROM some_table {limit 10 offset ?}";

        assertParseException(() -> FBEscapedParser.toNativeSql(input),
                "Extended limit escape ({limit <rows> offset <offset_rows>}) does not support parameters for <offset_rows>");
    }

    /**
     * Test of limit escape with the rows and row_offset parameter with a parameter for rows and offset_rows.
     * <p>
     * Expects exception, as parameter for offset_rows is not supported by driver implementation.
     * </p>
     */
    @Test
    void testExtendedLimitEscapeRowsAndOffsetParameter() {
        final String input = "SELECT * FROM some_table {limit ? offset ?}";

        assertParseException(() -> FBEscapedParser.toNativeSql(input),
                "Extended limit escape ({limit <rows> offset <offset_rows>}) does not support parameters for <offset_rows>");
    }

    @ParameterizedTest
    @ValueSource(strings = { "call", "CALL" })
    void testCallEscape(String call) throws Exception {
        final String input = "{" + call + " FUNCTION(?,?)}";
        final String expectedOutput = "EXECUTE PROCEDURE FUNCTION(?,?)";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {call ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "call", "CALL" })
    void testQuestionmarkCallEscape(String call) throws Exception {
        final String input = "{?=" + call + " FUNCTION(?,?)}";
        final String expectedOutput = "EXECUTE PROCEDURE FUNCTION(?,?,?)";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {call ..}");
    }

    @Test
    void testQuestionmarkCallEscapeExtraWhitespace() throws Exception {
        final String input = "{? = call FUNCTION(?,?)}";
        final String expectedOutput = "EXECUTE PROCEDURE FUNCTION(?,?,?)";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {call ..}");
    }

    @Test
    void testUnsupportedKeyword() {
        // NOTE: need to include an existent keyword, otherwise string isn't parsed at all
        final String input = "{fn ABS(?)} {doesnotexist xyz}";

        assertParseException(() -> FBEscapedParser.toNativeSql(input),
                "Unknown keyword doesnotexist for escaped syntax.");
    }

    @Test
    void testTooManyCurlyBraceOpen() {
        final String input = "{escape '&'";

        assertParseException(() -> FBEscapedParser.toNativeSql(input), "Unbalanced JDBC escape, too many '{'");
    }

    @Test
    void testTooManyCurlyBraceClose() {
        final String input = "{escape '&'}}";

        assertParseException(() -> FBEscapedParser.toNativeSql(input), "Unbalanced JDBC escape, too many '}'");
    }

    @Test
    void testCurlyBraceOpenClose() {
        // NOTE: need to include an existent keyword, otherwise string isn't parsed at all
        final String input = "{escape '&'} {}";

        assertParseException(() -> FBEscapedParser.toNativeSql(input),
                "Unexpected first character inside JDBC escape: }");
    }

    @Test
    void testAdditionalWhitespaceBetweenEscapeAndParameter() throws Exception {
        final String input = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' {escape      '&'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' ESCAPE '&'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {escape ..}");
    }

    @Test
    void testAdditionalWhitespaceAfterParameter() throws Exception {
        final String input = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' {escape '&'     }";
        final String expectedOutput = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' ESCAPE '&'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for {escape ..}");
    }

    @Test
    void testNestedEscapes() throws Exception {
        final String input = "{fn LTRIM({fn RTRIM('  abc  ')})}";
        final String expectedOutput = "TRIM(LEADING FROM TRIM(TRAILING FROM '  abc  '))";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser preserves whitespace inside parameters (implementation coalesces multiple whitespace characters into one space)
     */
    @Test
    void testWhitespaceInParameter() throws Exception {
        final String input = "{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}";
        final String expectedOutput = "TRIM(LEADING FROM CAST( ? AS VARCHAR(10)))";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser does not process JDBC escapes inside a line comment
     */
    @Test
    void testEscapeInLineComment() throws Exception {
        final String input = "{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))} --{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}\n{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}";
        final String expectedOutput = "TRIM(LEADING FROM CAST( ? AS VARCHAR(10))) --{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}\nTRIM(LEADING FROM CAST( ? AS VARCHAR(10)))";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser does not process JDBC escapes inside a block comment
     */
    @Test
    void testEscapeInBlockComment() throws Exception {
        final String input = "{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))} /*{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}\n*/{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}";
        final String expectedOutput = "TRIM(LEADING FROM CAST( ? AS VARCHAR(10))) /*{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}\n*/TRIM(LEADING FROM CAST( ? AS VARCHAR(10)))";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser correctly processes an escape that is directly after a '-' (potential start of line comment).
     */
    @Test
    void testLineCommentStartFollowedByEscape() throws Exception {
        final String input = "6-{fn EXP(2)}";
        final String expectedOutput = "6-EXP(2)";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser correctly processes an escape that is directly after a '/' (potential start of block comment).
     */
    @Test
    void testBlockCommentStartFollowedByEscape() throws Exception {
        final String input = "6/{fn EXP(2)}";
        final String expectedOutput = "6/EXP(2)";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output for nested escapes");
    }

    @Test
    void testQLiteral_basic() throws Exception {
        final String input = "q'x {fn EXP(2)} x'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(input, parseResult, "Expected identical output for Q-literal with JDBC escape in literal");
    }

    @Test
    void testQLiteral_processesEscapeAfterLiteral() throws Exception {
        final String input = "Q'x {fn EXP(2)} x'{fn EXP(2)}";
        final String expectedOutput = "Q'x {fn EXP(2)} x'EXP(2)";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output");
    }

    @Test
    void testQButNotLiteral() throws Exception {
        final String input = "qMx {fn EXP(2)} x'";
        final String expectedOutput = "qMx EXP(2) x'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(expectedOutput, parseResult, "Unexpected output");
    }

    @Test
    void testQLiteralStart_AtEndOfString_throwsParseException() {
        final String input = "{fn EXP(2)} q'";

        assertParseException(() -> FBEscapedParser.toNativeSql(input),
                "Unexpected end of string at parser state Q_LITERAL_START");
    }

    @Test
    void testQLiteral_InLiteralEndOfString_throwsParseException() {
        final String input = "{fn EXP(2)} q'abc";

        assertParseException(() -> FBEscapedParser.toNativeSql(input),
                "Unexpected end of string at parser state Q_LITERAL_START");
    }

    @Test
    void testQLiteralSpecials() throws Exception {
        checkQLiteralSpecialsBalancedStartEnd('(', ')');
        checkQLiteralSpecialsBalancedStartEnd(')', ')');
        checkQLiteralSpecialsBalancedStartEnd('{', '}');
        checkQLiteralSpecialsBalancedStartEnd('}', '}');
        checkQLiteralSpecialsBalancedStartEnd('[', ']');
        checkQLiteralSpecialsBalancedStartEnd(']', ']');
        checkQLiteralSpecialsBalancedStartEnd('<', '>');
        checkQLiteralSpecialsBalancedStartEnd('>', '>');
    }

    private void checkQLiteralSpecialsBalancedStartEnd(char start, char end) throws Exception {
        final String input = "q'" + start + " {fn EXP(2)} " + end + "'";

        String parseResult = FBEscapedParser.toNativeSql(input);
        assertEquals(input, parseResult, "Unexpected output");
    }
}
