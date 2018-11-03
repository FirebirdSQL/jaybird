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
package org.firebirdsql.jdbc;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class MetadataPatternMatcherParameterizedTest {

    private final String metadataPattern;
    private final String expectedRegexPattern;
    private final List<String> expectedMatches;

    public MetadataPatternMatcherParameterizedTest(String metadataPattern, String expectedRegexPattern,
            List<String> expectedMatches) {
        this.metadataPattern = metadataPattern;
        this.expectedRegexPattern = expectedRegexPattern;
        this.expectedMatches = expectedMatches;
    }

    @Parameterized.Parameters(name = "{index}: {0} => {1}")
    public static List<Object[]> parameters() {
        return asList(
                testCase(null, null, asList("", "a", "XYZ", "a_x")),
                testCase("%", ".*", asList("", "a", "XYZ", "a_x")),
                testCase("abc", "\\Qabc\\E", singletonList("abc")),
                testCase("ab_c", "\\Qab\\E.\\Qc\\E", asList("abac", "ab_c", "abDc")),
                testCase("ab%c", "\\Qab\\E.*\\Qc\\E", asList("abc", "ab%c", "ab_c", "ab123456789cabc")),
                testCase("ab\\_c", "\\Qab_c\\E", singletonList("ab_c")),
                testCase("ab\\\\c", "\\Qab\\c\\E", singletonList("ab\\c")),
                testCase("AB%", "\\QAB\\E.*", asList("AB", "ABC", "AB%", "AB_", "ABCDEFGHIJK...")),
                testCase("%AB%", ".*\\QAB\\E.*", asList("AB", "ABAB", "ABABAB", "%AB%", "1234AB1234AB123")),
                // Syntax error in LIKE, but accepted in metadata pattern
                testCase("ab\\c", "\\Qab\\c\\E", singletonList("ab\\c")),
                // Syntax error in LIKE, but accepted in metadata patter
                testCase("ab\\", "\\Qab\\\\E", singletonList("ab\\")),
                // Regex quote edge cases; Syntax error in LIKE, but accepted in metadata patter
                testCase("\\Qabc\\E", "\\Q\\Qabc\\E\\\\E\\Q\\E", singletonList("\\Qabc\\E")),
                // Regex quote edge cases; Syntax error in LIKE, but accepted in metadata patter
                testCase("abc\\Edef", "\\Qabc\\E\\\\E\\Qdef\\E", singletonList("abc\\Edef"))
        );
    }

    @Test
    public void testPatternToRegex() {
        assumeThat("patternToRegex not supported for this pattern", metadataPattern, is(notNullValue()));
        assertEquals("Unexpected regex pattern for '" + metadataPattern + "'",
                expectedRegexPattern, MetadataPatternMatcher.patternToRegex(metadataPattern));
    }

    @Test
    public void testMetadataPatternMatcher() {
        final MetadataPatternMatcher metadataPatternMatcher =
                MetadataPattern.compile(metadataPattern).toMetadataPatternMatcher();
        Matcher<String> matchesMetadataPattern = new TypeSafeMatcher<String>() {
            @Override
            protected boolean matchesSafely(String s) {
                return metadataPatternMatcher.matches(s);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("matches pattern ").appendValue(metadataPattern);
            }
        };

        assertThat(expectedMatches, everyItem(matchesMetadataPattern));
        if (metadataPattern == null || "%".equals(metadataPattern)) {
            assertTrue("All pattern should match null", metadataPatternMatcher.matches(null));
        } else {
            assertFalse("Pattern should not match null", metadataPatternMatcher.matches(null));
        }
    }

    private static Object[] testCase(String likePattern, String expectedRegexPattern, List<String> expectedMatches) {
        return new Object[] { likePattern, expectedRegexPattern, expectedMatches };
    }
}