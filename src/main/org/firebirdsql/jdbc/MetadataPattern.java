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

import org.firebirdsql.util.MetadataPatternMatcher;

/**
 * Holder of a database metadata pattern.
 * <p>
 * Provides information whether the pattern is the all-pattern, or if the condition needs a normal equality comparison,
 * or a SQL {@code LIKE}.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class MetadataPattern {

    private static final MetadataPattern ALL_PATTERN = new MetadataPattern(ConditionType.NONE, null);
    private static final MetadataPattern EMPTY_PATTERN = new MetadataPattern(ConditionType.SQL_EQUALS, "");

    private final ConditionType conditionType;
    private final String conditionValue;

    /**
     * Create a metadata pattern.
     *
     * @param conditionType Type of condition to be used.
     * @param conditionValue Value to be used with the specified condition type
     */
    private MetadataPattern(ConditionType conditionType, String conditionValue) {
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
    }

    /**
     * @return Type of condition to use for this metadata pattern
     */
    final ConditionType getConditionType() {
        return conditionType;
    }

    /**
     * @return Value for the condition (this may be different than the original pattern value)
     */
    final String getConditionValue() {
        return conditionValue;
    }

    /**
     * Compiles the metadata pattern.
     *
     * @param metadataPattern Metadata pattern string
     * @return MetadataPattern instance
     */
    static MetadataPattern compile(String metadataPattern) {
        if (isAllCondition(metadataPattern)) {
            return ALL_PATTERN;
        }
        if (metadataPattern.isEmpty()) {
            return EMPTY_PATTERN;
        }
        if (!MetadataPatternMatcher.containsPatternSpecialChars(metadataPattern)) {
            return new MetadataPattern(ConditionType.SQL_EQUALS, metadataPattern);
        }

        ConditionType conditionType = ConditionType.SQL_EQUALS;
        boolean hasEscape = false;
        final int patternLength = metadataPattern.length();
        final StringBuilder likePattern = new StringBuilder(patternLength);
        for (int idx = 0; idx < patternLength; idx++) {
            char ch = metadataPattern.charAt(idx);
            switch (ch) {
            case '%':
            case '_':
                conditionType = ConditionType.SQL_LIKE;
                likePattern.append(ch);
                break;
            case '\\':
                hasEscape = true;
                if (idx < patternLength - 1
                        && MetadataPatternMatcher.isPatternSpecialChar(metadataPattern.charAt(idx + 1))) {
                    // We add the character here so we skip it in the next iteration to distinguish
                    // unescaped wildcards (which trigger SQL_LIKE) from escaped wildcards
                    likePattern.append('\\').append(metadataPattern.charAt(++idx));
                } else {
                    // Escape followed by non-special or end of string: introduce escape
                    likePattern.append('\\').append('\\');
                }
                break;
            default:
                likePattern.append(ch);
                break;
            }
        }

        if (!hasEscape) {
            return new MetadataPattern(conditionType, metadataPattern);
        }

        if (conditionType == ConditionType.SQL_EQUALS) {
            // We have no plain (unescaped) wildcards; strip escapes so we can use plain equals
            for (int idx = 0; idx < likePattern.length(); idx++) {
                if (likePattern.charAt(idx) == '\\') {
                    // This removes the escape and then skips the next (previously escaped) character
                    likePattern.deleteCharAt(idx);
                }
            }
        }

        return new MetadataPattern(conditionType, likePattern.toString());
    }

    static boolean isAllCondition(String metadataPattern) {
        return metadataPattern == null || "%".equals(metadataPattern);
    }

    enum ConditionType {
        NONE,
        SQL_LIKE,
        SQL_EQUALS
    }

}
