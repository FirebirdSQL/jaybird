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
package org.firebirdsql.common.matchers;

import org.hamcrest.*;

import java.util.regex.Pattern;

/**
 * Hamcrest matcher for string matching a regular expression.
 *
 * @author Mark Rotteveel
 */
public class RegexMatcher extends TypeSafeMatcher<String> {

    private final Pattern pattern;

    private RegexMatcher(String regex) {
        this(Pattern.compile(regex));
    }

    private RegexMatcher(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    protected boolean matchesSafely(String s) {
        return s != null && pattern.matcher(s).matches();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a string match regular expression ").appendValue(pattern);
    }

    public static RegexMatcher matchesRegex(String regex) {
        return new RegexMatcher(regex);
    }
}
