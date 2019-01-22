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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * @since 4.0
 */
abstract class MetadataPatternMatcher {

    private MetadataPatternMatcher() {
        // Only allow derivation in nested classes
    }

    /**
     * Derives a metadata pattern matcher from a metadata pattern instance.
     *
     * @param metadataPattern
     *         Metadata pattern instance
     * @return Matcher for {@code metadataPattern}
     */
    static MetadataPatternMatcher fromPattern(MetadataPattern metadataPattern) {
        switch (metadataPattern.getConditionType()) {
        case NONE:
            return AllMatcher.INSTANCE;
        case SQL_EQUALS:
            return new EqualsMatcher(metadataPattern.getConditionValue());
        case SQL_STARTING_WITH:
            return new StartingWithMatcher(metadataPattern.getConditionValue());
        case SQL_LIKE:
            return new LikeMatcher(metadataPattern.getConditionValue());
        default:
            throw new AssertionError("Unexpected condition type " + metadataPattern.getConditionType());
        }
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
    abstract boolean matches(String value);

    private static final class AllMatcher extends MetadataPatternMatcher {

        private static final AllMatcher INSTANCE = new AllMatcher();

        @Override
        boolean matches(String value) {
            return true;
        }
    }

    private static final class EqualsMatcher extends MetadataPatternMatcher {

        private final String pattern;

        private EqualsMatcher(String pattern) {
            this.pattern = pattern;
        }

        @Override
        boolean matches(String value) {
            return pattern.equals(value);
        }

    }

    private static final class StartingWithMatcher extends MetadataPatternMatcher {

        private final String pattern;

        private StartingWithMatcher(String pattern) {
            this.pattern = pattern;
        }

        @Override
        boolean matches(String value) {
            return value != null && value.startsWith(pattern);
        }

    }

    private static final class LikeMatcher extends MetadataPatternMatcher {

        private final Matcher regexMatcher;

        private LikeMatcher(String pattern) {
            String regexPattern = MetadataPatternMatcher.patternToRegex(pattern);
            Pattern compiledPattern = Pattern.compile(regexPattern);
            regexMatcher = compiledPattern.matcher("");
        }

        @Override
        boolean matches(String value) {
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
                    if (!MetadataPattern.isPatternSpecialChar(nextChar)) {
                        // backslash before non-escapable character, handle as normal, see JDBC-562 and ODBC spec
                        // NOTE: given the use of MetadataPattern, this situation will not occur
                        subPattern.append('\\');
                    }
                    subPattern.append(nextChar);
                } else {
                    // backslash at end of string, handled as normal, see JDBC-562 and ODBC spec
                    // NOTE: given the use of MetadataPattern, this situation will not occur
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
