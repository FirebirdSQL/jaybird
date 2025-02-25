// SPDX-FileCopyrightText: Copyright 2019-2020 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
