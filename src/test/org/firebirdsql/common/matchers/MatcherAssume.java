// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class MatcherAssume {

    public static <T> void assumeThat(String reason, T actual, Matcher<? super T> matcher) {
        assumeTrue(matcher.matches(actual),
                () -> {
                    Description description = new StringDescription()
                            .appendText(reason)
                            .appendText(System.lineSeparator())
                            .appendText("Expected: ")
                            .appendDescriptionOf(matcher)
                            .appendText(System.lineSeparator())
                            .appendText("     but: ");
                    matcher.describeMismatch(actual, description);
                    return description.toString();
                });
    }

}
