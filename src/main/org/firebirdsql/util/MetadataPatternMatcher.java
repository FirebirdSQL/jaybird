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
 * Emulates behavior of a database metadata pattern.
 * <p>
 * This behaves similar to (but not 100% identical to) a SQL {@code LIKE} pattern with {@code ESCAPE '\'} clause.
 * </p>
 * <p>
 * This implementation is not thread-safe.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public abstract class MetadataPatternMatcher {

    private MetadataPatternMatcher() {
        // Only allow derivation in nested classes
    }

    /**
     * Compiles a database metadata pattern.
     *
     * @param pattern
     *         database metadata pattern (non-null)
     * @return Matcher for {@code pattern}
     */
    public static MetadataPatternMatcher compile(String pattern) {
        if (containsPatternSpecialChars(requireNonNull(pattern, "pattern"))) {
            return new RegexMatcher(pattern);
        }
        return new SimpleEqualsMatcher(pattern);
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
     * @param pattern
     *         SQL like pattern (assuming escape {@code \})
     * @return {@code true} if the string contains any like special characters
     */
    static boolean containsPatternSpecialChars(String pattern) {
        for (int idx = 0; idx < pattern.length(); idx++) {
            if (isPatternSpecialChar(pattern.charAt(idx))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if character is a database metadata pattern special.
     *
     * @param charVal
     *         Character to check
     * @return {@code true} if {@code charVal} is a SQL like special ({@code \_%})
     */
    static boolean isPatternSpecialChar(char charVal) {
        return charVal == '%' || charVal == '_' || charVal == '\\';
    }

    private static final class SimpleEqualsMatcher extends MetadataPatternMatcher {

        private final String pattern;

        private SimpleEqualsMatcher(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean matches(String value) {
            return pattern.equals(value);
        }

    }

    private static final class RegexMatcher extends MetadataPatternMatcher {

        private final Matcher regexMatcher;

        private RegexMatcher(String pattern) {
            String regexPattern = MetadataPatternMatcher.patternToRegex(pattern);
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
     * Creates a regular expression pattern equivalent to the provided database metadata pattern.
     *
     * @param metadataPattern
     *         database metadata pattern
     * @return Pattern for the provided like string.
     */
    static String patternToRegex(final String metadataPattern) {
        final int patternLength = metadataPattern.length();
        // Derivation of additional 10: 8 chars for 2x quote pair (\Q..\E) + 2 chars for .*
        final StringBuilder patternString = new StringBuilder(patternLength + 10);
        final StringBuilder subPattern = new StringBuilder();
        for (int idx = 0; idx < patternLength; idx++) {
            char charVal = metadataPattern.charAt(idx);
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
                if (idx < patternLength) {
                    char nextChar = metadataPattern.charAt(idx);
                    if (!isPatternSpecialChar(nextChar)) {
                        // backslash before non-escapable character, handle as normal, see JDBC-562 and ODBC spec
                        subPattern.append('\\');
                    }
                    subPattern.append(nextChar);
                } else {
                    // backslash at end of string, handled as normal, see JDBC-562 and ODBC spec
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
