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
package org.firebirdsql.jdbc.metadata;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetadataPatternTest {

    @Test
    public void testIsPatternSpecialChar_noSpecials() {
        assertFalse("No pattern special char", MetadataPattern.isPatternSpecialChar('a'));
    }

    @Test
    public void testIsPatternSpecialChar_underscore() {
        assertTrue("pattern special char", MetadataPattern.isPatternSpecialChar('_'));
    }

    @Test
    public void testIsPatternSpecialChar_percent() {
        assertTrue("pattern special char", MetadataPattern.isPatternSpecialChar('%'));
    }

    @Test
    public void testIsPatternSpecialChar_backslash() {
        assertTrue("pattern special char", MetadataPattern.isPatternSpecialChar('\\'));
    }

    @Test
    public void containsPatternSpecialChars_noSpecials() {
        assertThat(Arrays.asList("abcd#*&$^", "", "xyz with spaces"),
                everyItem(not(ContainsPatternSpecialCharMatcher.containsPatternSpecialCharMatcher())));
    }

    @Test
    public void containsPatternSpecialChars_withSpecials() {
        assertThat(Arrays.asList("%", "_", "\\", "abc%", "_ab%", "a\\_b"),
                everyItem(ContainsPatternSpecialCharMatcher.containsPatternSpecialCharMatcher()));
    }

    @Test
    public void testEscapeWildcards() {
        assertEquals("escape wildcard incorrect", "test\\\\me", MetadataPattern.escapeWildcards("test\\me"));
        assertEquals("escape wildcard incorrect", "test\\%me", MetadataPattern.escapeWildcards("test%me"));
        assertEquals("escape wildcard incorrect", "test\\_me", MetadataPattern.escapeWildcards("test_me"));
        assertEquals("escape wildcard incorrect", "test\\%\\_me", MetadataPattern.escapeWildcards("test%_me"));
        assertEquals("escape wildcard incorrect", "test\\\\\\_me", MetadataPattern.escapeWildcards("test\\_me"));
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

        @Factory
        public static ContainsPatternSpecialCharMatcher containsPatternSpecialCharMatcher() {
            return INSTANCE;
        }
    }

}