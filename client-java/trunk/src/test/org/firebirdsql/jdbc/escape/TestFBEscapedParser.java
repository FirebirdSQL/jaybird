/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.escape;

import static org.junit.Assert.*;

import org.firebirdsql.jdbc.escape.FBEscapedParser.EscapeParserMode;
import org.junit.Test;

/**
 * Tests for {@link FBEscapedParser}.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBEscapedParser {
    
    @Test
    public void testStringWithoutEscapes() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table WHERE x = 'xyz'";
        
        String parseResult = parser.parse(input);
        assertEquals("Expected output identical to input for string without escapes", input, parseResult);
    }
    
    @Test
    public void testEscapeEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' {escape '&'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' ESCAPE '&'";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {escape ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testFunctionEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table WHERE {fn abs(x)} = ?";
        final String expectedOutput = "SELECT * FROM some_table WHERE abs(x) = ?";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {fn ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testDateEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table WHERE x = {d '2012-12-28'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x = DATE '2012-12-28'";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {d ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testTimeEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table WHERE x = {t '22:15:28'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x = TIME '22:15:28'";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {t ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testTimestampEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table WHERE x = {ts '2012-12-28 22:15:28'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x = TIMESTAMP '2012-12-28 22:15:28'";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {ts ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testOuterjoinEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM {oj some_table FULL OUTER JOIN some_other_table ON some_table.x = some_other_table.x}";
        final String expectedOutput = "SELECT * FROM some_table FULL OUTER JOIN some_other_table ON some_table.x = some_other_table.x";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {oj ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testSimpleLimitEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table {limit 10}";
        final String expectedOutput = "SELECT * FROM some_table ROWS 10";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {limit ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testExtendedLimitEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table {limit 10 offset 15}";
        final String expectedOutput = "SELECT * FROM some_table ROWS 15 TO 15+10";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {limit ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testSimpleLimitEscapeWithParameter() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table {limit ?}";
        final String expectedOutput = "SELECT * FROM some_table ROWS ?";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {limit ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testExtendedLimitEscapeRowsParameter() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table {limit ? offset 15}";
        final String expectedOutput = "SELECT * FROM some_table ROWS 15 TO 15+?";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {limit ..}", expectedOutput, parseResult);
    }
    
    /**
     * Test of limit escape with the rows and row_offset parameter with a literal value for rows and a parameter for offset_rows.
     * <p>
     * Expects exception, as parameter for offset_rows is not supported by driver implementation.
     * </p>
     */
    @Test(expected=FBSQLParseException.class)
    public void testExtendedLimitEscapeOffsetParameter() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table {limit 10 offset ?}";
        
        parser.parse(input);
        fail("Expected parse of limit escape with parameter for offset to fail");
    }
    
    /**
     * Test of limit escape with the rows and row_offset parameter with a parameter for rows and offset_rows.
     * <p>
     * Expects exception, as parameter for offset_rows is not supported by driver implementation.
     * </p>
     */
    @Test(expected=FBSQLParseException.class)
    public void testExtendedLimitEscapeRowsAndOffsetParameter() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table {limit ? offset ?}";
        
        parser.parse(input);
        fail("Expected parse of limit escape with parameter for offset to fail");
    }
    
    @Test
    public void testCallEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "{call FUNCTION(?,?)}";
        final String expectedOutput = "EXECUTE PROCEDURE FUNCTION(?, ?)";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {call ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testQuestionmarkCallEscape() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "{?=call FUNCTION(?,?)}";
        final String expectedOutput = "EXECUTE PROCEDURE FUNCTION(?, ?, ?)";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {call ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testQuestionmarkCallEscapeExtraWhitespace() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "{? = call FUNCTION(?,?)}";
        final String expectedOutput = "EXECUTE PROCEDURE FUNCTION(?, ?, ?)";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {call ..}", expectedOutput, parseResult);
    }
    
    @Test(expected=FBSQLParseException.class)
    public void testUnsupportedKeyword() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        // NOTE: need to include an existent keyword, otherwise string isn't parsed at all
        final String input = "{fn ABS(?)} {doesnotexist xyz}";
        
        parser.parse(input);
        fail("Non existent keyword should result in an FBSQLParseException");
    }
    
    @Test(expected=FBSQLParseException.class)
    public void testTooManyCurlyBraceOpen() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "{escape '&'";
        
        parser.parse(input);
        fail("Too many open curly braces should result in an FBSQLParseException");
    }
    
    @Test(expected=FBSQLParseException.class)
    public void testTooManyCurlyBraceClose() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "{escape '&'}}";
        
        parser.parse(input);
        fail("Too many close curly braces should result in an FBSQLParseException");
    }
    
    @Test(expected=FBSQLParseException.class)
    public void testCurlyBraceOpenClose() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        // NOTE: need to include an existent keyword, otherwise string isn't parsed at all
        final String input = "{escape '&'} {}";
        
        parser.parse(input);
        fail("Too many close curly braces should result in an FBSQLParseException");
    }
    
    @Test
    public void testAdditionalWhitespaceBetweenEscapeAndParameter() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' {escape      '&'}";
        final String expectedOutput = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' ESCAPE '&'";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {escape ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testAdditionalWhitespaceAfterParameter() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' {escape '&'     }";
        final String expectedOutput = "SELECT * FROM some_table WHERE x LIKE '_x&_yz' ESCAPE '&'";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for {escape ..}", expectedOutput, parseResult);
    }
    
    @Test
    public void testNestedEscapes() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "{fn LTRIM({fn RTRIM('  abc  ')})}";
        final String expectedOutput = "TRIM(LEADING FROM TRIM(TRAILING FROM '  abc  '))";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for nested escapes", expectedOutput, parseResult);
    }
    
    /**
     * Tests if the parser preserves whitespace inside parameters (implementation coalesces multiple whitespace characters into one space)
     * @throws Exception
     */
    @Test
    public void testWhitespaceInParameter() throws Exception {
        final FBEscapedParser parser = new FBEscapedParser(EscapeParserMode.USE_BUILT_IN);
        final String input = "{fn LTRIM(CAST( ?\tAS  VARCHAR(10)))}";
        final String expectedOutput = "TRIM(LEADING FROM CAST( ? AS VARCHAR(10)))";
        
        String parseResult = parser.parse(input);
        assertEquals("Unexpected output for nested escapes", expectedOutput, parseResult);
    }
}
