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

import org.firebirdsql.util.InternalApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Condition clause for constructing metadata query conditions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
@InternalApi
public final class Clause {

    private final String condition;
    private final String value;

    /**
     * Creates a metadata conditional clause.
     *
     * @param columnName
     *         Column name or expression resulting in a string value
     * @param pattern
     *         Metadata pattern
     */
    public Clause(String columnName, String pattern) {
        MetadataPattern metadataPattern = MetadataPattern.compile(pattern);
        condition = metadataPattern.renderCondition(columnName);
        value = metadataPattern.getConditionValue();
    }

    /**
     * @return The condition for this clause suffixed with {@code " and "}, or empty string if the condition is empty
     */
    public String getCondition() {
        return getCondition(true);
    }

    /**
     * @return The condition for this clause suffixed with {@code " and "} when {@code includeAnd} is {@code true}, or
     * empty string if the condition is empty
     */
    public String getCondition(boolean includeAnd) {
        if (includeAnd) {
            return getCondition("", " and ");
        }
        return getPlainCondition();
    }

    /**
     * Condition rendered with prefix and suffix.
     *
     * @param prefix Prefix
     * @param suffix Suffix
     * @return The condition for this clause prefixed with {@code prefix} and suffixed with {@code suffix}, or empty
     * string if the condition is empty
     */
    public String getCondition(String prefix, String suffix) {
        if (emptyCondition()) {
            return "";
        }
        if (Objects.equals("", prefix) && Objects.equals("", suffix)) {
            return condition;
        }
        return prefix + condition + suffix;
    }

    private String getPlainCondition() {
        if (emptyCondition()) {
            return "";
        }
        return condition;
    }

    public String getValue() {
        return value;
    }

    public boolean hasCondition() {
        return !emptyCondition();
    }

    private boolean emptyCondition() {
        return condition.isEmpty();
    }

    public static boolean anyCondition(Clause clause1, Clause clause2) {
        return clause1.hasCondition() || clause2.hasCondition();
    }

    public static boolean anyCondition(Clause... clauses) {
        for (Clause clause : clauses) {
            if (clause.hasCondition()) {
                return true;
            }
        }
        return false;
    }

    public static List<String> parameters(Clause clause1) {
        if (clause1.hasCondition()) {
            return Collections.singletonList(clause1.getValue());
        }
        return Collections.emptyList();
    }

    public static List<String> parameters(Clause clause1, Clause clause2) {
        if (!anyCondition(clause1, clause2)) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>(2);
        if (clause1.hasCondition()) {
            list.add(clause1.getValue());
        }
        if (clause2.hasCondition()) {
            list.add(clause2.getValue());
        }
        return list;
    }

    public static List<String> parameters(Clause... clauses) {
        List<String> list = new ArrayList<>(clauses.length);
        for (Clause clause : clauses) {
            if (clause.hasCondition()) {
                list.add(clause.getValue());
            }
        }
        return list;
    }
}
