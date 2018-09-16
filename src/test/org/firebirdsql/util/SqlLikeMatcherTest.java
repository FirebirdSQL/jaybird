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
package org.firebirdsql.util;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SqlLikeMatcherTest {

    @Test
    public void testIsLikeSpecialChar_noSpecials() {
        assertFalse("No LIKE special char", SqlLikeMatcher.isLikeSpecialChar('a'));
    }

    @Test
    public void testIsLikeSpecialChar_underscore() {
        assertTrue("LIKE special char", SqlLikeMatcher.isLikeSpecialChar('_'));
    }

    @Test
    public void testIsLikeSpecialChar_percent() {
        assertTrue("LIKE special char", SqlLikeMatcher.isLikeSpecialChar('%'));
    }

    @Test
    public void testIsLikeSpecialChar_backslash() {
        assertTrue("LIKE special char", SqlLikeMatcher.isLikeSpecialChar('\\'));
    }

    @Test
    public void containsLikeSpecialChars_noSpecials() {
        assertThat(Arrays.asList("abcd#*&$^", "", "xyz with spaces"),
                everyItem(not(ContainsLikeSpecialCharMatcher.containsLikeSpecialCharMatcher())));
    }

    @Test
    public void containsLikeSpecialChars_withSpecials() {
        assertThat(Arrays.asList("%", "_", "\\", "abc%", "_ab%", "a\\_b"),
                everyItem(ContainsLikeSpecialCharMatcher.containsLikeSpecialCharMatcher()));
    }

    /**
     * Matcher implementation serving as test harness for {@link SqlLikeMatcher#containsLikeSpecialChars(String)} .
     */
    private static class ContainsLikeSpecialCharMatcher extends TypeSafeMatcher<String> {

        private static final ContainsLikeSpecialCharMatcher INSTANCE = new ContainsLikeSpecialCharMatcher();

        @Override
        protected boolean matchesSafely(String s) {
            return SqlLikeMatcher.containsLikeSpecialChars(s);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("containsLikeSpecialChars");
        }

        @Factory
        public static ContainsLikeSpecialCharMatcher containsLikeSpecialCharMatcher() {
            return INSTANCE;
        }
    }

}