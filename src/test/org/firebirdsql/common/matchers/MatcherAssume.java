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
