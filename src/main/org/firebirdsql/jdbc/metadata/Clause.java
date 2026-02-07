// SPDX-FileCopyrightText: Copyright 2001-2026 Firebird development team and individual contributors
// SPDX-FileCopyrightText: Copyright 2019-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * Condition clause for constructing metadata query conditions.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
@InternalApi
public final class Clause {

    private final String condition;
    private final @Nullable String value;

    /**
     * Creates a metadata conditional clause.
     *
     * @param columnName
     *         Column name or expression resulting in a string value
     * @param pattern
     *         Metadata pattern
     */
    public Clause(String columnName, @Nullable String pattern) {
        this(columnName, MetadataPattern.compile(pattern));
    }

    private Clause(String columnName, MetadataPattern metadataPattern) {
        condition = metadataPattern.renderCondition(columnName);
        value = metadataPattern.getConditionValue();
    }

    /**
     * Creates an equals clause.
     *
     * @param columnName
     *         Column name or expression resulting in a string value
     * @param value
     *         value for equals condition
     * @return clause for a SQL equals ({@code =}) condition
     */
    public static Clause equalsClause(String columnName, String value) {
        return new Clause(columnName, MetadataPattern.equalsCondition(value));
    }

    /**
     * Creates an {@code IS NULL} clause.
     *
     * @param columnName
     *         column name or expression resulting in a null value
     * @return clause for SQL {@code IS NULL} condition
     */
    static Clause isNullClause(String columnName) {
        return new Clause(columnName, MetadataPattern.isNullCondition());
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

    public @Nullable String getValue() {
        return value;
    }

    public boolean hasCondition() {
        return !emptyCondition();
    }

    private boolean emptyCondition() {
        return condition.isEmpty();
    }

    private boolean hasValue() {
        return value != null;
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

    public static boolean anyCondition(List<Clause> clauses) {
        for (Clause clause : clauses) {
            if (clause.hasCondition()) {
                return true;
            }
        }
        return false;
    }

    private static boolean anyParameter(Clause clause1, Clause clause2) {
        return clause1.hasCondition() && clause1.hasValue() || clause2.hasCondition() && clause2.hasValue();
    }

    public static List<String> parameters(Clause clause1) {
        if (clause1.hasCondition() && clause1.hasValue()) {
            //noinspection NullableProblems : hasValue() ensures it's not null
            return Collections.singletonList(clause1.getValue());
        }
        return Collections.emptyList();
    }

    public static List<String> parameters(Clause clause1, Clause clause2) {
        if (!anyParameter(clause1, clause2)) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>(2);
        if (clause1.hasCondition() && clause1.hasValue()) {
            //noinspection DataFlowIssue : hasValue() ensures it's not null
            list.add(clause1.getValue());
        }
        if (clause2.hasCondition() && clause2.hasValue()) {
            //noinspection DataFlowIssue : hasValue() ensures it's not null
            list.add(clause2.getValue());
        }
        return list;
    }

    public static List<String> parameters(Clause... clauses) {
        return parameters(Arrays.asList(clauses));
    }

    public static List<String> parameters(List<Clause> clauses) {
        List<String> list = new ArrayList<>(clauses.size());
        for (Clause clause : clauses) {
            if (clause.hasCondition() && clause.hasValue()) {
                //noinspection DataFlowIssue : hasValue() ensures it's not null
                list.add(clause.getValue());
            }
        }
        return list;
    }

    public static String conjunction(Clause... clauses) {
        return conjunction(Arrays.asList(clauses));
    }

    public static String conjunction(List<Clause> clauses) {
        return clauses.stream()
                .filter(Clause::hasCondition)
                .map(clause -> clause.getCondition(false))
                .collect(joining("\nand "));
    }

}
