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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Emulates behavior of a SQL {@code LIKE} pattern, assuming an {@code ESCAPE '\'} clause.
 * <p>
 * This implementation is not thread-safe.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class SqlLikeMatcher {

    private SqlLikeMatcher() {
        // Only allow derivation in nested classes
    }

    /**
     * Compiles a SQL {@code LIKE} pattern.
     *
     * @param sqlLikePattern
     *         SQL {@code LIKE} pattern (non-null)
     * @return Matcher for {@code sqlLikePattern}
     */
    public static SqlLikeMatcher compile(String sqlLikePattern) {
        if (containsLikeSpecialChars(requireNonNull(sqlLikePattern, "sqlLikePattern"))) {
            return new RegexMatcher(sqlLikePattern);
        }
        return new SimpleEqualsMatcher(sqlLikePattern);
    }

    /**
     * Checks if {@code value} matches the pattern of this matcher.
     * <p>
     * This method is not thread-safe.
     * </p>
     *
     * @param value
     *         Value to check
     * @return {@code true} if {@code value} matches this pattern, {@code false} otherwise
     */
    public abstract boolean matches(String value);

    /**
     * Scans string to determine if string contains any of {@code \_%} that indicates additional processing is needed.
     *
     * @param sqlLikePattern
     *         SQL like pattern (assuming escape {@code \})
     * @return {@code true} if the string contains any like special characters
     */
    static boolean containsLikeSpecialChars(String sqlLikePattern) {
        for (int idx = 0; idx < sqlLikePattern.length(); idx++) {
            if (isLikeSpecialChar(sqlLikePattern.charAt(idx))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if character is a SQL like special.
     *
     * @param charVal
     *         Character to check
     * @return {@code true} if {@code charVal} is a SQL like special ({@code \_%})
     */
    static boolean isLikeSpecialChar(char charVal) {
        return charVal == '%' || charVal == '_' || charVal == '\\';
    }

    private static final class SimpleEqualsMatcher extends SqlLikeMatcher {

        private final String sqlLikePattern;

        private SimpleEqualsMatcher(String sqlLikePattern) {
            this.sqlLikePattern = sqlLikePattern;
        }

        @Override
        public boolean matches(String value) {
            return sqlLikePattern.equals(value);
        }

    }

    private static final class RegexMatcher extends SqlLikeMatcher {

        private final Matcher regexMatcher;

        private RegexMatcher(String sqlLikePattern) {
            String regexPattern = SqlLikeMatcher.sqlLikeToRegex(sqlLikePattern);
            Pattern compiledPattern = Pattern.compile(regexPattern);
            regexMatcher = compiledPattern.matcher("");
        }

        @Override
        public boolean matches(String value) {
            if (value == null) {
                return false;
            }
            regexMatcher.reset(value);
            return regexMatcher.matches();
        }
    }

    /**
     * Creates a regular expression pattern equivalent to the provided SQL {@code LIKE} pattern.
     *
     * @param sqlLike
     *         SQL like pattern (assuming escape {@code \})
     * @return Pattern for the provided like string.
     */
    static String sqlLikeToRegex(String sqlLike) {
        final int sqlLikeLength = sqlLike.length();
        // Derivation of additional 10: 8 chars for 2x quote pair (\Q..\E) + 2 chars for .*
        final StringBuilder patternString = new StringBuilder(sqlLikeLength + 10);
        final StringBuilder subPattern = new StringBuilder();
        for (int idx = 0; idx < sqlLikeLength; idx++) {
            char charVal = sqlLike.charAt(idx);
            switch (charVal) {
            case '_':
            case '%':
                if (subPattern.length() > 0) {
                    patternString.append(Pattern.quote(subPattern.toString()));
                    subPattern.setLength(0);
                }
                patternString.append(charVal == '_' ? "." : ".*");
                break;
            case '\\':
                idx += 1;
                if (idx < sqlLikeLength) {
                    char nextChar = sqlLike.charAt(idx);
                    if (!isLikeSpecialChar(nextChar)) {
                        // backslash before non-escapable character
                        // technically invalid escape, but add escape character as normal character
                        subPattern.append('\\');
                    }
                    subPattern.append(nextChar);
                } else {
                    // backslash at end of string,
                    // technically invalid escape, but add escape character as normal character
                    subPattern.append('\\');
                }
                break;
            default:
                subPattern.append(charVal);
                break;
            }
        }

        if (subPattern.length() > 0) {
            patternString.append(Pattern.quote(subPattern.toString()));
        }

        return patternString.toString();
    }

}
