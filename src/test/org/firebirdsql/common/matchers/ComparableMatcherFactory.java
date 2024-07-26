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

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Factory for matchers to assert comparable values.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class ComparableMatcherFactory {

    private ComparableMatcherFactory() {
        // no instances
    }

    /**
     * Creates a {@link Comparable} matcher for the specified {@code comparison}.
     *
     * @param comparison
     *         comparison to assert ({@code <}, {@code <=}, {@code ==}, {@code >=}, {@code >}, or {@code lessThan},
     *         {@code lessThanOrEqualTo}, {@code equalTo}, {@code greaterThanOrEqualTo}, {@code greaterThan})
     * @param operand
     *         operand to compare against
     * @param <T>
     *         type parameter
     * @return matcher to assert the comparison
     * @throws IllegalArgumentException
     *         for an unsupported value for {@code comparison}
     */
    public static <T extends Comparable<T>> Matcher<T> compares(String comparison, T operand) {
        return switch (comparison) {
            case "<", "lessThan" -> Matchers.lessThan(operand);
            case "<=", "lessThanOrEqualTo" -> Matchers.lessThanOrEqualTo(operand);
            case "==", "equalTo" -> Matchers.comparesEqualTo(operand);
            case ">=", "greaterThanOrEqualTo" -> Matchers.greaterThanOrEqualTo(operand);
            case ">", "greaterThan" -> Matchers.greaterThan(operand);
            default -> throw new IllegalArgumentException("Unsupported comparison: " + comparison);
        };
    }
}
