// SPDX-FileCopyrightText: Copyright 2012-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.jdbc.QuoteStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;

import static org.firebirdsql.jdbc.escape.EscapeFunctionAsserts.assertParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link FBEscapedParser}.
 *
 * @author Mark Rotteveel
 */
class FBEscapedParserTest {

    private final FBEscapedParser parser =
            FBEscapedParser.of(FBTestProperties.maximumVersionSupported(), QuoteStrategy.DIALECT_3);

    private String toNative(String input) throws SQLException {
        return parser.toNative(input);
    }

    @Test
    void testStringWithoutEscapes() throws Exception {
        final String input = "SELECT * FROM some_table WHERE x = 'xyz'";

        assertEquals(input, toNative(input), "Expected output identical to input for string without escapes");
    }

    @ParameterizedTest
    @ValueSource(strings = { "escape", "ESCAPE" })
    void testEscapeEscape(String escape) throws Exception {
        final String input = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' {" + escape + " '&'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' ESCAPE '&'";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {escape ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "fn", "FN" })
    void testFunctionEscape(String fn) throws Exception {
        final String input = "SELECT * FROM some_table WHERE {" + fn + " abs(x)} = ?";
        final String expectedOutput = "SELECT * FROM some_table WHERE abs(x) = ?";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {fn ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "d", "D" })
    void testDateEscape(String d) throws Exception {
        final String input = "SELECT * FROM some_table WHERE x = {" + d + " '2012-12-28'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x = DATE '2012-12-28'";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {d ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "t", "T" })
    void testTimeEscape(String t) throws Exception {
        final String input = "SELECT * FROM some_table WHERE x = {" + t + " '22:15:28'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x = TIME '22:15:28'";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {t ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "ts", "TS" })
    void testTimestampEscape(String ts) throws Exception {
        final String input = "SELECT * FROM some_table WHERE x = {" + ts + " '2012-12-28 22:15:28'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x = TIMESTAMP '2012-12-28 22:15:28'";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {ts ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "oj", "OJ" })
    void testOuterjoinEscape(String oj) throws Exception {
        final String input = "SELECT * FROM {" + oj + " some_table FULL OUTER JOIN some_other_table ON some_table.x = some_other_table.x}";
        final String expectedOutput = "SELECT * FROM some_table FULL OUTER JOIN some_other_table ON some_table.x = some_other_table.x";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {oj ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "limit", "LIMIT" })
    void testSimpleLimitEscape(String limit) throws Exception {
        final String input = "SELECT * FROM some_table {" + limit + " 10}";
        final String expectedOutput = "SELECT * FROM some_table ROWS 10";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {limit ..}");
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

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {limit ..}");
    }

    @Test
    void testSimpleLimitEscapeWithParameter() throws Exception {
        final String input = "SELECT * FROM some_table {limit ?}";
        final String expectedOutput = "SELECT * FROM some_table ROWS ?";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {limit ..}");
    }

    @Test
    void testExtendedLimitEscapeRowsParameter() throws Exception {
        final String input = "SELECT * FROM some_table {limit ? offset 15}";
        final String expectedOutput = "SELECT * FROM some_table ROWS 15 TO 15+?";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {limit ..}");
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

        assertParseException(() -> toNative(input),
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

        assertParseException(() -> toNative(input),
                "Extended limit escape ({limit <rows> offset <offset_rows>}) does not support parameters for <offset_rows>");
    }

    @ParameterizedTest
    @ValueSource(strings = { "call", "CALL" })
    void testCallEscape(String call) throws Exception {
        final String input = "{" + call + " FUNC(?,?)}";
        final String expectedOutput = "EXECUTE PROCEDURE \"FUNC\"(?,?)";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {call ..}");
    }

    @ParameterizedTest
    @ValueSource(strings = { "call", "CALL" })
    void testQuestionmarkCallEscape(String call) throws Exception {
        final String input = "{?=" + call + " FUNC(?,?)}";
        final String expectedOutput = "EXECUTE PROCEDURE \"FUNC\"(?,?,?)";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {call ..}");
    }

    @Test
    void testQuestionmarkCallEscapeExtraWhitespace() throws Exception {
        final String input = "{? = call FUNC(?,?)}";
        final String expectedOutput = "EXECUTE PROCEDURE \"FUNC\"(?,?,?)";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {call ..}");
    }

    @Test
    void testUnsupportedKeyword() {
        // NOTE: need to include an existent keyword, otherwise string isn't parsed at all
        final String input = "{fn ABS(?)} {doesnotexist xyz}";

        assertParseException(() -> toNative(input),
                "Unknown keyword doesnotexist for escaped syntax.");
    }

    @Test
    void testTooManyCurlyBraceOpen() {
        final String input = "{escape '&'";

        assertParseException(() -> toNative(input), "Unbalanced JDBC escape, too many '{'");
    }

    @Test
    void testTooManyCurlyBraceClose() {
        final String input = "{escape '&'}}";

        assertParseException(() -> toNative(input), "Unbalanced JDBC escape, too many '}'");
    }

    @Test
    void testCurlyBraceOpenClose() {
        // NOTE: need to include an existent keyword, otherwise string isn't parsed at all
        final String input = "{escape '&'} {}";

        assertParseException(() -> toNative(input),
                "Unexpected first character inside JDBC escape: }");
    }

    @Test
    void testAdditionalWhitespaceBetweenEscapeAndParameter() throws Exception {
        final String input = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' {escape      '&'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' ESCAPE '&'";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {escape ..}");
    }

    @Test
    void testAdditionalWhitespaceAfterParameter() throws Exception {
        final String input = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' {escape '&'     }";
        final String expectedOutput = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' ESCAPE '&'";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for {escape ..}");
    }

    @Test
    void testNestedEscapes() throws Exception {
        final String input = "{fn LTRIM({fn RTRIM('  abc  ')})}";
        final String expectedOutput = "TRIM(LEADING FROM TRIM(TRAILING FROM '  abc  '))";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser preserves whitespace inside parameters (implementation coalesces multiple whitespace characters into one space)
     */
    @Test
    void testWhitespaceInParameter() throws Exception {
        final String input = "{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}";
        final String expectedOutput = "TRIM(LEADING FROM CAST( ? AS VARCHAR(10)))";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser does not process JDBC escapes inside a line comment
     */
    @Test
    void testEscapeInLineComment() throws Exception {
        final String input = "{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))} --{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}\n{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}";
        final String expectedOutput = "TRIM(LEADING FROM CAST( ? AS VARCHAR(10))) --{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}\nTRIM(LEADING FROM CAST( ? AS VARCHAR(10)))";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser does not process JDBC escapes inside a block comment
     */
    @Test
    void testEscapeInBlockComment() throws Exception {
        final String input = "{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))} /*{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}\n*/{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}";
        final String expectedOutput = "TRIM(LEADING FROM CAST( ? AS VARCHAR(10))) /*{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}\n*/TRIM(LEADING FROM CAST( ? AS VARCHAR(10)))";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser correctly processes an escape that is directly after a '-' (potential start of line comment).
     */
    @Test
    void testLineCommentStartFollowedByEscape() throws Exception {
        final String input = "6-{fn EXP(2)}";
        final String expectedOutput = "6-EXP(2)";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for nested escapes");
    }

    /**
     * Tests if the parser correctly processes an escape that is directly after a '/' (potential start of block comment).
     */
    @Test
    void testBlockCommentStartFollowedByEscape() throws Exception {
        final String input = "6/{fn EXP(2)}";
        final String expectedOutput = "6/EXP(2)";

        assertEquals(expectedOutput, toNative(input), "Unexpected output for nested escapes");
    }

    @Test
    void testQLiteral_basic() throws Exception {
        final String input = "q'x {fn EXP(2)} x'";

        assertEquals(input, toNative(input), "Expected identical output for Q-literal with JDBC escape in literal");
    }

    @Test
    void testQLiteral_processesEscapeAfterLiteral() throws Exception {
        final String input = "Q'x {fn EXP(2)} x'{fn EXP(2)}";
        final String expectedOutput = "Q'x {fn EXP(2)} x'EXP(2)";

        assertEquals(expectedOutput, toNative(input), "Unexpected output");
    }

    @Test
    void testQButNotLiteral() throws Exception {
        final String input = "qMx {fn EXP(2)} x'";
        final String expectedOutput = "qMx EXP(2) x'";

        assertEquals(expectedOutput, toNative(input), "Unexpected output");
    }

    @Test
    void testQLiteralStart_AtEndOfString_throwsParseException() {
        final String input = "{fn EXP(2)} q'";

        assertParseException(() -> toNative(input),
                "Unexpected end of string at parser state Q_LITERAL_START");
    }

    @Test
    void testQLiteral_InLiteralEndOfString_throwsParseException() {
        final String input = "{fn EXP(2)} q'abc";

        assertParseException(() -> toNative(input),
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

        assertEquals(input, toNative(input), "Unexpected output");
    }
}
