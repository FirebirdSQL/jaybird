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
package org.firebirdsql.jdbc.metadata;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

class MetadataPatternTest {

    @Test
    void testIsPatternSpecialChar_noSpecials() {
        assertFalse(MetadataPattern.isPatternSpecialChar('a'), "No pattern special char");
    }

    @Test
    void testIsPatternSpecialChar_underscore() {
        assertTrue(MetadataPattern.isPatternSpecialChar('_'), "pattern special char");
    }

    @Test
    void testIsPatternSpecialChar_percent() {
        assertTrue(MetadataPattern.isPatternSpecialChar('%'), "pattern special char");
    }

    @Test
    void testIsPatternSpecialChar_backslash() {
        assertTrue(MetadataPattern.isPatternSpecialChar('\\'), "pattern special char");
    }

    @Test
    void containsPatternSpecialChars_noSpecials() {
        assertThat(Arrays.asList("abcd#*&$^", "", "xyz with spaces"),
                everyItem(not(ContainsPatternSpecialCharMatcher.containsPatternSpecialCharMatcher())));
    }

    @Test
    void containsPatternSpecialChars_withSpecials() {
        assertThat(Arrays.asList("%", "_", "\\", "abc%", "_ab%", "a\\_b"),
                everyItem(ContainsPatternSpecialCharMatcher.containsPatternSpecialCharMatcher()));
    }

    @Test
    void testEscapeWildcards() {
        assertEquals("test\\\\me", MetadataPattern.escapeWildcards("test\\me"), "escape wildcard incorrect");
        assertEquals("test\\%me", MetadataPattern.escapeWildcards("test%me"), "escape wildcard incorrect");
        assertEquals("test\\_me", MetadataPattern.escapeWildcards("test_me"), "escape wildcard incorrect");
        assertEquals("test\\%\\_me", MetadataPattern.escapeWildcards("test%_me"), "escape wildcard incorrect");
        assertEquals("test\\\\\\_me", MetadataPattern.escapeWildcards("test\\_me"), "escape wildcard incorrect");
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                testCase(null, MetadataPattern.ConditionType.NONE, null),
                testCase("%", MetadataPattern.ConditionType.NONE, null),
                testCase("", MetadataPattern.ConditionType.SQL_EQUALS, ""),
                testCase("_", MetadataPattern.ConditionType.SQL_LIKE, "_"),
                testCase("\\", MetadataPattern.ConditionType.SQL_EQUALS, "\\"),
                testCase("\\\\", MetadataPattern.ConditionType.SQL_EQUALS, "\\"),
                testCase("a", MetadataPattern.ConditionType.SQL_EQUALS, "a"),
                testCase("a%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "a"),
                testCase("\\a%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "\\a"),
                testCase("ab c%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "ab c"),
                testCase("ab\\_c%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "ab_c"),
                testCase("ab\\%c%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "ab%c"),
                testCase("a_b%", MetadataPattern.ConditionType.SQL_LIKE, "a_b%"),
                testCase("a%b%", MetadataPattern.ConditionType.SQL_LIKE, "a%b%"),
                testCase("a\\__b%", MetadataPattern.ConditionType.SQL_LIKE, "a\\__b%"),
                testCase("a_", MetadataPattern.ConditionType.SQL_LIKE, "a_"),
                testCase("%abc", MetadataPattern.ConditionType.SQL_LIKE, "%abc"),
                testCase("_abc", MetadataPattern.ConditionType.SQL_LIKE, "_abc"),
                testCase("_ab\\c", MetadataPattern.ConditionType.SQL_LIKE, "_ab\\\\c"),
                testCase("_ab\\", MetadataPattern.ConditionType.SQL_LIKE, "_ab\\\\"),
                testCase("_ab\\_", MetadataPattern.ConditionType.SQL_LIKE, "_ab\\_"),
                testCase("_ab\\%", MetadataPattern.ConditionType.SQL_LIKE, "_ab\\%"),
                testCase("ab\\%cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab%cd"),
                testCase("ab\\_cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab_cd"),
                testCase("ab\\_\\cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab_\\cd"),
                testCase("a_\\_\\cd", MetadataPattern.ConditionType.SQL_LIKE, "a_\\_\\\\cd"),
                testCase("ab\\\\cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab\\cd"),
                testCase("ab\\cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab\\cd"),
                testCase("ab\\", MetadataPattern.ConditionType.SQL_EQUALS, "ab\\")
        );
    }

    @ParameterizedTest(name = "{index}: {0} => {1} value {2}")
    @MethodSource("parameters")
    void testCompile(String metadataPattern, MetadataPattern.ConditionType expectedConditionType,
            String expectedConditionValue) {
        MetadataPattern compiledPattern = MetadataPattern.compile(metadataPattern);

        assertEquals(expectedConditionType, compiledPattern.getConditionType(), "conditionType");
        assertEquals(expectedConditionValue, compiledPattern.getConditionValue(), "conditionValue");
    }

    private static Arguments testCase(String metadataPattern, MetadataPattern.ConditionType expectedConditionType,
            String expectedConditionValue) {
        return Arguments.of(metadataPattern, expectedConditionType, expectedConditionValue);
    }

    /**
     * Matcher implementation serving as test harness for {@link MetadataPattern#containsPatternSpecialChars(String)} .
     */
    private static class ContainsPatternSpecialCharMatcher extends TypeSafeMatcher<String> {

        private static final ContainsPatternSpecialCharMatcher INSTANCE = new ContainsPatternSpecialCharMatcher();

        @Override
        protected boolean matchesSafely(String s) {
            return MetadataPattern.containsPatternSpecialChars(s);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("containsPatternSpecialChars");
        }

        public static ContainsPatternSpecialCharMatcher containsPatternSpecialCharMatcher() {
            return INSTANCE;
        }
    }

}